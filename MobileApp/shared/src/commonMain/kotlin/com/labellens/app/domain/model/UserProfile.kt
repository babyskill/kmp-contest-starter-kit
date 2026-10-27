package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Allergen(val displayName: String) {
    PEANUT("Peanut"),
    MILK("Milk / Dairy"),
    EGG("Egg"),
    SOY("Soy"),
    WHEAT_GLUTEN("Wheat / Gluten"),
    FISH("Fish"),
    SHELLFISH("Shellfish / Crustaceans"),
    SESAME("Sesame"),
    TREE_NUTS("Tree Nuts"),
}

@Serializable
enum class AvoidPreference(val displayName: String) {
    ADDED_SUGAR("Added Sugar"),
    PALM_OIL("Palm Oil"),
    ARTIFICIAL_SWEETENERS("Artificial Sweeteners"),
    ARTIFICIAL_COLOURS("Artificial Colours"),
    MSG("MSG (Monosodium Glutamate)"),
    HIGH_SODIUM("High Sodium"),
}

@Serializable
enum class DietaryGoal(val displayName: String) {
    LOW_SUGAR("Low Sugar"),
    LOW_SODIUM("Low Sodium"),
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    HALAL_CONSCIOUS("Halal Conscious"),
    PREGNANCY_SAFE("Pregnancy-Safe Caution"),
    DIABETES_CAUTIOUS("Diabetes-Cautious"),
}

@Serializable
data class UserProfile(
    val allergies: Set<Allergen> = emptySet(),
    val avoidList: Set<AvoidPreference> = emptySet(),
    val goals: Set<DietaryGoal> = emptySet(),
)
