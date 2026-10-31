package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AllergenCategory
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.FitHeaderInfo
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.MarketPacks
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.PortionInfo
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.UserProfile

object ProfileFitEvaluator {

    // Thresholds as named constants per PRD Section 4 / Scope 3
    const val SODIUM_HIGH_THRESHOLD_MG: Double = 460.0 // 20% DV per serving (460 mg)
    const val ADDED_SUGARS_HIGH_THRESHOLD_G: Double = 10.0 // 20% DV per serving (10 g)

    // Non-nutritive sweeteners list (Sugar alcohols explicitly excluded)
    val NON_NUTRITIVE_SWEETENERS: List<String> = listOf(
        "sucralose",
        "aspartame",
        "acesulfame potassium",
        "acesulfame k",
        "ace-k",
        "saccharin",
        "steviol glycosides",
        "stevia",
        "stevia leaf extract",
        "monk fruit extract",
        "monk fruit",
        "luo han guo",
        "neotame",
        "advantame",
    )

    // Caffeine sources
    val CAFFEINE_SOURCES: List<String> = listOf(
        "caffeine",
        "guarana",
        "guarana extract",
        "yerba mate",
        "yerba mate extract",
        "green tea extract",
    )

    // Artificial colours (Caramel colour is NOT artificial for this flag)
    val ARTIFICIAL_COLOURS: List<String> = listOf(
        "red 40",
        "allura red",
        "allura red ac",
        "fd&c red no. 40",
        "fd&c red 40",
        "e129",
        "yellow 5",
        "tartrazine",
        "fd&c yellow no. 5",
        "fd&c yellow 5",
        "e102",
        "yellow 6",
        "sunset yellow",
        "sunset yellow fcf",
        "fd&c yellow no. 6",
        "fd&c yellow 6",
        "e110",
        "blue 1",
        "brilliant blue",
        "brilliant blue fcf",
        "fd&c blue no. 1",
        "fd&c blue 1",
        "e133",
        "blue 2",
        "indigotine",
        "indigo carmine",
        "fd&c blue no. 2",
        "fd&c blue 2",
        "e132",
        "red 3",
        "erythrosine",
        "fd&c red no. 3",
        "fd&c red 3",
        "e127",
        "green 3",
        "fast green",
        "fast green fcf",
        "fd&c green no. 3",
        "fd&c green 3",
        "e143",
    )

    // Explicit false-positive whitelist: these exact ingredients MUST NOT trigger the corresponding allergen
    val FALSE_POSITIVE_WHITELIST: Map<AllergenCategory, List<String>> = mapOf(
        AllergenCategory.MILK to listOf(
            "cocoa butter",
            "lactic acid",
            "cream of tartar",
            "coconut milk",
            "coconut cream",
            "shea butter",
            "butternut squash",
            "bơ ca cao",
            "axit lactic",
            "kem tartar",
        ),
        AllergenCategory.TREE_NUTS to listOf(
            "nutmeg",
            "butternut",
            "butternut squash",
            "nhục đậu khấu",
        ),
        AllergenCategory.EGG to listOf(
            "eggplant",
            "cà tím",
        ),
        AllergenCategory.WHEAT_GLUTEN to listOf(
            "buckwheat",
            "kiều mạch",
        ),
    )

    // Allergen category member terms (English and Vietnamese overlay)
    val CATEGORY_MEMBERS: Map<AllergenCategory, List<String>> = mapOf(
        AllergenCategory.MILK to listOf(
            "milk", "whey", "casein", "caseinate", "sodium caseinate", "calcium caseinate",
            "lactose", "butter", "buttermilk", "butter oil", "butter fat", "ghee",
            "cream", "heavy cream", "sour cream", "cheese", "cheddar", "mozzarella", "parmesan",
            "milk powder", "dry milk", "skim milk", "whole milk", "nonfat milk", "evaporated milk",
            "condensed milk", "milk protein", "milk protein isolate", "milk protein concentrate",
            "lactalbumin", "lactoglobulin", "curds", "yogurt", "kefir",
            "sữa", "váng sữa", "kem sữa", "bơ sữa", "phô mai", "đạm sữa",
        ),
        AllergenCategory.EGG to listOf(
            "egg", "eggs", "egg white", "egg whites", "egg yolk", "egg yolks", "whole egg",
            "dried egg", "egg powder", "albumin", "ovalbumin", "lysozyme", "mayonnaise",
            "meringue", "globulin", "ovomucin", "ovomucoid", "vitellin",
            "trứng", "lòng trắng trứng", "lòng đỏ trứng",
        ),
        AllergenCategory.FISH to listOf(
            "fish", "salmon", "tuna", "cod", "anchovy", "anchovies", "halibut", "sardine",
            "sardines", "trout", "mackerel", "tilapia", "haddock", "pollock", "bass",
            "flounder", "snapper", "swordfish", "fish sauce", "fish oil", "fish gelatin", "isinglass",
            "cá", "nước mắm", "dầu cá",
        ),
        AllergenCategory.CRUSTACEANS to listOf(
            "crustacean", "crustaceans", "shrimp", "prawn", "prawns", "crab", "lobster",
            "crawfish", "crayfish", "krill", "shrimp paste", "crab extract",
            "tôm", "cua", "giáp xác", "mắm tôm",
        ),
        AllergenCategory.TREE_NUTS to listOf(
            "tree nut", "tree nuts", "almond", "almonds", "walnut", "walnuts", "cashew", "cashews",
            "pecan", "pecans", "pistachio", "pistachios", "macadamia", "macadamias",
            "brazil nut", "brazil nuts", "hazelnut", "hazelnuts", "chestnut", "chestnuts",
            "pine nut", "pine nuts", "praline", "marzipan",
            "hạt hạnh nhân", "hạt óc chó", "hạt điều", "hạt dẻ", "hạt dẻ cười", "hạt macca",
        ),
        AllergenCategory.PEANUT to listOf(
            "peanut", "peanuts", "peanut butter", "peanut oil", "peanut flour",
            "groundnut", "groundnuts", "arachis oil", "beer nuts",
            "đậu phộng", "lạc", "bơ đậu phộng", "dầu đậu phộng",
        ),
        AllergenCategory.WHEAT_GLUTEN to listOf(
            "wheat", "gluten", "wheat flour", "wheat starch", "wheat bran", "wheat germ",
            "wheat grass", "spelt", "kamut", "farro", "durum", "semolina", "bulgur",
            "couscous", "emmer", "einkorn", "barley", "malt", "barley malt", "rye",
            "triticale", "vital wheat gluten",
            "lúa mì", "bột mì", "lúa mạch",
        ),
        AllergenCategory.SOY to listOf(
            "soy", "soya", "soybean", "soybeans", "soy lecithin", "soy protein",
            "soy protein isolate", "soy sauce", "tofu", "edamame", "tempeh", "miso",
            "tamari", "textured vegetable protein", "tvp", "soy flour",
            "đậu nành", "đậu tương", "nước tương", "tàu hũ", "đậu phụ",
        ),
        AllergenCategory.SESAME to listOf(
            "sesame", "sesame seed", "sesame seeds", "sesame oil", "tahini", "sesamol",
            "sesamum indicum", "mè", "vừng", "dầu mè", "dầu vừng",
        ),
        AllergenCategory.CELERY to listOf(
            "celery", "celery seed", "celery seeds", "celery salt", "celery rib",
            "celery stalk", "celery juice", "celeriac", "cần tây",
        ),
        AllergenCategory.MUSTARD to listOf(
            "mustard", "mustard seed", "mustard seeds", "mustard powder", "mustard flour",
            "mustard oil", "dijon mustard", "mù tạt", "hạt mù tạt",
        ),
        AllergenCategory.SULPHITES to listOf(
            "sulphite", "sulphites", "sulfite", "sulfites", "sulphur dioxide", "sulfur dioxide",
            "sodium sulfite", "sodium bisulfite", "sodium metabisulfite", "potassium bisulfite",
            "potassium metabisulfite", "e220", "e221", "e222", "e223", "e224", "e226", "e227", "e228",
            "lưu huỳnh điôxít", "chất bảo quản sulfite",
        ),
        AllergenCategory.LUPIN to listOf(
            "lupin",
            "lupine",
            "lupin flour",
            "lupin seed",
            "lupin seeds",
            "lupin bean",
            "lupin beans",
            "đậu lupin",
        ),
        AllergenCategory.MOLLUSCS to listOf(
            "mollusc", "molluscs", "mollusk", "mollusks", "clam", "clams", "mussel", "mussels",
            "oyster", "oysters", "scallop", "scallops", "squid", "calamari", "octopus",
            "snail", "snails", "escargot", "abalone", "cuttlefish", "oyster sauce",
            "thân mềm", "nghêu", "sò", "ốc", "hến", "mực", "bạch tuộc", "hàu", "dầu hào",
        ),
    )

    // Precompiled regex boundary patterns for categories
    private val categoryPatterns: Map<AllergenCategory, List<Pair<String, Regex>>> by lazy {
        CATEGORY_MEMBERS.mapValues { (_, terms) ->
            terms.sortedByDescending { it.length }.map { term ->
                term to Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
            }
        }
    }

    private val sweetenerPatterns: List<Pair<String, Regex>> by lazy {
        NON_NUTRITIVE_SWEETENERS.sortedByDescending { it.length }.map { term ->
            term to Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    private val caffeinePatterns: List<Pair<String, Regex>> by lazy {
        CAFFEINE_SOURCES.sortedByDescending { it.length }.map { term ->
            term to Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    private val colorPatterns: List<Pair<String, Regex>> by lazy {
        ARTIFICIAL_COLOURS.sortedByDescending { it.length }.map { term ->
            term to Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    /**
     * Checks if a token contains a keyword matching Unicode-safe word boundaries.
     * Prevents false positives while supporting non-ASCII scripts (Vietnamese accents, etc.).
     */
    fun matchesWord(text: String, keyword: String): Boolean {
        val clean = text.lowercase().trim()
        val target = keyword.lowercase().trim()
        if (clean.isEmpty() || target.isEmpty()) return false
        if (clean == target) return true
        val regex = Regex("(?i)(?:^|[^\\p{L}\\p{N}])${Regex.escape(target)}(?:$|[^\\p{L}\\p{N}])")
        return regex.containsMatchIn(clean)
    }

    /**
     * Checks if a token matches an allergen category using token/phrase boundaries
     * while respecting the false-positive whitelist.
     */
    fun matchAllergenCategory(token: String, category: AllergenCategory): String? {
        val clean = token.lowercase().trim()
        if (clean.isEmpty()) return null

        // Check false-positive whitelist first
        val whitelist = FALSE_POSITIVE_WHITELIST[category]
        if (whitelist != null) {
            for (fp in whitelist) {
                if (matchesWord(clean, fp)) {
                    return null
                }
            }
        }

        val terms = CATEGORY_MEMBERS[category] ?: return null
        for (term in terms) {
            if (matchesWord(clean, term)) {
                return term
            }
        }
        return null
    }

    /**
     * Checks if a token matches a custom free-text allergen using token/phrase boundaries.
     */
    fun matchCustomAllergen(token: String, customAllergen: String): Boolean = matchesWord(token, customAllergen)

    /**
     * Precautionary statement detection (Section 2 - CANNOT_RULE_OUT condition a).
     */
    fun extractPrecautionaryStatements(text: String): List<String> {
        val statements = mutableListOf<String>()
        val regex = Regex(
            "(?i)(?:may contain traces of|may contain|made in a facility that also processes|manufactured in a facility that also processes|facility also processes|có thể chứa|sản xuất tại cơ sở có xử lý|sản xuất trên cùng dây chuyền với)[^.\\n]*",
        )
        regex.findAll(text).forEach { match ->
            statements.add(match.value.trim())
        }
        return statements
    }

    /**
     * Conservative mapping table for catch-all terms by market (Section 2 - CANNOT_RULE_OUT condition b).
     * Documents which allergen derivatives can legally be hidden inside non-specific terms.
     */
    fun allergensHiddenByCatchAll(term: String, market: Market): Set<AllergenCategory> {
        val clean = term.lowercase().trim()
        val isFlavor = clean.contains("flavor") || clean.contains("flavour") || clean.contains("hương liệu")
        val isSpice = clean.contains("spice") || clean.contains("spices") || clean.contains("gia vị")
        val isProprietary = clean.contains("proprietary") || clean.contains("blend") || clean.contains("hỗn hợp độc quyền")

        if (isProprietary) {
            return AllergenCategory.entries.toSet()
        }

        return when (market) {
            Market.US -> when {
                isFlavor -> setOf(
                    AllergenCategory.MILK,
                    AllergenCategory.EGG,
                    AllergenCategory.SOY,
                    AllergenCategory.PEANUT,
                    AllergenCategory.TREE_NUTS,
                )

                isSpice -> setOf(
                    AllergenCategory.SESAME,
                    AllergenCategory.SOY,
                    AllergenCategory.MUSTARD,
                    AllergenCategory.CELERY,
                )

                else -> emptySet()
            }

            Market.EU_UK -> when {
                isFlavor -> setOf(
                    AllergenCategory.CELERY,
                    AllergenCategory.MUSTARD,
                    AllergenCategory.SULPHITES,
                    AllergenCategory.MILK,
                    AllergenCategory.SOY,
                )

                isSpice -> setOf(
                    AllergenCategory.CELERY,
                    AllergenCategory.MUSTARD,
                    AllergenCategory.SESAME,
                )

                else -> emptySet()
            }

            Market.AU_NZ -> when {
                isFlavor -> setOf(
                    AllergenCategory.MILK,
                    AllergenCategory.SOY,
                    AllergenCategory.SULPHITES,
                    AllergenCategory.SESAME,
                )

                isSpice -> setOf(
                    AllergenCategory.SESAME,
                    AllergenCategory.SOY,
                )

                else -> emptySet()
            }

            Market.JP, Market.KR -> emptySet()
        }
    }

    /**
     * Never silent-pass heuristics (Section 4):
     * Cut-off is detected if:
     * 1. Ingredient text ends with a comma (e.g. "...,")
     * 2. Ingredient text ends with "and" / "và"
     * 3. Ingredient text ends with "or" / "hoặc"
     * 4. Ingredient text ends with open parenthesis '(' or '['
     * 5. No ingredients section was captured at all
     */
    fun isCutOffDetected(rawLabelText: String, ingredientTokens: List<String>): Boolean {
        val trimmed = rawLabelText.trim()
        if (trimmed.isEmpty() || ingredientTokens.isEmpty()) {
            return true
        }

        // Check if no ingredients keyword or token could be isolated
        val lower = trimmed.lowercase()
        val hasIngredientsHeader = lower.contains("ingredients") ||
            lower.contains("thành phần") ||
            lower.contains("원재료") ||
            lower.contains("原材料") ||
            lower.contains("配料")

        if (!hasIngredientsHeader) {
            return true
        }

        val ingredientSection = if (hasIngredientsHeader) {
            when {
                lower.contains("ingredients:") -> trimmed.substring(lower.indexOf("ingredients:") + 12)
                lower.contains("ingredients") -> trimmed.substring(lower.indexOf("ingredients") + 11)
                lower.contains("thành phần:") -> trimmed.substring(lower.indexOf("thành phần:") + 11)
                lower.contains("thành phần") -> trimmed.substring(lower.indexOf("thành phần") + 10)
                else -> trimmed
            }
                .substringBefore("Nutrition Facts")
                .substringBefore("Thông tin dinh dưỡng")
                .trim()
        } else {
            trimmed
        }

        val cleanSection = ingredientSection.trimEnd()
        if (cleanSection.isEmpty()) return true

        // Heuristic 1: ends with comma
        if (cleanSection.endsWith(",")) return true

        // Heuristic 2: ends with "and" / "và"
        if (Regex("(?i)\\b(?:and|và)\\s*$").containsMatchIn(cleanSection)) return true

        // Heuristic 3: ends with "or" / "hoặc"
        if (Regex("(?i)\\b(?:or|hoặc)\\s*$").containsMatchIn(cleanSection)) return true

        // Heuristic 4: ends with open parenthesis '(' or '[' (or unclosed)
        if (cleanSection.endsWith("(") || cleanSection.endsWith("[")) return true
        val openParenCount = cleanSection.count { it == '(' }
        val closeParenCount = cleanSection.count { it == ')' }
        if (openParenCount > closeParenCount && cleanSection.takeLast(10).contains("(")) return true

        return false
    }

    data class EvaluationInput(
        val rawLabelText: String,
        val ingredientTokens: List<String>,
        val nutritionFacts: List<NutritionFact>,
        val portionInfo: PortionInfo,
        val userProfile: UserProfile,
        val language: AppLanguage,
    )

    data class FlagEvaluation(
        val flag: AttentionFlag,
        val exceedsPerServing: Boolean,
        val exceedsWholePack: Boolean,
        val matchedTerm: String,
        val verbatimSourceLine: String?,
    )

    /**
     * Computes the deterministic Six-State Result Header based on:
     * priority: CONTAINS_YOURS > CANNOT_RULE_OUT > NOT_ENOUGH_INFO > DEPENDS_ON_PORTION > WORTH_NOTING > NO_MATCHES.
     */
    fun evaluate(input: EvaluationInput): FitHeaderInfo {
        val userAllergens = input.userProfile.allergies.map { it.toCategory() }.toSet()
        val customAllergens = input.userProfile.customAllergens.filter { it.isNotBlank() }
        val exceptions = input.userProfile.ingredientExceptions.map { it.lowercase().trim() }.toSet()
        val activePackCategories = MarketPacks.activeCategories(
            market = input.userProfile.selectedMarket,
            importedFoodMode = input.userProfile.importedFoodMode,
        )

        // Effective categories to match against: intersection of user declared allergens and active pack categories
        val declaredCategories = userAllergens.filter { activePackCategories.contains(it) }.toSet()

        // 1. Check CONTAINS_YOURS (Declared allergen directly matches)
        // Hard rule: user exceptions NEVER suppress a CONTAINS_YOURS direct allergen match
        val directAllergenMatches = mutableListOf<String>()
        val directMatchLines = mutableListOf<String>()

        for (token in input.ingredientTokens) {
            val lowerToken = token.lowercase().trim()
            val isPrecautionaryToken = lowerToken.startsWith("may contain") ||
                lowerToken.startsWith("made in a facility") ||
                lowerToken.startsWith("manufactured in a facility") ||
                lowerToken.startsWith("traces of") ||
                lowerToken.startsWith("có thể chứa") ||
                lowerToken.startsWith("sản xuất tại")
            if (isPrecautionaryToken) continue

            for (category in declaredCategories) {
                val matched = matchAllergenCategory(token, category)
                if (matched != null) {
                    directAllergenMatches.add("${category.localizedName(input.language)} ($token)")
                    directMatchLines.add(token)
                }
            }
            for (custom in customAllergens) {
                if (matchCustomAllergen(token, custom)) {
                    directAllergenMatches.add("$custom ($token)")
                    directMatchLines.add(token)
                }
            }
        }

        if (directAllergenMatches.isNotEmpty()) {
            return FitHeaderInfo(
                state = ProfileFitState.CONTAINS_YOURS,
                title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.CONTAINS_YOURS),
                subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.CONTAINS_YOURS),
                matchedTerms = directAllergenMatches.distinct(),
                verbatimSourceLine = directMatchLines.distinct().joinToString(", "),
                reasonNote = AnalysisStrings.fitContainsYoursDetail(input.language, directAllergenMatches.distinct().joinToString(", ")),
                isCutOff = false,
            )
        }

        // 2. Check CANNOT_RULE_OUT
        // Condition (a): precautionary statement names user allergen
        val precautionaryMatches = mutableListOf<String>()
        val precautionaryStatements = extractPrecautionaryStatements(input.rawLabelText)
        for (stmt in precautionaryStatements) {
            for (category in declaredCategories) {
                val matched = matchAllergenCategory(stmt, category)
                if (matched != null) {
                    precautionaryMatches.add("${category.localizedName(input.language)} ($stmt)")
                }
            }
            for (custom in customAllergens) {
                if (matchCustomAllergen(stmt, custom)) {
                    precautionaryMatches.add("$custom ($stmt)")
                }
            }
        }

        // Condition (b): catch-all term legally hides user allergen in selected market
        val catchAllMatches = mutableListOf<String>()
        if (declaredCategories.isNotEmpty()) {
            for (token in input.ingredientTokens) {
                val hiddenInMarket = allergensHiddenByCatchAll(token, input.userProfile.selectedMarket)
                val matchingDeclared = hiddenInMarket.intersect(declaredCategories)
                if (matchingDeclared.isNotEmpty()) {
                    val names = matchingDeclared.joinToString(", ") { it.localizedName(input.language) }
                    catchAllMatches.add("$token ($names)")
                }
            }
        }

        if (precautionaryMatches.isNotEmpty() || catchAllMatches.isNotEmpty()) {
            val allReasons = (precautionaryMatches + catchAllMatches).distinct()
            return FitHeaderInfo(
                state = ProfileFitState.CANNOT_RULE_OUT,
                title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.CANNOT_RULE_OUT),
                subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.CANNOT_RULE_OUT),
                matchedTerms = allReasons,
                verbatimSourceLine = (precautionaryStatements + catchAllMatches.map { it.substringBefore(" (") }).distinct().joinToString("; "),
                reasonNote = AnalysisStrings.fitCannotRuleOutDetail(input.language, allReasons.joinToString(", ")),
                isCutOff = false,
            )
        }

        // 3. Check NOT_ENOUGH_INFO (Cut-off detection)
        val cutOff = isCutOffDetected(input.rawLabelText, input.ingredientTokens)
        if (cutOff) {
            return FitHeaderInfo(
                state = ProfileFitState.NOT_ENOUGH_INFO,
                title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.NOT_ENOUGH_INFO),
                subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.NOT_ENOUGH_INFO),
                matchedTerms = emptyList(),
                verbatimSourceLine = null,
                reasonNote = AnalysisStrings.fitNotEnoughInfoDetail(input.language),
                isCutOff = true,
            )
        }

        // 4 & 5. Check Attention Flags (WORTH_NOTING vs DEPENDS_ON_PORTION)
        // Enabled attention flags only
        val enabledFlags = input.userProfile.enabledAttentionFlags
        val flagResults = evaluateAttentionFlags(
            nutritionFacts = input.nutritionFacts,
            portionInfo = input.portionInfo,
            tokens = input.ingredientTokens,
            enabledFlags = enabledFlags,
            exceptions = exceptions,
            language = input.language,
        )

        val perServingTriggers = flagResults.filter { it.exceedsPerServing }
        val wholePackOnlyTriggers = flagResults.filter { !it.exceedsPerServing && it.exceedsWholePack }

        // Priority 4: DEPENDS_ON_PORTION (if an attention flag exceeds threshold ONLY at whole pack amounts)
        // Priority 5: WORTH_NOTING (if an attention flag exceeds per-serving threshold)
        // Wait, note priority matrix: DEPENDS_ON_PORTION > WORTH_NOTING
        if (wholePackOnlyTriggers.isNotEmpty() && perServingTriggers.isEmpty()) {
            val terms = wholePackOnlyTriggers.map { it.matchedTerm }
            val lines = wholePackOnlyTriggers.mapNotNull { it.verbatimSourceLine }
            return FitHeaderInfo(
                state = ProfileFitState.DEPENDS_ON_PORTION,
                title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.DEPENDS_ON_PORTION),
                subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.DEPENDS_ON_PORTION),
                matchedTerms = terms,
                verbatimSourceLine = lines.joinToString(", "),
                reasonNote = AnalysisStrings.fitDependsOnPortionDetail(input.language, terms.joinToString(", ")),
                isCutOff = false,
            )
        }

        if (perServingTriggers.isNotEmpty()) {
            val terms = perServingTriggers.map { it.matchedTerm }
            val lines = perServingTriggers.mapNotNull { it.verbatimSourceLine }
            return FitHeaderInfo(
                state = ProfileFitState.WORTH_NOTING,
                title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.WORTH_NOTING),
                subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.WORTH_NOTING),
                matchedTerms = terms,
                verbatimSourceLine = lines.joinToString(", "),
                reasonNote = AnalysisStrings.fitWorthNotingDetail(input.language, terms.joinToString(", ")),
                isCutOff = false,
            )
        }

        // 6. NO_MATCHES: ingredient list fully captured and no state above applies
        return FitHeaderInfo(
            state = ProfileFitState.NO_MATCHES,
            title = AnalysisStrings.fitStateTitle(input.language, ProfileFitState.NO_MATCHES),
            subtitle = AnalysisStrings.fitStateSubtitle(input.language, ProfileFitState.NO_MATCHES),
            matchedTerms = emptyList(),
            verbatimSourceLine = null,
            reasonNote = AnalysisStrings.fitNoMatchesDetail(input.language),
            isCutOff = false,
        )
    }

    private fun evaluateAttentionFlags(
        nutritionFacts: List<NutritionFact>,
        portionInfo: PortionInfo,
        tokens: List<String>,
        enabledFlags: Set<AttentionFlag>,
        exceptions: Set<String>,
        language: AppLanguage,
    ): List<FlagEvaluation> {
        val results = mutableListOf<FlagEvaluation>()
        val servings = portionInfo.servingsPerContainer ?: 1.0

        // 1. Sodium Flag
        if (enabledFlags.contains(AttentionFlag.SODIUM)) {
            val sodiumFact = nutritionFacts.firstOrNull { it.name.contains("Sodium", ignoreCase = true) || it.name.contains("Natri", ignoreCase = true) }
            val sodiumMg = sodiumFact?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            val sodiumWholePackMg = sodiumMg * servings

            // Check if sodium is exempted
            val isExempt = exceptions.contains("sodium") || exceptions.contains("natri") || exceptions.contains("salt") || exceptions.contains("muối")
            if (!isExempt) {
                val exceedsPerServing = sodiumMg >= SODIUM_HIGH_THRESHOLD_MG
                val exceedsWholePack = sodiumWholePackMg >= SODIUM_HIGH_THRESHOLD_MG
                if (exceedsPerServing || exceedsWholePack) {
                    val label = if (language == AppLanguage.VIETNAMESE) "Natri (${sodiumMg.toInt()}mg)" else "Sodium (${sodiumMg.toInt()}mg)"
                    results.add(
                        FlagEvaluation(
                            flag = AttentionFlag.SODIUM,
                            exceedsPerServing = exceedsPerServing,
                            exceedsWholePack = exceedsWholePack,
                            matchedTerm = label,
                            verbatimSourceLine = sodiumFact?.let { "${it.name}: ${it.amount}" },
                        ),
                    )
                }
            }
        }

        // 2. Added Sugars Flag
        if (enabledFlags.contains(AttentionFlag.ADDED_SUGARS)) {
            val addedSugarFact = nutritionFacts.firstOrNull {
                it.name.contains("Added Sugar", ignoreCase = true) || it.name.contains("Đường bổ sung", ignoreCase = true)
            }
            val addedSugarsG = addedSugarFact?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            val addedSugarsWholePackG = addedSugarsG * servings

            val isExempt = exceptions.contains("added sugar") || exceptions.contains("added sugars") || exceptions.contains("đường bổ sung")
            if (!isExempt) {
                val exceedsPerServing = addedSugarsG >= ADDED_SUGARS_HIGH_THRESHOLD_G
                val exceedsWholePack = addedSugarsWholePackG >= ADDED_SUGARS_HIGH_THRESHOLD_G
                if (exceedsPerServing || exceedsWholePack) {
                    val label = if (language == AppLanguage.VIETNAMESE) "Đường bổ sung (${addedSugarsG}g)" else "Added Sugars (${addedSugarsG}g)"
                    results.add(
                        FlagEvaluation(
                            flag = AttentionFlag.ADDED_SUGARS,
                            exceedsPerServing = exceedsPerServing,
                            exceedsWholePack = exceedsWholePack,
                            matchedTerm = label,
                            verbatimSourceLine = addedSugarFact?.let { "${it.name}: ${it.amount}" },
                        ),
                    )
                }
            }
        }

        // 3. Non-Nutritive Sweeteners Flag
        if (enabledFlags.contains(AttentionFlag.NON_NUTRITIVE_SWEETENERS)) {
            for (token in tokens) {
                val clean = token.lowercase().trim()
                for ((sweetener, regex) in sweetenerPatterns) {
                    if (regex.containsMatchIn(clean)) {
                        // Check if exempted
                        val isExempt = exceptions.contains(sweetener) || exceptions.contains(clean)
                        if (!isExempt) {
                            results.add(
                                FlagEvaluation(
                                    flag = AttentionFlag.NON_NUTRITIVE_SWEETENERS,
                                    exceedsPerServing = true,
                                    exceedsWholePack = true,
                                    matchedTerm = sweetener,
                                    verbatimSourceLine = token,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // 4. Caffeine Flag
        if (enabledFlags.contains(AttentionFlag.CAFFEINE)) {
            for (token in tokens) {
                val clean = token.lowercase().trim()
                for ((caffeineSource, regex) in caffeinePatterns) {
                    if (regex.containsMatchIn(clean)) {
                        val isExempt = exceptions.contains(caffeineSource) || exceptions.contains(clean)
                        if (!isExempt) {
                            results.add(
                                FlagEvaluation(
                                    flag = AttentionFlag.CAFFEINE,
                                    exceedsPerServing = true,
                                    exceedsWholePack = true,
                                    matchedTerm = caffeineSource,
                                    verbatimSourceLine = token,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // 5. Artificial Colours Flag
        if (enabledFlags.contains(AttentionFlag.ARTIFICIAL_COLOURS)) {
            for (token in tokens) {
                val clean = token.lowercase().trim()
                for ((color, regex) in colorPatterns) {
                    if (regex.containsMatchIn(clean)) {
                        val isExempt = exceptions.contains(color) || exceptions.contains(clean)
                        if (!isExempt) {
                            results.add(
                                FlagEvaluation(
                                    flag = AttentionFlag.ARTIFICIAL_COLOURS,
                                    exceedsPerServing = true,
                                    exceedsWholePack = true,
                                    matchedTerm = color,
                                    verbatimSourceLine = token,
                                ),
                            )
                        }
                    }
                }
            }
        }

        return results
    }
}
