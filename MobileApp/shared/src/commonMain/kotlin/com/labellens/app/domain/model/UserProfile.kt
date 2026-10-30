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
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            PEANUT -> "Đậu phộng / Lạc"
            MILK -> "Sữa / Chế phẩm sữa"
            EGG -> "Trứng"
            SOY -> "Đậu nành"
            WHEAT_GLUTEN -> "Lúa mì / Gluten"
            FISH -> "Cá"
            SHELLFISH -> "Hải sản / Giáp xác"
            SESAME -> "Vừng / Mè"
            TREE_NUTS -> "Hạt cây"
        }

        AppLanguage.ENGLISH -> displayName
    }
}

@Serializable
enum class AvoidPreference(val displayName: String) {
    ADDED_SUGAR("Added Sugar"),
    PALM_OIL("Palm Oil"),
    ARTIFICIAL_SWEETENERS("Artificial Sweeteners"),
    ARTIFICIAL_COLOURS("Artificial Colours"),
    MSG("MSG (Monosodium Glutamate)"),
    HIGH_SODIUM("High Sodium"),
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            ADDED_SUGAR -> "Đường bổ sung"
            PALM_OIL -> "Dầu cọ"
            ARTIFICIAL_SWEETENERS -> "Chất tạo ngọt nhân tạo"
            ARTIFICIAL_COLOURS -> "Phẩm màu nhân tạo"
            MSG -> "Bột ngọt (MSG / Mì chính)"
            HIGH_SODIUM -> "Hàm lượng natri cao"
        }

        AppLanguage.ENGLISH -> displayName
    }
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
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            LOW_SUGAR -> "Ít đường"
            LOW_SODIUM -> "Ít muối / Natri"
            VEGAN -> "Thuần chay"
            VEGETARIAN -> "Ăn chay"
            HALAL_CONSCIOUS -> "Tiêu chuẩn Halal"
            PREGNANCY_SAFE -> "An toàn thai kỳ"
            DIABETES_CAUTIOUS -> "Thận trọng tiểu đường"
        }

        AppLanguage.ENGLISH -> displayName
    }
}

@Serializable
data class UserProfile(
    val allergies: Set<Allergen> = emptySet(),
    val avoidList: Set<AvoidPreference> = emptySet(),
    val goals: Set<DietaryGoal> = emptySet(),
)
