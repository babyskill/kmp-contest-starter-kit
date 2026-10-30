package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal

object AnalysisStrings {

    fun uncertaintyShortText(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Văn bản nhãn có vẻ ngắn hoặc chưa đầy đủ. Một số thành phần hoặc giá trị dinh dưỡng có thể chưa được ghi nhận."
        AppLanguage.ENGLISH -> "Label text appears short or incomplete. Some ingredients or nutrition values might not be captured."
    }

    fun uncertaintyUnclassified(language: AppLanguage, tokens: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Không tìm thấy đối sánh tiêu chuẩn cho các thành phần: $tokens."
        AppLanguage.ENGLISH -> "Could not find standard matches for parsed ingredients: $tokens."
    }

    fun allergenTitle(language: AppLanguage, allergen: Allergen): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa ${allergen.localizedName(language)}"
        AppLanguage.ENGLISH -> "Contains ${allergen.displayName}"
    }

    fun allergenDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Tìm thấy trong thành phần ($canonicalName). Trùng khớp với danh sách dị ứng trong hồ sơ của bạn."
        AppLanguage.ENGLISH -> "Found in ingredients ($canonicalName). Matched against your profile allergen list."
    }

    fun highSodiumTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Hàm lượng natri cao"
        AppLanguage.ENGLISH -> "High Sodium Level"
    }

    fun highSodiumDetail(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Sản phẩm này chứa hàm lượng natri cao trong mỗi khẩu phần, thuộc danh sách bạn cần tránh."
        AppLanguage.ENGLISH -> "This product provides a high concentration of sodium per serving, which you flagged to avoid."
    }

    fun addedSugarTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Đường bổ sung / Chất tạo ngọt"
        AppLanguage.ENGLISH -> "Added Sugar / Sweetener"
    }

    fun addedSugarDetail(language: AppLanguage, source: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa đường bổ sung ($source)."
        AppLanguage.ENGLISH -> "Contains added sugars ($source)."
    }

    fun avoidTitle(language: AppLanguage, avoid: AvoidPreference): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa ${avoid.localizedName(language)}"
        AppLanguage.ENGLISH -> "Contains ${avoid.displayName}"
    }

    fun avoidDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Phát hiện $canonicalName, trùng khớp với danh sách cần tránh của bạn."
        AppLanguage.ENGLISH -> "Identified $canonicalName, matching your personal avoidance list."
    }

    fun goalWatchSodiumTitle(language: AppLanguage, goal: DietaryGoal): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chú ý natri (Mục tiêu: ${goal.localizedName(language)})"
        AppLanguage.ENGLISH -> "Watch Sodium (Your Goal: ${goal.displayName})"
    }

    fun goalWatchSodiumDetail(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Hàm lượng natri đáng lưu ý so với mục tiêu dinh dưỡng của bạn."
        AppLanguage.ENGLISH -> "Sodium content is notable relative to your dietary goal."
    }

    fun goalElevatedSugarTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Hàm lượng đường đáng chú ý"
        AppLanguage.ENGLISH -> "Elevated Sugar Content"
    }

    fun goalElevatedSugarDetail(language: AppLanguage, goal: DietaryGoal): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa lượng carb/đường đáng kể, có thể ảnh hưởng đến mục tiêu ${goal.localizedName(language)} của bạn."
        AppLanguage.ENGLISH -> "Contains notable carbohydrates/sugars, which may impact your ${goal.displayName} goal."
    }

    fun goalNotVeganTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Không phù hợp với người ăn thuần chay"
        AppLanguage.ENGLISH -> "Not Vegan Suitable"
    }

    fun goalNotVeganDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa $canonicalName, có nguồn gốc từ động vật."
        AppLanguage.ENGLISH -> "Contains $canonicalName, derived from animal sources."
    }

    fun goalNotVegetarianTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Không phù hợp với người ăn chay"
        AppLanguage.ENGLISH -> "Not Vegetarian Suitable"
    }

    fun goalNotVegetarianDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa $canonicalName."
        AppLanguage.ENGLISH -> "Contains $canonicalName."
    }

    fun goalNonHalalTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần không phù hợp Halal"
        AppLanguage.ENGLISH -> "Non-Halal Ingredient"
    }

    fun goalNonHalalDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa $canonicalName."
        AppLanguage.ENGLISH -> "Contains $canonicalName."
    }

    fun goalCaffeineTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chứa caffeine"
        AppLanguage.ENGLISH -> "Contains Caffeine"
    }

    fun goalCaffeineDetail(language: AppLanguage, canonicalName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Tìm thấy trong $canonicalName; nên hạn chế trong thai kỳ."
        AppLanguage.ENGLISH -> "Found in $canonicalName; moderate intake during pregnancy."
    }

    fun additiveUsedAs(language: AppLanguage, plainExplanation: String, category: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "$plainExplanation Dùng làm ${category.lowercase()}."
        AppLanguage.ENGLISH -> "$plainExplanation Used as ${category.lowercase()}."
    }

    fun fallbackCategory(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần phổ biến"
        AppLanguage.ENGLISH -> "Standard ingredient"
    }

    fun fallbackPlainExplanation(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần nhãn đã nhận diện. Không có cảnh báo đặc biệt."
        AppLanguage.ENGLISH -> "Recognised label component. No specific flags noted."
    }

    fun fallbackWhyUsed(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần công thức hoặc chế biến thông thường."
        AppLanguage.ENGLISH -> "Common recipe or processing component."
    }

    fun fallbackDoseContextNote(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần thực phẩm tiêu chuẩn."
        AppLanguage.ENGLISH -> "Standard culinary ingredient."
    }

    fun nutritionName(language: AppLanguage, standardName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> when (standardName) {
            "Calories" -> "Năng lượng (Calories)"
            "Sugars" -> "Đường (Sugars)"
            "Sodium" -> "Natri (Sodium)"
            "Saturated Fat" -> "Chất béo bão hòa"
            "Protein" -> "Chất đạm (Protein)"
            "Dietary Fiber" -> "Chất xơ (Fiber)"
            else -> standardName
        }

        AppLanguage.ENGLISH -> standardName
    }

    fun nutritionContext(language: AppLanguage, indicatorKey: String): String = when (language) {
        AppLanguage.VIETNAMESE -> when (indicatorKey) {
            "Substantial" -> "Đáng kể"
            "Standard" -> "Tiêu chuẩn"
            "Notable sugar" -> "Nhiều đường"
            "Low sugar" -> "Ít đường"
            "Moderate" -> "Vừa phải"
            "High sodium" -> "Hàm lượng natri cao"
            "Low sodium" -> "Ít natri"
            "Notable" -> "Đáng lưu ý"
            "Protein source" -> "Nguồn cung cấp đạm"
            "Good fiber source" -> "Nguồn chất xơ dồi dào"
            else -> indicatorKey
        }

        AppLanguage.ENGLISH -> indicatorKey
    }

    fun quickGlance(language: AppLanguage, ingredientsCount: Int, watchOutCount: Int): String = when (language) {
        AppLanguage.VIETNAMESE -> {
            val personalStr = if (watchOutCount > 0) "$watchOutCount lưu ý cá nhân" else "Không có cảnh báo cá nhân"
            "$ingredientsCount thành phần đã phân tích • $personalStr"
        }

        AppLanguage.ENGLISH -> {
            val personalStr = if (watchOutCount > 0) "$watchOutCount personal note${if (watchOutCount > 1) "s" else ""}" else "No personal flags"
            "$ingredientsCount ingredients parsed • $personalStr"
        }
    }

    fun verdictSupplement(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Công thức thực phẩm bổ sung tiêu chuẩn cung cấp các vitamin hoạt tính cùng chất kết dính thông thường."
        AppLanguage.ENGLISH -> "Standard dietary supplement formula providing active vitamins with common binding agents."
    }

    fun verdictWatchOut(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Vui lòng xem lại các lưu ý cá nhân phía trên trùng khớp với hồ sơ dinh dưỡng hoặc cảnh báo dị ứng của bạn."
        AppLanguage.ENGLISH -> "Review the personal flags above that match your specific dietary preferences or allergen warnings."
    }

    fun verdictWorthNoting(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Sản phẩm chế biến tiện lợi với hương vị hài hòa, có bổ sung các phụ gia và gia vị thông thường."
        AppLanguage.ENGLISH -> "Convenient formulated product with balanced taste, incorporating common additives and seasonings."
    }

    fun verdictGoodToKnow(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Công thức lành mạnh tập trung vào các nguyên liệu tự nhiên nguyên bản và chế biến tối thiểu."
        AppLanguage.ENGLISH -> "Wholesome formulation centered on straightforward whole ingredients and minimal processing."
    }

    fun verdictBaseFood(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Công thức thực phẩm đóng gói tiêu chuẩn với thành phần quen thuộc và tỷ lệ cân bằng."
        AppLanguage.ENGLISH -> "Standard packaged food formulation with familiar ingredients and balanced recipe composition."
    }

    fun verdictDisclaimer(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chỉ mang tính chất giáo dục - đây không phải lời khuyên y tế."
        AppLanguage.ENGLISH -> "Education only - this is not medical advice."
    }
}
