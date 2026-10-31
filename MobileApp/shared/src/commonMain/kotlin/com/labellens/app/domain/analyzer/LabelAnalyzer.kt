package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.KnowledgeIngredient
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile

class LabelAnalyzer {

    fun analyze(
        productName: String,
        rawLabelText: String,
        userProfile: UserProfile,
        category: ProductCategory = ProductCategory.FOOD,
        scanSource: ScanSource = ScanSource.MANUAL_TEXT,
        barcode: String? = null,
        sourceAttribution: String? = null,
        language: AppLanguage = AppLanguage.DEFAULT,
    ): LabelAnalysisResult {
        val normalizedText = rawLabelText.trim()
        val uncertaintyNotes = mutableListOf<String>()

        if (normalizedText.length < 15) {
            uncertaintyNotes.add(AnalysisStrings.uncertaintyShortText(language))
        }

        // 1. Split and parse raw ingredients
        val rawTokens = extractIngredientTokens(normalizedText)
        val matchedIngredients = mutableListOf<MatchedItem>()
        val unclassifiedTokens = mutableListOf<String>()

        for (token in rawTokens) {
            val match = findBestMatch(token)
            if (match != null) {
                matchedIngredients.add(MatchedItem(rawToken = token, ingredient = match))
            } else {
                unclassifiedTokens.add(token)
            }
        }

        // Also check explicit "CONTAINS:" or allergen statements on label
        val containsSection = extractContainsSection(normalizedText)
        if (containsSection.isNotBlank()) {
            val allergenTokens = containsSection.split(Regex("[,;、，\n.]"))
                .map { it.replace("함유", "").replace("を含む", "").replace("由来", "").trim() }
                .filter { it.isNotBlank() }
            for (token in allergenTokens) {
                val match = findBestMatch(token)
                if (match != null && matchedIngredients.none { it.ingredient.canonicalName == match.canonicalName }) {
                    matchedIngredients.add(MatchedItem(rawToken = token, ingredient = match))
                }
            }
        }

        if (unclassifiedTokens.isNotEmpty() && matchedIngredients.isEmpty()) {
            uncertaintyNotes.add(AnalysisStrings.uncertaintyUnclassified(language, unclassifiedTokens.take(3).joinToString(", ")))
        }

        // 2. Parse nutrition facts
        val nutritionFacts = extractNutritionFacts(normalizedText, language)
        val highSodiumDetected = isHighSodium(nutritionFacts, normalizedText, language)
        val highSugarDetected = isHighSugar(nutritionFacts, normalizedText, language)

        // 3. Evaluate Sections: Watch out for you (Personal ONLY), Worth noting, Good to know
        val watchOutForYou = mutableListOf<FlaggedItem>()
        val worthNoting = mutableListOf<FlaggedItem>()
        val goodToKnow = mutableListOf<FlaggedItem>()

        // Personal Allergen checks
        for (allergen in userProfile.allergies) {
            val match = matchedIngredients.firstOrNull { it.ingredient.allergenMatch == allergen }
            if (match != null) {
                val ingDisplayName = match.ingredient.displayName(language)
                watchOutForYou.add(
                    FlaggedItem(
                        title = AnalysisStrings.allergenTitle(language, allergen),
                        detail = AnalysisStrings.allergenDetail(language, ingDisplayName),
                        severity = FlagSeverity.PERSONAL_ALLERGEN,
                        relatedIngredient = ingDisplayName,
                    ),
                )
            }
        }

        // Personal Avoid preference checks
        for (avoid in userProfile.avoidList) {
            when (avoid) {
                AvoidPreference.HIGH_SODIUM -> {
                    if (highSodiumDetected) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.highSodiumTitle(language),
                                detail = AnalysisStrings.highSodiumDetail(language),
                                severity = FlagSeverity.PERSONAL_AVOID,
                            ),
                        )
                    }
                }

                AvoidPreference.ADDED_SUGAR -> {
                    val sugarMatch = matchedIngredients.firstOrNull {
                        it.ingredient.avoidMatch == AvoidPreference.ADDED_SUGAR
                    }
                    if (sugarMatch != null || highSugarDetected) {
                        val sugarSource = sugarMatch?.ingredient?.displayName(language)
                            ?: if (language == AppLanguage.VIETNAMESE) "thành phần dinh dưỡng" else "identified in nutrition facts"
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.addedSugarTitle(language),
                                detail = AnalysisStrings.addedSugarDetail(language, sugarSource),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = sugarMatch?.ingredient?.displayName(language),
                            ),
                        )
                    }
                }

                else -> {
                    val match = matchedIngredients.firstOrNull { it.ingredient.avoidMatch == avoid }
                    if (match != null) {
                        val ingDisplayName = match.ingredient.displayName(language)
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.avoidTitle(language, avoid),
                                detail = AnalysisStrings.avoidDetail(language, ingDisplayName),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = ingDisplayName,
                            ),
                        )
                    }
                }
            }
        }

        // Personal Dietary Goals
        for (goal in userProfile.goals) {
            val sodiumTitle = AnalysisStrings.highSodiumTitle(language)
            when (goal) {
                DietaryGoal.LOW_SODIUM -> {
                    if (highSodiumDetected && watchOutForYou.none { it.title == sodiumTitle }) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalWatchSodiumTitle(language, goal),
                                detail = AnalysisStrings.goalWatchSodiumDetail(language),
                                severity = FlagSeverity.PERSONAL_AVOID,
                            ),
                        )
                    }
                }

                DietaryGoal.LOW_SUGAR, DietaryGoal.DIABETES_CAUTIOUS -> {
                    val sugarTitle = AnalysisStrings.addedSugarTitle(language)
                    if (highSugarDetected && watchOutForYou.none { it.title == sugarTitle || it.title.contains("Sugar") || it.title.contains("Đường") }) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalElevatedSugarTitle(language),
                                detail = AnalysisStrings.goalElevatedSugarDetail(language, goal),
                                severity = FlagSeverity.PERSONAL_AVOID,
                            ),
                        )
                    }
                }

                DietaryGoal.VEGAN -> {
                    val nonVegan = matchedIngredients.firstOrNull {
                        it.ingredient.avoidForTags.contains("non-vegan") ||
                            it.ingredient.allergenMatch in listOf(Allergen.MILK, Allergen.EGG, Allergen.FISH, Allergen.SHELLFISH)
                    }
                    if (nonVegan != null) {
                        val ingDisplayName = nonVegan.ingredient.displayName(language)
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalNotVeganTitle(language),
                                detail = AnalysisStrings.goalNotVeganDetail(language, ingDisplayName),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = ingDisplayName,
                            ),
                        )
                    }
                }

                DietaryGoal.VEGETARIAN -> {
                    val nonVeg = matchedIngredients.firstOrNull {
                        it.ingredient.avoidForTags.contains("non-vegetarian") ||
                            it.ingredient.allergenMatch in listOf(Allergen.FISH, Allergen.SHELLFISH)
                    }
                    if (nonVeg != null) {
                        val ingDisplayName = nonVeg.ingredient.displayName(language)
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalNotVegetarianTitle(language),
                                detail = AnalysisStrings.goalNotVegetarianDetail(language, ingDisplayName),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = ingDisplayName,
                            ),
                        )
                    }
                }

                DietaryGoal.HALAL_CONSCIOUS -> {
                    val porkItem = matchedIngredients.firstOrNull { it.ingredient.avoidForTags.contains("pork") || it.ingredient.avoidForTags.contains("non-halal") }
                    if (porkItem != null) {
                        val ingDisplayName = porkItem.ingredient.displayName(language)
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalNonHalalTitle(language),
                                detail = AnalysisStrings.goalNonHalalDetail(language, ingDisplayName),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = ingDisplayName,
                            ),
                        )
                    }
                }

                DietaryGoal.PREGNANCY_SAFE -> {
                    val caffeine = matchedIngredients.firstOrNull { it.ingredient.avoidForTags.contains("caffeine") }
                    if (caffeine != null) {
                        val ingDisplayName = caffeine.ingredient.displayName(language)
                        watchOutForYou.add(
                            FlaggedItem(
                                title = AnalysisStrings.goalCaffeineTitle(language),
                                detail = AnalysisStrings.goalCaffeineDetail(language, ingDisplayName),
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = ingDisplayName,
                            ),
                        )
                    }
                }
            }
        }

        // Worth Noting (General notes: additives, intense sweeteners, MSG, preservatives, saturated fats)
        for (item in matchedIngredients) {
            val ing = item.ingredient
            val ingDisplayName = ing.displayName(language)
            val isAlreadyFlagged = watchOutForYou.any {
                it.relatedIngredient == ingDisplayName || it.relatedIngredient == ing.canonicalName
            }
            if (ing.avoidMatch != null && !isAlreadyFlagged) {
                worthNoting.add(
                    FlaggedItem(
                        title = ingDisplayName,
                        detail = AnalysisStrings.additiveUsedAs(language, ing.plainExplanation(language), ing.category(language)),
                        severity = FlagSeverity.WORTH_NOTING,
                        relatedIngredient = ingDisplayName,
                    ),
                )
            } else if (ing.category.contains("Preservative", ignoreCase = true) ||
                ing.category.contains("Synthetic", ignoreCase = true) ||
                ing.category.contains("Acidulant", ignoreCase = true) ||
                ing.avoidForTags.contains("flavor-enhancer")
            ) {
                if (worthNoting.none { it.title == ingDisplayName || it.title == ing.canonicalName } && !isAlreadyFlagged) {
                    worthNoting.add(
                        FlaggedItem(
                            title = ingDisplayName,
                            detail = "${ing.whyUsed(language)} (${ing.doseContextNote(language)})",
                            severity = FlagSeverity.WORTH_NOTING,
                            relatedIngredient = ingDisplayName,
                        ),
                    )
                }
            }
        }

        // Good to Know (Positive attributes, whole grains, vitamins, natural botanicals, healthy oils)
        for (item in matchedIngredients) {
            val ing = item.ingredient
            if (ing.isGenerallyPositive) {
                val ingDisplayName = ing.displayName(language)
                goodToKnow.add(
                    FlaggedItem(
                        title = ingDisplayName,
                        detail = "${ing.plainExplanation(language)} ${ing.doseContextNote(language)}",
                        severity = FlagSeverity.GOOD_TO_KNOW,
                        relatedIngredient = ingDisplayName,
                    ),
                )
            }
        }

        // 4. Generate Full Breakdown list
        val fullBreakdown = mutableListOf<IngredientBreakdown>()
        for (item in matchedIngredients.distinctBy { it.ingredient.canonicalName }) {
            val ing = item.ingredient
            fullBreakdown.add(
                IngredientBreakdown(
                    rawName = item.rawToken,
                    canonicalName = ing.displayName(language),
                    category = ing.category(language),
                    plainExplanation = ing.plainExplanation(language),
                    whyUsed = ing.whyUsed(language),
                    doseContextNote = ing.doseContextNote(language),
                    avoidForTags = ing.avoidForTags,
                    isKnown = true,
                    matchedAliases = ing.aliases.take(5),
                ),
            )
        }
        for (token in unclassifiedTokens.distinct()) {
            if (token.isNotBlank() && token.length > 2) {
                fullBreakdown.add(
                    IngredientBreakdown(
                        rawName = token,
                        canonicalName = token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        category = AnalysisStrings.fallbackCategory(language),
                        plainExplanation = AnalysisStrings.fallbackPlainExplanation(language),
                        whyUsed = AnalysisStrings.fallbackWhyUsed(language),
                        doseContextNote = AnalysisStrings.fallbackDoseContextNote(language),
                        isKnown = false,
                    ),
                )
            }
        }

        // 5. Construct Plain-Language Verdict
        val verdict = generateVerdict(
            productName = productName,
            category = category,
            watchOutCount = watchOutForYou.size,
            worthNotingCount = worthNoting.size,
            goodToKnowCount = goodToKnow.size,
            matchedIngredients = matchedIngredients.map { it.ingredient },
            nutritionFacts = nutritionFacts,
            language = language,
        )

        val quickGlance = AnalysisStrings.quickGlance(language, fullBreakdown.size, watchOutForYou.size)

        return LabelAnalysisResult(
            productName = productName,
            category = category,
            scanSource = scanSource,
            verdict = verdict,
            watchOutForYou = watchOutForYou,
            worthNoting = worthNoting,
            goodToKnow = goodToKnow,
            fullBreakdown = fullBreakdown,
            nutritionHighlights = nutritionFacts,
            rawInputText = normalizedText,
            uncertaintyNotes = uncertaintyNotes,
            barcode = barcode,
            quickGlance = quickGlance,
            sourceAttribution = sourceAttribution,
        )
    }

    private data class MatchedItem(val rawToken: String, val ingredient: KnowledgeIngredient)

    private fun extractIngredientTokens(text: String): List<String> {
        val lower = text.lowercase()
        val ingredientsPart = when {
            lower.contains("ingredients:") -> text.substring(lower.indexOf("ingredients:") + 12)
            lower.contains("원재료명:") -> text.substring(lower.indexOf("원재료명:") + 5)
            lower.contains("원재료명 및 함량:") -> text.substring(lower.indexOf("원재료명 및 함량:") + 11)
            lower.contains("原材料名:") -> text.substring(lower.indexOf("原材料名:") + 5)
            lower.contains("配料:") -> text.substring(lower.indexOf("配料:") + 3)
            lower.contains("配料表:") -> text.substring(lower.indexOf("配料表:") + 4)
            lower.contains("thành phần:") -> text.substring(lower.indexOf("thành phần:") + 11)
            else -> text
        }

        val cleaned = ingredientsPart
            .substringBefore("Nutrition Facts")
            .substringBefore("영양정보")
            .substringBefore("栄養成分")
            .substringBefore("营养成分")
            .substringBefore("Thông tin dinh dưỡng")
            .substringBefore("CONTAINS:")
            .substringBefore("알레르기")
            .substringBefore("FREE OF:")
            .substringBefore("Made in a facility free from")
            .substringBefore("Manufactured in a facility that also processes")

        // Split by commas, semicolons, Korean/Japanese punctuation, or brackets
        val splitRegex = Regex("[,;、，\n]")
        return cleaned.split(splitRegex)
            .map { it.replace(Regex("[()\\[\\]{}•.]"), " ").trim() }
            .filter { it.length in 2..60 }
    }

    private fun extractContainsSection(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("contains:") -> {
                val after = text.substring(lower.indexOf("contains:") + 9)
                after.substringBefore("MANUFACTURED IN")
                    .substringBefore("Manufactured in")
                    .substringBefore("MADE IN")
                    .substringBefore("Made in")
                    .substringBefore("DISTRIBUTED")
                    .substringBefore("FREE OF")
                    .substringBefore("\n\n")
            }

            lower.contains("chứa:") -> {
                val after = text.substring(lower.indexOf("chứa:") + 5)
                after.substringBefore("\n\n")
            }

            lower.contains("알레르기 정보:") -> {
                val after = text.substring(lower.indexOf("알레르기 정보:") + 8)
                after.substringBefore("\n\n")
            }

            lower.contains("알레르기:") -> {
                val after = text.substring(lower.indexOf("알레르기:") + 5)
                after.substringBefore("\n\n")
            }

            lower.contains("アレルギー物質:") -> {
                val after = text.substring(lower.indexOf("アレルギー物質:") + 8)
                after.substringBefore("\n\n")
            }

            lower.contains("アレルゲン情報:") -> {
                val after = text.substring(lower.indexOf("アレルゲン情報:") + 8)
                after.substringBefore("\n\n")
            }

            else -> ""
        }
    }

    private fun findBestMatch(token: String): KnowledgeIngredient? {
        val cleanToken = token.lowercase().trim()
        if (cleanToken.isEmpty()) return null

        return KnowledgeBase.ingredients.firstOrNull { ing ->
            if (cleanToken == ing.canonicalName.lowercase()) return@firstOrNull true
            if (cleanToken == ing.displayName(AppLanguage.VIETNAMESE).lowercase()) return@firstOrNull true
            ing.aliases.any { alias ->
                val cAlias = alias.lowercase().trim()
                if (cAlias.length <= 2) {
                    cleanToken == cAlias
                } else if (cAlias == "milk" || cAlias == "butter") {
                    // Prevent "coconut milk" matching dairy milk, or "almond butter" matching dairy butter
                    if (cleanToken.contains("coconut milk") || cleanToken.contains("almond butter") || cleanToken.contains("peanut butter") || cleanToken.contains("cocoa butter")) {
                        false
                    } else {
                        val wordBoundary = Regex("\\b${Regex.escape(cAlias)}\\b")
                        wordBoundary.containsMatchIn(cleanToken)
                    }
                } else {
                    val wordBoundary = Regex("\\b${Regex.escape(cAlias)}\\b")
                    wordBoundary.containsMatchIn(cleanToken) || cleanToken.contains(cAlias)
                }
            }
        }
    }

    private fun extractNutritionFacts(text: String, language: AppLanguage): List<NutritionFact> {
        val facts = mutableListOf<NutritionFact>()

        // Patterns to match: Calories, Total Sugar/Sugars, Sodium, Saturated Fat, Protein, Dietary Fiber
        val caloriesPattern = Regex("(?i)(calories|칼로리|열량|エネルギー|热量|năng lượng)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:kcal|cal)?")
        val altCaloriesPattern = Regex("(?i)([0-9]+(?:\\.[0-9]+)?)\\s*(?:kcal)")
        val sugarPattern = Regex("(?i)(total sugar|sugars|sugar|당류|糖類|糖|đường)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val sodiumPattern = Regex("(?i)(sodium|나트륨|ナトリウム|钠|natri)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(mg|g)?")
        val satFatPattern = Regex("(?i)(saturated fat|sat fat|포화지방|飽和脂肪酸|饱和脂肪|chất béo bão hòa)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val proteinPattern = Regex("(?i)(protein|단백질|たんぱく質|蛋白质|đạm|chất đạm)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val fiberPattern = Regex("(?i)(dietary fiber|fiber|fibre|식이섬유|食物繊維|膳食纤维|chất xơ)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")

        val calMatch = caloriesPattern.find(text) ?: altCaloriesPattern.find(text)
        calMatch?.let {
            val amount = if (it.groupValues.size > 2) it.groupValues[2] else it.groupValues[1]
            val ctxKey = if (amount.toDoubleOrNull() ?: 0.0 > 300) "Substantial" else "Standard"
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Calories"),
                    amount = "$amount kcal",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        sugarPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            val num = amount.toDoubleOrNull() ?: 0.0
            val ctxKey = if (num > 15) {
                "Notable sugar"
            } else if (num < 2) {
                "Low sugar"
            } else {
                "Moderate"
            }
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Sugars"),
                    amount = "$amount $unit",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        sodiumPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "mg" }
            val num = amount.toDoubleOrNull() ?: 0.0
            val mgVal = if (unit.equals("g", ignoreCase = true)) num * 1000 else num
            val ctxKey = if (mgVal > 800) {
                "High sodium"
            } else if (mgVal < 140) {
                "Low sodium"
            } else {
                "Moderate"
            }
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Sodium"),
                    amount = "${mgVal.toInt()} mg",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        satFatPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            val ctxKey = if ((amount.toDoubleOrNull() ?: 0.0) > 5) "Notable" else "Standard"
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Saturated Fat"),
                    amount = "$amount $unit",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        proteinPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            val ctxKey = if ((amount.toDoubleOrNull() ?: 0.0) >= 5) "Protein source" else "Standard"
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Protein"),
                    amount = "$amount $unit",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        fiberPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            val ctxKey = if ((amount.toDoubleOrNull() ?: 0.0) >= 3) "Good fiber source" else "Standard"
            facts.add(
                NutritionFact(
                    name = AnalysisStrings.nutritionName(language, "Dietary Fiber"),
                    amount = "$amount $unit",
                    contextIndicator = AnalysisStrings.nutritionContext(language, ctxKey),
                ),
            )
        }

        return facts
    }

    private fun isHighSodium(facts: List<NutritionFact>, text: String, language: AppLanguage): Boolean {
        val sodiumName = AnalysisStrings.nutritionName(language, "Sodium")
        val sodiumFact = facts.firstOrNull { it.name == sodiumName || it.name == "Sodium" }
        if (sodiumFact != null) {
            val num = sodiumFact.amount.substringBefore(" ").toDoubleOrNull() ?: 0.0
            if (num >= 800) return true
        }
        // Fallback check in text
        val match = Regex("(?i)(?:sodium|natri)\\s*([0-9]{3,})\\s*mg").find(text)
        if (match != null) {
            val mg = match.groupValues[1].toIntOrNull() ?: 0
            return mg >= 800
        }
        return false
    }

    private fun isHighSugar(facts: List<NutritionFact>, text: String, language: AppLanguage): Boolean {
        val sugarName = AnalysisStrings.nutritionName(language, "Sugars")
        val sugarFact = facts.firstOrNull { it.name == sugarName || it.name == "Sugars" }
        if (sugarFact != null) {
            val num = sugarFact.amount.substringBefore(" ").toDoubleOrNull() ?: 0.0
            if (num >= 15) return true
        }
        return false
    }

    private fun generateVerdict(
        productName: String,
        category: ProductCategory,
        watchOutCount: Int,
        worthNotingCount: Int,
        goodToKnowCount: Int,
        matchedIngredients: List<KnowledgeIngredient>,
        nutritionFacts: List<NutritionFact>,
        language: AppLanguage,
    ): String {
        val baseVerdict = when {
            category == ProductCategory.SUPPLEMENT -> {
                AnalysisStrings.verdictSupplement(language)
            }

            watchOutCount > 0 -> {
                AnalysisStrings.verdictWatchOut(language)
            }

            worthNotingCount > 2 -> {
                AnalysisStrings.verdictWorthNoting(language)
            }

            goodToKnowCount >= 2 && worthNotingCount <= 1 -> {
                AnalysisStrings.verdictGoodToKnow(language)
            }

            else -> {
                AnalysisStrings.verdictBaseFood(language)
            }
        }

        // Hard guardrail: supplement results append exactly the required disclaimer
        return if (category == ProductCategory.SUPPLEMENT) {
            "$baseVerdict ${AnalysisStrings.verdictDisclaimer(language)}"
        } else {
            baseVerdict
        }
    }
}
