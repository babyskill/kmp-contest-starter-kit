package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.ProductType
import com.labellens.app.domain.model.ProfileFitState

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

    fun categoryName(language: AppLanguage, category: String): String = when (language) {
        AppLanguage.VIETNAMESE -> when (category.trim()) {
            "Acidity Regulator / Antioxidant" -> "Chất điều chỉnh độ acid / Chất chống oxy hóa"

            "Acidulant" -> "Chất điều chỉnh độ acid"

            "Acidulant / Preservative" -> "Chất điều chỉnh độ acid / Chất bảo quản"

            "Alkaline Agent / Leavener" -> "Chất tạo kiềm / Chất tạo xốp"

            "Alkaline Salt" -> "Muối kiềm"

            "Allium / Spice" -> "Hành tỏi / Gia vị"

            "Animal Fat" -> "Mỡ động vật"

            "Animal Protein" -> "Đạm động vật"

            "Animal Protein / Gelling Agent" -> "Đạm động vật / Chất tạo gel"

            "Anti-Caking Agent" -> "Chất chống đông vón"

            "Antioxidant" -> "Chất chống oxy hóa"

            "Antioxidant / Nutrient" -> "Chất chống oxy hóa / Dưỡng chất"

            "Aromatic Rhizome" -> "Củ gia vị thơm"

            "Base Liquid" -> "Dung dịch nền"

            "Botanical Extract" -> "Chiết xuất thực vật"

            "Carbohydrate / Carrier" -> "Carbohydrate / Chất mang"

            "Citrus Herb" -> "Thảo mộc họ cam chanh"

            "Condiment / Fermented Food" -> "Gia vị / Thực phẩm lên men"

            "Curing Agent / Preservative" -> "Chất ủ muối / Chất bảo quản"

            "Dairy" -> "Chế phẩm từ sữa"

            "Dairy Fat" -> "Chất béo từ sữa"

            "Emulsifier" -> "Chất nhũ hóa"

            "Emulsifier / Protein Source" -> "Chất nhũ hóa / Nguồn đạm"

            "Fermented Chili Paste" -> "Tương ớt lên men"

            "Fermented Seasoning" -> "Gia vị lên men"

            "Fermented Vinegar" -> "Giấm lên men"

            "Firming Agent / Electrolyte" -> "Chất làm rắn / Chất điện giải"

            "Fish / Seafood" -> "Cá / Hải sản"

            "Flavor Enhancer" -> "Chất điều vị"

            "Flavoring" -> "Hương liệu"

            "Flavoring / Mold Inhibitor" -> "Hương liệu / Chất ức chế nấm mốc"

            "Flow Agent / Lubricant" -> "Chất chống dính / Chất trợ chảy"

            "Food Coloring" -> "Phẩm màu thực phẩm"

            "Gelling Agent" -> "Chất tạo gel"

            "Grain / Carbohydrate" -> "Ngũ cốc / Carbohydrate"

            "Herb" -> "Thảo mộc"

            "Herb / Seed" -> "Thảo mộc / Hạt giống"

            "Humectant" -> "Chất giữ ẩm"

            "Leavening Agent" -> "Chất tạo xốp"

            "Marine Botanical" -> "Thực vật biển"

            "Marine Botanical / Umami" -> "Rong biển / Vị Umami"

            "Marine Protein" -> "Đạm hải sản"

            "Mineral" -> "Khoáng chất"

            "Mineral / Anti-Caking Agent" -> "Khoáng chất / Chất chống đông vón"

            "Mineral / Color / Antacid" -> "Khoáng chất / Phẩm màu / Kháng acid"

            "Mineral / Seasoning" -> "Khoáng chất / Gia vị"

            "Mineral Compound" -> "Hợp chất khoáng"

            "Mineral Pigment" -> "Sắc tố khoáng"

            "Moisture Retention / Phosphate" -> "Chất giữ ẩm / Phosphat"

            "Mold Inhibitor" -> "Chất ức chế nấm mốc"

            "Mushroom Botanical / Umami" -> "Chiết xuất nấm / Vị Umami"

            "Natural Antioxidant" -> "Chất chống oxy hóa tự nhiên"

            "Natural Color" -> "Phẩm màu tự nhiên"

            "Natural Plant Pigment" -> "Sắc tố thực vật tự nhiên"

            "Natural Spice / Color" -> "Gia vị tự nhiên / Màu thực phẩm"

            "Natural Sweetener" -> "Chất tạo ngọt tự nhiên"

            "Non-Nutritive Sweetener" -> "Chất tạo ngọt không calo"

            "Nut / Legume" -> "Hạt dinh dưỡng / Đậu"

            "Nut / Seed Butter" -> "Bơ hạt dinh dưỡng"

            "Plant / Botanical" -> "Thực vật / Thảo mộc"

            "Plant Botanical" -> "Chiết xuất thực vật"

            "Plant Emulsifier" -> "Chất nhũ hóa thực vật"

            "Plant Fat" -> "Chất béo thực vật"

            "Plant Gelling Agent" -> "Chất tạo gel thực vật"

            "Plant Oil" -> "Dầu thực vật"

            "Plant-Based Sweetener" -> "Chất tạo ngọt từ thực vật"

            "Preservative" -> "Chất bảo quản"

            "Protein / Binding Agent" -> "Chất đạm / Chất kết dính"

            "Salt Substitute" -> "Chất thay thế muối"

            "Savory Flavoring" -> "Gia vị đậm đà"

            "Seed / Oil" -> "Hạt lấy dầu"

            "Shellfish" -> "Hải sản giáp xác"

            "Simple Sugar" -> "Đường đơn"

            "Spice" -> "Gia vị"

            "Spice / Herb" -> "Gia vị / Thảo mộc"

            "Starch" -> "Tinh bột"

            "Sugar Alcohol" -> "Đường rượu (Polyol)"

            "Sweetener" -> "Chất tạo ngọt"

            "Synthetic Antioxidant" -> "Chất chống oxy hóa tổng hợp"

            "Synthetic Dye" -> "Phẩm màu tổng hợp"

            "Synthetic Emulsifier" -> "Chất nhũ hóa tổng hợp"

            "Thickener / Stabilizer" -> "Chất làm dày / Chất ổn định"

            "Tree Nut" -> "Hạt cây dinh dưỡng"

            "Vegetable Fat" -> "Chất béo thực vật"

            "Vegetable Oil" -> "Dầu thực vật"

            "Vitamin" -> "Vitamin"

            "Vitamin / Color" -> "Vitamin / Phẩm màu"

            "Vitamin / Supplement Active" -> "Vitamin / Hoạt chất bổ sung"

            "Whole Grain" -> "Ngũ cốc nguyên hạt"

            else -> when (category.lowercase().trim()) {
                "sweetener" -> "chất tạo ngọt"
                "preservative" -> "chất bảo quản"
                "seasoning" -> "gia vị"
                "emulsifier" -> "chất nhũ hóa"
                "antioxidant" -> "chất chống oxy hóa"
                "acidulant" -> "chất điều chỉnh độ acid"
                "thickener" -> "chất làm dày"
                "stabilizer" -> "chất ổn định"
                "color" -> "phẩm màu"
                "flavor" -> "hương liệu"
                "flavor enhancer" -> "chất điều vị"
                else -> category
            }
        }

        AppLanguage.ENGLISH -> category
    }

    fun additiveUsedAs(language: AppLanguage, plainExplanation: String, category: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "$plainExplanation Dùng làm ${categoryName(language, category).lowercase()}."
        AppLanguage.ENGLISH -> "$plainExplanation Used as ${category.lowercase()}."
    }

    fun fallbackCategory(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chưa có trong cơ sở dữ liệu"
        AppLanguage.ENGLISH -> "Not in the database yet"
    }

    fun fallbackPlainExplanation(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần chưa có trong cơ sở dữ liệu đối sánh."
        AppLanguage.ENGLISH -> "Ingredient not yet in standard knowledge base."
    }

    fun fallbackWhyUsed(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chưa có thông tin công dụng."
        AppLanguage.ENGLISH -> "No usage information available."
    }

    fun fallbackDoseContextNote(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chưa có ghi chú liều lượng."
        AppLanguage.ENGLISH -> "No dosage note available."
    }

    fun nutritionName(language: AppLanguage, standardName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> when (standardName.trim()) {
            "Calories" -> "Năng lượng (Calories)"
            "Sugars" -> "Đường (Sugars)"
            "Total Sugar", "Total Sugars" -> "Tổng lượng đường (Total Sugars)"
            "Added Sugar", "Added Sugars" -> "Đường bổ sung (Added Sugars)"
            "Sodium" -> "Natri (Sodium)"
            "Saturated Fat", "Sat Fat" -> "Chất béo bão hòa"
            "Trans Fat" -> "Chất béo chuyển hóa"
            "Total Fat", "Fat" -> "Tổng chất béo (Total Fat)"
            "Cholesterol" -> "Cholesterol"
            "Total Carbohydrate", "Carbohydrate", "Carbohydrates", "Carbs" -> "Tổng Carbohydrate (Carbs)"
            "Protein" -> "Chất đạm (Protein)"
            "Dietary Fiber", "Fiber", "Fibre" -> "Chất xơ (Fiber)"
            "Calcium" -> "Canxi (Calcium)"
            "Iron" -> "Sắt (Iron)"
            "Potassium" -> "Kali (Potassium)"
            "Vitamin A" -> "Vitamin A"
            "Vitamin C" -> "Vitamin C"
            "Vitamin D", "Vitamin D3" -> "Vitamin D"
            "Vitamin E" -> "Vitamin E"
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

    // Wave 1 Strings: Roll-up, 2% Cut-Line, Badges, Halos & Marquee Notes
    fun sweetenersRollUpTitle(language: AppLanguage, count: Int): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chất tạo ngọt (phát hiện $count loại)"
        AppLanguage.ENGLISH -> "Sweeteners ($count found)"
    }

    fun sodiumRollUpTitle(language: AppLanguage, count: Int): String = when (language) {
        AppLanguage.VIETNAMESE -> "Nguồn Natri (phát hiện $count loại)"
        AppLanguage.ENGLISH -> "Sodium Sources ($count found)"
    }

    fun cutLineDivider(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Hàm lượng rất nhỏ / Hương liệu (≤ 2%)"
        AppLanguage.ENGLISH -> "Tiny amounts / Flavourings (≤ 2%)"
    }

    fun catchAllBadge(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thuật ngữ chung"
        AppLanguage.ENGLISH -> "Catch-all term"
    }

    fun catchAllNote(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Nhãn không tiết lộ các chất cụ thể nào được sử dụng trong danh mục chung này."
        AppLanguage.ENGLISH -> "The label does not disclose which specific substances are inside this term."
    }

    fun haloFibreTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chất xơ bổ sung & Chất tạo ngọt"
        AppLanguage.ENGLISH -> "Added Fiber & Sweeteners"
    }

    fun haloFibreDetail(language: AppLanguage, fiberName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chất xơ được bổ sung dưới dạng thành phần cô lập ($fiberName) trong khi sản phẩm cũng chứa các chất tạo ngọt bổ sung."
        AppLanguage.ENGLISH -> "Dietary fiber is included as an isolated ingredient ($fiberName) while the product also contains added sweeteners."
    }

    fun haloFruitTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Trái cây từ nước ép cô đặc"
        AppLanguage.ENGLISH -> "Fruit from Concentrate"
    }

    fun haloFruitDetail(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Thành phần trái cây trong sản phẩm này xuất hiện dưới dạng nước ép cô đặc thay vì trái cây nguyên quả."
        AppLanguage.ENGLISH -> "The fruit content in this product appears in the ingredient list as fruit juice concentrate rather than whole fruit."
    }

    fun marqueeCutLineNote(language: AppLanguage, ingredientName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Được liệt kê trong nhóm thành phần chiếm 2% trở xuống ($ingredientName)."
        AppLanguage.ENGLISH -> "Listed in the 2%-or-less section ($ingredientName)."
    }

    fun marqueeSaltNote(language: AppLanguage, ingredientName: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Được liệt kê sau muối trong danh sách thành phần ($ingredientName)."
        AppLanguage.ENGLISH -> "Listed after salt in the ingredient list ($ingredientName)."
    }

    fun positionTitle(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Vị trí trên nhãn thành phần"
        AppLanguage.ENGLISH -> "Position on Label"
    }

    // Wave 2 Strings: Six-State Result Header & Fit Explanations
    fun fitStateTitle(language: AppLanguage, state: com.labellens.app.domain.model.ProfileFitState): String = when (language) {
        AppLanguage.VIETNAMESE -> when (state) {
            com.labellens.app.domain.model.ProfileFitState.CONTAINS_YOURS -> "Chứa chất gây dị ứng của bạn"
            com.labellens.app.domain.model.ProfileFitState.CANNOT_RULE_OUT -> "Không thể loại trừ dị ứng"
            com.labellens.app.domain.model.ProfileFitState.NOT_ENOUGH_INFO -> "Chưa đủ thông tin"
            com.labellens.app.domain.model.ProfileFitState.DEPENDS_ON_PORTION -> "Tùy thuộc khẩu phần"
            com.labellens.app.domain.model.ProfileFitState.WORTH_NOTING -> "Đáng lưu ý"
            com.labellens.app.domain.model.ProfileFitState.NO_MATCHES -> "Không có cảnh báo phù hợp"
        }

        AppLanguage.ENGLISH -> when (state) {
            com.labellens.app.domain.model.ProfileFitState.CONTAINS_YOURS -> "Contains Your Allergen"
            com.labellens.app.domain.model.ProfileFitState.CANNOT_RULE_OUT -> "Cannot Rule Out Allergen"
            com.labellens.app.domain.model.ProfileFitState.NOT_ENOUGH_INFO -> "Not Enough Info"
            com.labellens.app.domain.model.ProfileFitState.DEPENDS_ON_PORTION -> "Depends on Portion"
            com.labellens.app.domain.model.ProfileFitState.WORTH_NOTING -> "Worth Noting"
            com.labellens.app.domain.model.ProfileFitState.NO_MATCHES -> "No Profile Matches"
        }
    }

    fun fitStateSubtitle(language: AppLanguage, state: com.labellens.app.domain.model.ProfileFitState): String = when (language) {
        AppLanguage.VIETNAMESE -> when (state) {
            com.labellens.app.domain.model.ProfileFitState.CONTAINS_YOURS -> "Trùng khớp trực tiếp với chất dị ứng được khai báo trong hồ sơ cá nhân."
            com.labellens.app.domain.model.ProfileFitState.CANNOT_RULE_OUT -> "Cảnh báo phòng ngừa hoặc thuật ngữ chung có thể ẩn chứa chất dị ứng."
            com.labellens.app.domain.model.ProfileFitState.NOT_ENOUGH_INFO -> "Danh sách thành phần chưa đầy đủ hoặc bị cắt bớt — vui lòng kiểm tra lại nhãn."
            com.labellens.app.domain.model.ProfileFitState.DEPENDS_ON_PORTION -> "Đạt ngưỡng khi dùng cả hộp, mặc dù một khẩu phần ở mức chấp nhận được."
            com.labellens.app.domain.model.ProfileFitState.WORTH_NOTING -> "Các mục cần lưu ý vượt ngưỡng khuyến nghị trong mỗi khẩu phần."
            com.labellens.app.domain.model.ProfileFitState.NO_MATCHES -> "Đã đối chiếu các thành phần; không phát hiện chất dị ứng hay cảnh báo đã chọn."
        }

        AppLanguage.ENGLISH -> when (state) {
            com.labellens.app.domain.model.ProfileFitState.CONTAINS_YOURS -> "Direct match with allergens declared in your personal profile."
            com.labellens.app.domain.model.ProfileFitState.CANNOT_RULE_OUT -> "Precautionary warning or catch-all term may conceal declared allergens."
            com.labellens.app.domain.model.ProfileFitState.NOT_ENOUGH_INFO -> "Ingredient list appears incomplete or cut off — review label carefully."
            com.labellens.app.domain.model.ProfileFitState.DEPENDS_ON_PORTION -> "Within threshold per serving, but exceeds when consuming whole container."
            com.labellens.app.domain.model.ProfileFitState.WORTH_NOTING -> "Enabled attention flags exceed per-serving thresholds on this label."
            com.labellens.app.domain.model.ProfileFitState.NO_MATCHES -> "All declared ingredients checked; no profile flags or allergens triggered."
        }
    }

    fun fitContainsYoursDetail(language: AppLanguage, allergens: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Đã phát hiện chất dị ứng được khai báo trên nhãn: $allergens"
        AppLanguage.ENGLISH -> "Declared allergen identified on label: $allergens"
    }

    fun fitCannotRuleOutDetail(language: AppLanguage, reasons: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Khả năng có mặt chất dị ứng hoặc thành phần mang chưa công bố: $reasons"
        AppLanguage.ENGLISH -> "Possible allergen presence or undeclared carrier: $reasons"
    }

    fun fitNotEnoughInfoDetail(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Một phần danh sách thành phần đã bị cắt bớt hoặc chưa ghi nhận đủ. Vui lòng kiểm tra bao bì gốc."
        AppLanguage.ENGLISH -> "Part of the ingredient list was cut off or not captured. Check the physical package."
    }

    fun fitDependsOnPortionDetail(language: AppLanguage, flags: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Chỉ vượt ngưỡng 20% giá trị hằng ngày khi dùng cả hộp: $flags"
        AppLanguage.ENGLISH -> "Amount exceeds 20% DV only when consuming the whole package: $flags"
    }

    fun fitWorthNotingDetail(language: AppLanguage, flags: String): String = when (language) {
        AppLanguage.VIETNAMESE -> "Vượt ngưỡng lưu ý cho mỗi khẩu phần: $flags"
        AppLanguage.ENGLISH -> "Exceeds attention flag per-serving threshold: $flags"
    }

    fun fitNoMatchesDetail(language: AppLanguage): String = when (language) {
        AppLanguage.VIETNAMESE -> "Không phát hiện thành phần cần tránh hoặc chất dị ứng nào trong hồ sơ của bạn."
        AppLanguage.ENGLISH -> "No profile-flagged ingredients or allergens were detected."
    }

    fun fitStateBadge(language: AppLanguage, state: ProfileFitState): String = when (language) {
        AppLanguage.VIETNAMESE -> when (state) {
            ProfileFitState.CONTAINS_YOURS -> "CHỨA DỊ ỨNG"
            ProfileFitState.CANNOT_RULE_OUT -> "KHÔNG THỂ LOẠI TRỪ"
            ProfileFitState.NOT_ENOUGH_INFO -> "CHƯA ĐỦ THÔNG TIN"
            ProfileFitState.DEPENDS_ON_PORTION -> "TÙY KHẨU PHẦN"
            ProfileFitState.WORTH_NOTING -> "ĐÁNG LƯU Ý"
            ProfileFitState.NO_MATCHES -> "KHÔNG TRÙNG KHỚP"
        }

        AppLanguage.ENGLISH -> when (state) {
            ProfileFitState.CONTAINS_YOURS -> "CONTAINS YOURS"
            ProfileFitState.CANNOT_RULE_OUT -> "CANNOT RULE OUT"
            ProfileFitState.NOT_ENOUGH_INFO -> "NOT ENOUGH INFO"
            ProfileFitState.DEPENDS_ON_PORTION -> "DEPENDS ON PORTION"
            ProfileFitState.WORTH_NOTING -> "WORTH NOTING"
            ProfileFitState.NO_MATCHES -> "NO MATCHES"
        }
    }

    fun productTypeName(language: AppLanguage, type: ProductType): String = when (language) {
        AppLanguage.VIETNAMESE -> when (type) {
            ProductType.SHAMPOO -> "Dầu gội"
            ProductType.CONDITIONER -> "Dầu xả"
            ProductType.BODY_WASH -> "Sữa tắm"
            ProductType.CEREAL -> "Ngũ cốc"
            ProductType.SNACK -> "Đồ ăn nhẹ"
            ProductType.BISCUITS -> "Bánh quy"
            ProductType.NOODLES -> "Mì ăn liền"
            ProductType.BEVERAGE -> "Đồ uống"
            ProductType.SUPPLEMENT -> "Thực phẩm bổ sung"
            ProductType.GRANOLA_BAR -> "Thanh ngũ cốc"
            ProductType.FOOD -> "Thực phẩm"
            ProductType.PERSONAL_CARE -> "Chăm sóc cá nhân"
        }

        AppLanguage.ENGLISH -> when (type) {
            ProductType.SHAMPOO -> "Shampoo"
            ProductType.CONDITIONER -> "Conditioner"
            ProductType.BODY_WASH -> "Body Wash"
            ProductType.CEREAL -> "Cereal"
            ProductType.SNACK -> "Snack"
            ProductType.BISCUITS -> "Biscuits"
            ProductType.NOODLES -> "Instant Noodles"
            ProductType.BEVERAGE -> "Beverage"
            ProductType.SUPPLEMENT -> "Supplement"
            ProductType.GRANOLA_BAR -> "Granola Bar"
            ProductType.FOOD -> "Food"
            ProductType.PERSONAL_CARE -> "Personal Care"
        }
    }
}
