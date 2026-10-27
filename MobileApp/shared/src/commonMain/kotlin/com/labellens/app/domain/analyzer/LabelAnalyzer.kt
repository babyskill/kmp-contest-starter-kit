package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
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
    ): LabelAnalysisResult {
        val normalizedText = rawLabelText.trim()
        val uncertaintyNotes = mutableListOf<String>()

        if (normalizedText.length < 15) {
            uncertaintyNotes.add("Label text appears short or incomplete. Some ingredients or nutrition values might not be captured.")
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

        // Fallback: also scan entire label text against knowledge base for missed mentions
        val allTextMatches = KnowledgeBase.findMatchingIngredients(normalizedText)
        for (item in allTextMatches) {
            if (matchedIngredients.none { it.ingredient.canonicalName == item.canonicalName }) {
                matchedIngredients.add(MatchedItem(rawToken = item.canonicalName, ingredient = item))
            }
        }

        if (unclassifiedTokens.isNotEmpty() && matchedIngredients.isEmpty()) {
            uncertaintyNotes.add("Could not find standard matches for parsed ingredients: ${unclassifiedTokens.take(3).joinToString(", ")}.")
        }

        // 2. Parse nutrition facts
        val nutritionFacts = extractNutritionFacts(normalizedText)
        val highSodiumDetected = isHighSodium(nutritionFacts, normalizedText)
        val highSugarDetected = isHighSugar(nutritionFacts, normalizedText)

        // 3. Evaluate Sections: Watch out for you (Personal ONLY), Worth noting, Good to know
        val watchOutForYou = mutableListOf<FlaggedItem>()
        val worthNoting = mutableListOf<FlaggedItem>()
        val goodToKnow = mutableListOf<FlaggedItem>()

        // Personal Allergen checks
        for (allergen in userProfile.allergies) {
            val match = matchedIngredients.firstOrNull { it.ingredient.allergenMatch == allergen }
            if (match != null) {
                watchOutForYou.add(
                    FlaggedItem(
                        title = "Contains ${allergen.displayName}",
                        detail = "Found in ingredients (${match.ingredient.canonicalName}). Matched against your profile allergen list.",
                        severity = FlagSeverity.PERSONAL_ALLERGEN,
                        relatedIngredient = match.ingredient.canonicalName,
                    ),
                )
            }
        }

        // Personal Avoid preference checks
        for (avoid in userProfile.avoidList) {
            when (avoid) {
                com.labellens.app.domain.model.AvoidPreference.HIGH_SODIUM -> {
                    if (highSodiumDetected) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "High Sodium Level",
                                detail = "This product provides a high concentration of sodium per serving, which you flagged to avoid.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                            ),
                        )
                    }
                }

                com.labellens.app.domain.model.AvoidPreference.ADDED_SUGAR -> {
                    val sugarMatch = matchedIngredients.firstOrNull {
                        it.ingredient.avoidMatch == com.labellens.app.domain.model.AvoidPreference.ADDED_SUGAR
                    }
                    if (sugarMatch != null || highSugarDetected) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Added Sugar / Sweetener",
                                detail = "Contains added sugars (${sugarMatch?.ingredient?.canonicalName ?: "identified in nutrition facts"}).",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = sugarMatch?.ingredient?.canonicalName,
                            ),
                        )
                    }
                }

                else -> {
                    val match = matchedIngredients.firstOrNull { it.ingredient.avoidMatch == avoid }
                    if (match != null) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Contains ${avoid.displayName}",
                                detail = "Identified ${match.ingredient.canonicalName}, matching your personal avoidance list.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = match.ingredient.canonicalName,
                            ),
                        )
                    }
                }
            }
        }

        // Personal Dietary Goals
        for (goal in userProfile.goals) {
            when (goal) {
                DietaryGoal.LOW_SODIUM -> {
                    if (highSodiumDetected && watchOutForYou.none { it.title == "High Sodium Level" }) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Watch Sodium (Your Goal: Low Sodium)",
                                detail = "Sodium content is notable relative to your dietary goal.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                            ),
                        )
                    }
                }

                DietaryGoal.LOW_SUGAR, DietaryGoal.DIABETES_CAUTIOUS -> {
                    if (highSugarDetected && watchOutForYou.none { it.title.contains("Sugar") }) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Elevated Sugar Content",
                                detail = "Contains notable carbohydrates/sugars, which may impact your ${goal.displayName} goal.",
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
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Not Vegan Suitable",
                                detail = "Contains ${nonVegan.ingredient.canonicalName}, derived from animal sources.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = nonVegan.ingredient.canonicalName,
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
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Not Vegetarian Suitable",
                                detail = "Contains ${nonVeg.ingredient.canonicalName}.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = nonVeg.ingredient.canonicalName,
                            ),
                        )
                    }
                }

                DietaryGoal.HALAL_CONSCIOUS -> {
                    val porkItem = matchedIngredients.firstOrNull { it.ingredient.avoidForTags.contains("pork") || it.ingredient.avoidForTags.contains("non-halal") }
                    if (porkItem != null) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Non-Halal Ingredient",
                                detail = "Contains ${porkItem.ingredient.canonicalName}.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = porkItem.ingredient.canonicalName,
                            ),
                        )
                    }
                }

                DietaryGoal.PREGNANCY_SAFE -> {
                    // Check for raw unpasteurized or specific additives
                    val caffeine = matchedIngredients.firstOrNull { it.ingredient.avoidForTags.contains("caffeine") }
                    if (caffeine != null) {
                        watchOutForYou.add(
                            FlaggedItem(
                                title = "Contains Caffeine",
                                detail = "Found in ${caffeine.ingredient.canonicalName}; moderate intake during pregnancy.",
                                severity = FlagSeverity.PERSONAL_AVOID,
                                relatedIngredient = caffeine.ingredient.canonicalName,
                            ),
                        )
                    }
                }
            }
        }

        // Worth Noting (General notes: additives, intense sweeteners, MSG, preservatives, saturated fats)
        for (item in matchedIngredients) {
            val ing = item.ingredient
            if (ing.avoidMatch != null && watchOutForYou.none { it.relatedIngredient == ing.canonicalName }) {
                worthNoting.add(
                    FlaggedItem(
                        title = ing.canonicalName,
                        detail = "${ing.plainExplanation} Used as ${ing.category.lowercase()}.",
                        severity = FlagSeverity.WORTH_NOTING,
                        relatedIngredient = ing.canonicalName,
                    ),
                )
            } else if (ing.category.contains("Preservative", ignoreCase = true) ||
                ing.category.contains("Synthetic", ignoreCase = true) ||
                ing.category.contains("Acidulant", ignoreCase = true) ||
                ing.avoidForTags.contains("flavor-enhancer")
            ) {
                if (worthNoting.none { it.title == ing.canonicalName } && watchOutForYou.none { it.relatedIngredient == ing.canonicalName }) {
                    worthNoting.add(
                        FlaggedItem(
                            title = ing.canonicalName,
                            detail = "${ing.whyUsed} (${ing.doseContextNote})",
                            severity = FlagSeverity.WORTH_NOTING,
                            relatedIngredient = ing.canonicalName,
                        ),
                    )
                }
            }
        }

        // Good to Know (Positive attributes, whole grains, vitamins, natural botanicals, healthy oils)
        for (item in matchedIngredients) {
            val ing = item.ingredient
            if (ing.isGenerallyPositive) {
                goodToKnow.add(
                    FlaggedItem(
                        title = ing.canonicalName,
                        detail = "${ing.plainExplanation} ${ing.doseContextNote}",
                        severity = FlagSeverity.GOOD_TO_KNOW,
                        relatedIngredient = ing.canonicalName,
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
                    canonicalName = ing.canonicalName,
                    category = ing.category,
                    plainExplanation = ing.plainExplanation,
                    whyUsed = ing.whyUsed,
                    doseContextNote = ing.doseContextNote,
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
                        category = "Standard ingredient",
                        plainExplanation = "Recognised label component. No specific flags noted.",
                        whyUsed = "Common recipe or processing component.",
                        doseContextNote = "Standard culinary ingredient.",
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
        )

        val quickGlance = "${fullBreakdown.size} ingredients parsed • ${if (watchOutForYou.isNotEmpty()) "${watchOutForYou.size} personal note${if (watchOutForYou.size > 1) "s" else ""}" else "No personal flags"}"

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
            else -> text
        }

        val cleaned = ingredientsPart
            .substringBefore("Nutrition Facts")
            .substringBefore("영양정보")
            .substringBefore("栄養成分")
            .substringBefore("营养成分")
            .substringBefore("CONTAINS:")
            .substringBefore("알레르기")

        // Split by commas, semicolons, Korean/Japanese punctuation, or brackets
        val splitRegex = Regex("[,;、，\n]")
        return cleaned.split(splitRegex)
            .map { it.replace(Regex("[()\\[\\]{}•.]"), " ").trim() }
            .filter { it.length in 2..60 }
    }

    private fun findBestMatch(token: String): KnowledgeIngredient? {
        val cleanToken = token.lowercase().trim()
        if (cleanToken.isEmpty()) return null

        return KnowledgeBase.ingredients.firstOrNull { ing ->
            if (cleanToken == ing.canonicalName.lowercase()) return@firstOrNull true
            ing.aliases.any { alias ->
                val cAlias = alias.lowercase().trim()
                if (cAlias.length <= 2) {
                    cleanToken == cAlias
                } else {
                    cleanToken.contains(cAlias) || cAlias.contains(cleanToken)
                }
            }
        }
    }

    private fun extractNutritionFacts(text: String): List<NutritionFact> {
        val facts = mutableListOf<NutritionFact>()

        // Patterns to match: Calories, Total Sugar/Sugars, Sodium, Saturated Fat, Protein, Dietary Fiber
        val caloriesPattern = Regex("(?i)(calories|칼로리|열량|エネルギー|热量)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:kcal|cal)?")
        val sugarPattern = Regex("(?i)(total sugar|sugars|sugar|당류|糖類|糖)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val sodiumPattern = Regex("(?i)(sodium|나트륨|ナトリウム|钠)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(mg|g)?")
        val satFatPattern = Regex("(?i)(saturated fat|sat fat|포화지방|飽和脂肪酸|饱和脂肪)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val proteinPattern = Regex("(?i)(protein|단백질|たんぱく質|蛋白质)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")
        val fiberPattern = Regex("(?i)(dietary fiber|fiber|fibre|식이섬유|食物繊維|膳食纤维)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(g|mg)?")

        caloriesPattern.find(text)?.let {
            val amount = it.groupValues[2]
            facts.add(NutritionFact(name = "Calories", amount = "$amount kcal", contextIndicator = if (amount.toDoubleOrNull() ?: 0.0 > 300) "Substantial" else "Standard"))
        }

        sugarPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            val num = amount.toDoubleOrNull() ?: 0.0
            facts.add(
                NutritionFact(
                    name = "Sugars",
                    amount = "$amount $unit",
                    contextIndicator = if (num > 15) {
                        "Notable sugar"
                    } else if (num < 2) {
                        "Low sugar"
                    } else {
                        "Moderate"
                    },
                ),
            )
        }

        sodiumPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "mg" }
            val num = amount.toDoubleOrNull() ?: 0.0
            val mgVal = if (unit.equals("g", ignoreCase = true)) num * 1000 else num
            facts.add(
                NutritionFact(
                    name = "Sodium",
                    amount = "${mgVal.toInt()} mg",
                    contextIndicator = if (mgVal > 800) {
                        "High sodium"
                    } else if (mgVal < 140) {
                        "Low sodium"
                    } else {
                        "Moderate"
                    },
                ),
            )
        }

        satFatPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            facts.add(NutritionFact(name = "Saturated Fat", amount = "$amount $unit", contextIndicator = if ((amount.toDoubleOrNull() ?: 0.0) > 5) "Notable" else "Standard"))
        }

        proteinPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            facts.add(NutritionFact(name = "Protein", amount = "$amount $unit", contextIndicator = if ((amount.toDoubleOrNull() ?: 0.0) >= 5) "Protein source" else "Standard"))
        }

        fiberPattern.find(text)?.let {
            val amount = it.groupValues[2]
            val unit = it.groupValues[3].ifEmpty { "g" }
            facts.add(NutritionFact(name = "Dietary Fiber", amount = "$amount $unit", contextIndicator = if ((amount.toDoubleOrNull() ?: 0.0) >= 3) "Good fiber source" else "Standard"))
        }

        return facts
    }

    private fun isHighSodium(facts: List<NutritionFact>, text: String): Boolean {
        val sodiumFact = facts.firstOrNull { it.name == "Sodium" }
        if (sodiumFact != null) {
            val num = sodiumFact.amount.substringBefore(" ").toDoubleOrNull() ?: 0.0
            if (num >= 800) return true
        }
        // Fallback check in text
        val match = Regex("(?i)sodium\\s*([0-9]{3,})\\s*mg").find(text)
        if (match != null) {
            val mg = match.groupValues[1].toIntOrNull() ?: 0
            return mg >= 800
        }
        return false
    }

    private fun isHighSugar(facts: List<NutritionFact>, text: String): Boolean {
        val sugarFact = facts.firstOrNull { it.name == "Sugars" }
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
    ): String {
        val baseVerdict = when {
            category == ProductCategory.SUPPLEMENT -> {
                "Standard dietary supplement formula providing active vitamins with common binding agents."
            }

            watchOutCount > 0 -> {
                "Review the personal flags above that match your specific dietary preferences or allergen warnings."
            }

            worthNotingCount > 2 -> {
                "Convenient formulated product with balanced taste, incorporating common additives and seasonings."
            }

            goodToKnowCount >= 2 && worthNotingCount <= 1 -> {
                "Wholesome formulation centered on straightforward whole ingredients and minimal processing."
            }

            else -> {
                "Standard packaged food formulation with familiar ingredients and balanced recipe composition."
            }
        }

        // Hard guardrail: supplement results append exactly the required disclaimer
        return if (category == ProductCategory.SUPPLEMENT) {
            "$baseVerdict Education only - this is not medical advice."
        } else {
            baseVerdict
        }
    }
}
