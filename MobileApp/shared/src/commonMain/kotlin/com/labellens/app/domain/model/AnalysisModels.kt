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
)

@Serializable
data class NutritionFact(
    val name: String,
    val amount: String,
    val perServingNote: String? = null,
    val contextIndicator: String? = null, // e.g., "High", "Moderate", "Low"
)

@Serializable
data class LabelAnalysisResult(
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
)
