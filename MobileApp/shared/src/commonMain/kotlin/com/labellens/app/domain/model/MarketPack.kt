package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Market(val displayName: String, val regionCode: String) {
    US("United States", "US"),
    EU_UK("European Union & UK", "EU_UK"),
    AU_NZ("Australia & New Zealand", "AU_NZ"),
    JP("Japan", "JP"), // Enum placeholder, no pack data in this wave
    KR("South Korea", "KR"), // Enum placeholder, no pack data in this wave
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            US -> "Hoa Kỳ (US)"
            EU_UK -> "Liên minh Châu Âu & Anh (EU/UK)"
            AU_NZ -> "Úc & New Zealand (AU/NZ)"
            JP -> "Nhật Bản (JP)"
            KR -> "Hàn Quốc (KR)"
        }

        AppLanguage.ENGLISH -> displayName
    }
}

@Serializable
enum class AllergenCategory(val id: String, val defaultName: String) {
    MILK("milk", "Milk / Dairy"),
    EGG("egg", "Egg"),
    FISH("fish", "Fish"),
    CRUSTACEANS("crustaceans", "Crustaceans / Shellfish"),
    TREE_NUTS("tree_nuts", "Tree Nuts"),
    PEANUT("peanut", "Peanut"),
    WHEAT_GLUTEN("wheat_gluten", "Wheat / Gluten"),
    SOY("soy", "Soy / Soybeans"),
    SESAME("sesame", "Sesame"),
    CELERY("celery", "Celery"),
    MUSTARD("mustard", "Mustard"),
    SULPHITES("sulphites", "Sulphur dioxide / Sulphites"),
    LUPIN("lupin", "Lupin"),
    MOLLUSCS("molluscs", "Molluscs"),
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            MILK -> "Sữa / Chế phẩm sữa"
            EGG -> "Trứng"
            FISH -> "Cá"
            CRUSTACEANS -> "Hải sản / Giáp xác"
            TREE_NUTS -> "Hạt cây"
            PEANUT -> "Đậu phộng / Lạc"
            WHEAT_GLUTEN -> "Lúa mì / Gluten"
            SOY -> "Đậu nành"
            SESAME -> "Vừng / Mè"
            CELERY -> "Cần tây"
            MUSTARD -> "Mù tạt"
            SULPHITES -> "Sulphite / Lưu huỳnh"
            LUPIN -> "Đậu Lupin"
            MOLLUSCS -> "Thân mềm"
        }

        AppLanguage.ENGLISH -> defaultName
    }

    fun toAllergen(): Allergen = when (this) {
        MILK -> Allergen.MILK
        EGG -> Allergen.EGG
        FISH -> Allergen.FISH
        CRUSTACEANS -> Allergen.SHELLFISH
        TREE_NUTS -> Allergen.TREE_NUTS
        PEANUT -> Allergen.PEANUT
        WHEAT_GLUTEN -> Allergen.WHEAT_GLUTEN
        SOY -> Allergen.SOY
        SESAME -> Allergen.SESAME
        CELERY -> Allergen.CELERY
        MUSTARD -> Allergen.MUSTARD
        SULPHITES -> Allergen.SULPHITES
        LUPIN -> Allergen.LUPIN
        MOLLUSCS -> Allergen.MOLLUSCS
    }
}

@Serializable
enum class AttentionFlag(val displayName: String) {
    SODIUM("High Sodium"),
    ADDED_SUGARS("Added Sugars"),
    NON_NUTRITIVE_SWEETENERS("Non-Nutritive Sweeteners"),
    CAFFEINE("Caffeine"),
    ARTIFICIAL_COLOURS("Artificial Colours"),
    ;

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> when (this) {
            SODIUM -> "Hàm lượng Natri cao"
            ADDED_SUGARS -> "Đường bổ sung"
            NON_NUTRITIVE_SWEETENERS -> "Chất tạo ngọt không dinh dưỡng"
            CAFFEINE -> "Caffeine"
            ARTIFICIAL_COLOURS -> "Phẩm màu nhân tạo"
        }

        AppLanguage.ENGLISH -> displayName
    }
}

@Serializable
enum class ProfileFitState(val priority: Int) {
    CONTAINS_YOURS(6),
    CANNOT_RULE_OUT(5),
    NOT_ENOUGH_INFO(4),
    DEPENDS_ON_PORTION(3),
    WORTH_NOTING(2),
    NO_MATCHES(1),
}

@Serializable
data class FitHeaderInfo(
    val state: ProfileFitState,
    val title: String,
    val subtitle: String,
    val matchedTerms: List<String> = emptyList(),
    val verbatimSourceLine: String? = null,
    val reasonNote: String? = null,
    val isCutOff: Boolean = false,
    val badgeLabel: String = "",
)

@Serializable
data class MarketPack(
    val market: Market,
    val name: String,
    val categories: List<AllergenCategory>,
)

object MarketPacks {
    val US = MarketPack(
        market = Market.US,
        name = "US (FALCPA / FASTER)",
        categories = listOf(
            AllergenCategory.MILK,
            AllergenCategory.EGG,
            AllergenCategory.FISH,
            AllergenCategory.CRUSTACEANS,
            AllergenCategory.TREE_NUTS,
            AllergenCategory.PEANUT,
            AllergenCategory.WHEAT_GLUTEN,
            AllergenCategory.SOY,
            AllergenCategory.SESAME,
        ),
    )

    val EU_UK = MarketPack(
        market = Market.EU_UK,
        name = "EU & UK (Regulation 1169/2011)",
        categories = listOf(
            AllergenCategory.WHEAT_GLUTEN, // Cereals containing gluten
            AllergenCategory.CRUSTACEANS,
            AllergenCategory.EGG,
            AllergenCategory.FISH,
            AllergenCategory.PEANUT,
            AllergenCategory.SOY,
            AllergenCategory.MILK,
            AllergenCategory.TREE_NUTS,
            AllergenCategory.CELERY,
            AllergenCategory.MUSTARD,
            AllergenCategory.SESAME,
            AllergenCategory.SULPHITES,
            AllergenCategory.LUPIN,
            AllergenCategory.MOLLUSCS,
        ),
    )

    val AU_NZ = MarketPack(
        market = Market.AU_NZ,
        name = "Australia & New Zealand (FSANZ Standard 1.2.3)",
        categories = listOf(
            AllergenCategory.WHEAT_GLUTEN, // Cereals containing gluten
            AllergenCategory.CRUSTACEANS,
            AllergenCategory.EGG,
            AllergenCategory.FISH,
            AllergenCategory.MILK,
            AllergenCategory.PEANUT,
            AllergenCategory.SESAME,
            AllergenCategory.SOY,
            AllergenCategory.TREE_NUTS,
            AllergenCategory.LUPIN,
            AllergenCategory.MOLLUSCS,
            AllergenCategory.SULPHITES,
        ),
    )

    fun getPack(market: Market): MarketPack? = when (market) {
        Market.US -> US
        Market.EU_UK -> EU_UK
        Market.AU_NZ -> AU_NZ
        Market.JP, Market.KR -> null // Ship NO pack data for JP/KR in this wave
    }

    fun activeCategories(market: Market, importedFoodMode: Boolean): Set<AllergenCategory> {
        if (importedFoodMode) {
            return (US.categories + EU_UK.categories + AU_NZ.categories).toSet()
        }
        return getPack(market)?.categories?.toSet() ?: US.categories.toSet()
    }
}
