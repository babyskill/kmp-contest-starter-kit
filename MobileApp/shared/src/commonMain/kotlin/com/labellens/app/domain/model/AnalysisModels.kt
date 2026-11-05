package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProductCategory {
    FOOD,
    SUPPLEMENT,
    BEVERAGE,
    COSMETICS, // Category preserved in data model, explicitly out of MVP UI
}

@Serializable
enum class ProductType {
    SHAMPOO,
    CONDITIONER,
    BODY_WASH,
    CEREAL,
    SNACK,
    BISCUITS,
    NOODLES,
    BEVERAGE,
    SUPPLEMENT,
    GRANOLA_BAR,
    FOOD,
    PERSONAL_CARE,
}

@Serializable
enum class ScanSource {
    SAMPLE,
    BARCODE,
    PHOTO,
    MANUAL_TEXT,
}

@Serializable
enum class FlagSeverity {
    PERSONAL_ALLERGEN, // Allergen red #B3261E on #FDECEA
    PERSONAL_AVOID, // Amber #B7791F on #FBF3DF
    WORTH_NOTING, // Amber #B7791F on #FBF3DF
    GOOD_TO_KNOW, // Primary leaf green #16794C on #EDF2ED
}

@Serializable
data class FlaggedItem(
    val title: String,
    val detail: String,
    val severity: FlagSeverity,
    val relatedIngredient: String? = null,
    val verbatimSourceLine: String? = null,
    val isExceptionEligible: Boolean = false,
    val isAiGenerated: Boolean = false,
)

@Serializable
data class PortionInfo(
    val servingSize: String? = null,
    val servingsPerContainer: Double? = null,
    val hasMultipleServings: Boolean = false,
)

@Serializable
enum class PortionMode {
    SINGLE_SERVING,
    WHOLE_PACK,
}

@Serializable
enum class ClusterType {
    SWEETENERS,
    SODIUM_SOURCES,
}

@Serializable
data class ClusterRollUpItem(
    val rawName: String,
    val canonicalName: String,
    val category: String,
    val plainExplanation: String,
    val isSweetener: Boolean = false,
    val isSodiumSource: Boolean = false,
)

@Serializable
data class ClusterRollUp(
    val clusterType: ClusterType,
    val title: String,
    val count: Int,
    val items: List<ClusterRollUpItem> = emptyList(),
)

@Serializable
data class IngredientBreakdown(
    val rawName: String,
    val canonicalName: String,
    val category: String,
    val plainExplanation: String,
    val whyUsed: String,
    val doseContextNote: String,
    val avoidForTags: List<String> = emptyList(),
    val isKnown: Boolean = true,
    val matchedAliases: List<String> = emptyList(),
    val sourceNote: String = "Seed database & standard food codex",
    val isAfterCutLine: Boolean = false,
    val isCatchAll: Boolean = false,
    val catchAllNote: String? = null,
    val isAiGenerated: Boolean = false,
)

@Serializable
data class NutritionFact(
    val name: String,
    val amount: String,
    val perServingNote: String? = null,
    val contextIndicator: String? = null, // e.g., "High", "Moderate", "Low"
    val secondaryDetail: String? = null, // e.g. "6.0 tsp" or "50% DV • 2.9g salt eq"
)

@Serializable
data class LabelAnalysisResult(
    val id: String? = null,
    val productName: String,
    val category: ProductCategory,
    val scanSource: ScanSource,
    val verdict: String,
    val watchOutForYou: List<FlaggedItem> = emptyList(), // ONLY user's personal matches
    val worthNoting: List<FlaggedItem> = emptyList(), // General notable additives/sweeteners/etc.
    val goodToKnow: List<FlaggedItem> = emptyList(), // Wholesome ingredients, positive attributes
    val fullBreakdown: List<IngredientBreakdown> = emptyList(),
    val nutritionHighlights: List<NutritionFact> = emptyList(),
    val rawInputText: String,
    val uncertaintyNotes: List<String> = emptyList(),
    val barcode: String? = null,
    val quickGlance: String? = null, // Calm secondary indicator e.g. "Simple recipe • 4 key ingredients"
    val sourceAttribution: String? = null, // e.g. "Source: Open Food Facts"
    val portionInfo: PortionInfo = PortionInfo(),
    val sweetenerRollUp: ClusterRollUp? = null,
    val sodiumRollUp: ClusterRollUp? = null,
    val cutLineIndex: Int? = null,
    val cutLineLabel: String? = null,
    val haloNotes: List<FlaggedItem> = emptyList(),
    val fitHeader: FitHeaderInfo? = null,
    val productType: String? = null,
)
