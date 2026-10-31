package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.KnowledgeIngredient

object KnowledgeClusters {

    // Cluster 1: Sweeteners (at least 65 genuine US label names; 73 names included)
    val SWEETENER_NAMES: List<String> = listOf(
        "sugar",
        "cane sugar",
        "brown sugar",
        "raw sugar",
        "turbinado sugar",
        "demerara sugar",
        "coconut sugar",
        "date sugar",
        "beet sugar",
        "powdered sugar",
        "confectioners sugar",
        "invert sugar",
        "cane juice",
        "evaporated cane juice",
        "cane syrup",
        "brown rice syrup",
        "corn syrup",
        "high fructose corn syrup",
        "corn syrup solids",
        "glucose syrup",
        "tapioca syrup",
        "rice syrup",
        "barley malt syrup",
        "malt syrup",
        "sorghum syrup",
        "golden syrup",
        "maple syrup",
        "agave nectar",
        "agave syrup",
        "honey",
        "molasses",
        "blackstrap molasses",
        "treacle",
        "dextrose",
        "glucose",
        "fructose",
        "sucrose",
        "maltose",
        "lactose",
        "galactose",
        "maltodextrin",
        "dextrin",
        "fruit juice concentrate",
        "apple juice concentrate",
        "grape juice concentrate",
        "pear juice concentrate",
        "fruit puree concentrate",
        "caramel",
        "ethyl maltol",
        "barley malt",
        "malt extract",
        "corn sweetener",
        "refiners syrup",
        "sorghum",
        "saccharose",
        "isomaltulose",
        "coconut nectar",
        "date syrup",
        "yacon syrup",
        "monk fruit extract",
        "stevia leaf extract",
        // Additional genuine US label names to reach 73:
        "coconut palm sugar",
        "crystalline fructose",
        "evaporated cane syrup",
        "maple sugar",
        "oat syrup",
        "tapioca sweetener",
        "brown sugar syrup",
        "rice malt syrup",
        "fruit juice blend concentrate",
        "anhydrous dextrose",
        "dried cane syrup",
        "organic cane sugar",
    )

    // Cluster 2: Sodium Sources (at least 25 genuine US label names; 28 names included)
    val SODIUM_SOURCE_NAMES: List<String> = listOf(
        "salt",
        "sea salt",
        "sodium chloride",
        "table salt",
        "kosher salt",
        "monosodium glutamate",
        "msg",
        "sodium bicarbonate",
        "baking soda",
        "sodium benzoate",
        "sodium nitrite",
        "sodium nitrate",
        "disodium phosphate",
        "sodium phosphate",
        "trisodium phosphate",
        "sodium citrate",
        "sodium carbonate",
        "sodium erythorbate",
        "sodium ascorbate",
        "disodium inosinate",
        "disodium guanylate",
        "sodium caseinate",
        "sodium alginate",
        "sodium propionate",
        "sodium metabisulfite",
        "monosodium phosphate",
        "sodium acid pyrophosphate",
        "brine",
    )

    // Isolated Fibres for Halo Pattern A
    val ISOLATED_FIBERS: List<String> = listOf(
        "inulin",
        "chicory root fiber",
        "chicory root fibre",
        "polydextrose",
        "soluble corn fiber",
        "resistant maltodextrin",
        "chicory root extract",
    )

    // Catch-All Terms for Section 3
    val CATCH_ALL_TERMS: List<String> = listOf(
        "natural flavor",
        "natural flavors",
        "spices",
        "spice",
        "parfum",
        "fragrance",
        "proprietary blend",
    )

    // Common Fruit Identifiers for Halo Pattern B
    val FRUIT_NAMES: List<String> = listOf(
        "apple",
        "berry",
        "strawberries",
        "strawberry",
        "blueberries",
        "blueberry",
        "raspberries",
        "raspberry",
        "cherries",
        "cherry",
        "grape",
        "grapes",
        "peach",
        "peaches",
        "mango",
        "orange",
        "lemon",
        "lime",
        "pear",
        "pears",
        "pomegranate",
        "cranberries",
        "cranberry",
        "passionfruit",
        "fruit",
    )

    // Marquee ingredients to check for 2% cut-line / salt position
    val MARQUEE_INGREDIENTS: List<String> = listOf(
        "honey",
        "olive oil",
        "extra virgin olive oil",
        "avocado",
        "avocado oil",
        "truffle",
        "almond",
        "chia seed",
        "acai",
    )

    // Precompiled regexes sorted by descending length to match longest/most specific phrase first
    private val sweetenerPatterns: List<Pair<String, Regex>> by lazy {
        SWEETENER_NAMES.sortedByDescending { it.length }.map { name ->
            name to Regex("\\b${Regex.escape(name)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    private val sodiumPatterns: List<Pair<String, Regex>> by lazy {
        SODIUM_SOURCE_NAMES.sortedByDescending { it.length }.map { name ->
            name to Regex("\\b${Regex.escape(name)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    private val isolatedFiberPatterns: List<Pair<String, Regex>> by lazy {
        ISOLATED_FIBERS.sortedByDescending { it.length }.map { name ->
            name to Regex("\\b${Regex.escape(name)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    private val catchAllPatterns: List<Pair<String, Regex>> by lazy {
        CATCH_ALL_TERMS.sortedByDescending { it.length }.map { name ->
            name to Regex("\\b${Regex.escape(name)}\\b", RegexOption.IGNORE_CASE)
        }
    }

    /**
     * Checks if a single ingredient token matches a sweetener cluster name using phrase boundary.
     * Prevents false positives like 'nutmeg' or 'lactic acid'.
     */
    fun matchSweetener(token: String): String? {
        val clean = token.lowercase().trim()
        if (clean.isEmpty()) return null
        for ((name, pattern) in sweetenerPatterns) {
            if (pattern.containsMatchIn(clean)) {
                return name
            }
        }
        return null
    }

    /**
     * Checks if a single ingredient token matches a sodium source cluster name using phrase boundary.
     * Prevents false positives like 'unsalted butter'.
     */
    fun matchSodiumSource(token: String): String? {
        val clean = token.lowercase().trim()
        if (clean.isEmpty()) return null
        for ((name, pattern) in sodiumPatterns) {
            if (pattern.containsMatchIn(clean)) {
                return name
            }
        }
        return null
    }

    /**
     * Checks if a single ingredient token matches an isolated fiber.
     */
    fun matchIsolatedFiber(token: String): String? {
        val clean = token.lowercase().trim()
        if (clean.isEmpty()) return null
        for ((name, pattern) in isolatedFiberPatterns) {
            if (pattern.containsMatchIn(clean)) {
                return name
            }
        }
        return null
    }

    /**
     * Checks if a single ingredient token is a catch-all term (natural flavors, spices, etc.).
     */
    fun matchCatchAll(token: String): String? {
        val clean = token.lowercase().trim()
        if (clean.isEmpty()) return null
        for ((name, pattern) in catchAllPatterns) {
            if (pattern.containsMatchIn(clean)) {
                return name
            }
        }
        return null
    }

    /**
     * Resolves any cluster name or alias to its canonical KnowledgeIngredient.
     */
    fun resolveCanonical(clusterName: String): KnowledgeIngredient? = KnowledgeBase.findIngredientByAnyName(clusterName)
}
