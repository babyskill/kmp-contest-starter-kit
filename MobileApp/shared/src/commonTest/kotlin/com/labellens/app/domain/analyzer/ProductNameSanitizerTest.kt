package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductNameSanitizerTest {

    @Test
    fun testRejectsAuditDefectExamples() {
        // Example 1: "Thanh phan: Sua tuoi tiet trung 100%." (diacritic-insensitive, starts with Thanh phan, > 6 words)
        val ex1 = "Thanh phan: Sua tuoi tiet trung 100%."
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex1), "Should reject '$ex1'")
        assertEquals("Chưa đặt tên", ProductNameSanitizer.displayProductName(ex1, AppLanguage.VIETNAMESE))
        assertEquals("Untitled label", ProductNameSanitizer.displayProductName(ex1, AppLanguage.ENGLISH))

        // Vietnamese with diacritics
        val ex1WithDiacritics = "Thành phần: Sữa tươi tiệt trùng 100%."
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex1WithDiacritics), "Should reject '$ex1WithDiacritics'")

        // Example 2: "Ingredients: Enriched Wheat Flour (Wheat" (starts with Ingredients, sentence fragment ending mid-word)
        val ex2 = "Ingredients: Enriched Wheat Flour (Wheat"
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex2), "Should reject '$ex2'")
        assertEquals("Untitled label", ProductNameSanitizer.displayProductName(ex2, AppLanguage.ENGLISH))
        assertEquals("Chưa đặt tên", ProductNameSanitizer.displayProductName(ex2, AppLanguage.VIETNAMESE))

        // Sentence fragment without prefix (unclosed parenthesis / truncated mid-word)
        val ex2WithoutPrefix = "Enriched Wheat Flour (Wheat"
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex2WithoutPrefix), "Should reject '$ex2WithoutPrefix'")

        // Example 3: "Supplement Facts Serving Size: 1 Softgel." (starts with Supplement Facts)
        val ex3 = "Supplement Facts Serving Size: 1 Softgel."
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex3), "Should reject '$ex3'")
        assertEquals("Untitled label", ProductNameSanitizer.displayProductName(ex3, AppLanguage.ENGLISH))

        // Nutrition Facts
        val ex4 = "Nutrition Facts 100g"
        assertFalse(ProductNameSanitizer.looksLikeProductName(ex4), "Should reject '$ex4'")
    }

    @Test
    fun testRejectsSentenceFragmentsAndLists() {
        // Trailing punctuation / dangling connector
        assertFalse(ProductNameSanitizer.looksLikeProductName("Roasted Peanuts, Sugar,"))
        assertFalse(ProductNameSanitizer.looksLikeProductName("Water, Soybeans, Wheat, Salt."))
        assertFalse(ProductNameSanitizer.looksLikeProductName("Oat flakes and"))
        assertFalse(ProductNameSanitizer.looksLikeProductName("Bột mì và"))

        // Unbalanced brackets
        assertFalse(ProductNameSanitizer.looksLikeProductName("Flour [Wheat"))

        // More than 6 words
        assertFalse(ProductNameSanitizer.looksLikeProductName("This is a very long product name that should be rejected"))

        // Blank or generic placeholders
        assertFalse(ProductNameSanitizer.looksLikeProductName(""))
        assertFalse(ProductNameSanitizer.looksLikeProductName("   "))
        assertFalse(ProductNameSanitizer.looksLikeProductName("Scanned Product"))
        assertFalse(ProductNameSanitizer.looksLikeProductName("Sản phẩm quét"))
    }

    @Test
    fun testAcceptsValidProductNames() {
        assertTrue(ProductNameSanitizer.looksLikeProductName("Organic Almond Milk"))
        assertEquals("Organic Almond Milk", ProductNameSanitizer.displayProductName("Organic Almond Milk", AppLanguage.ENGLISH))

        assertTrue(ProductNameSanitizer.looksLikeProductName("Honey Oat Cereal"))
        assertEquals("Honey Oat Cereal", ProductNameSanitizer.displayProductName("Honey Oat Cereal", AppLanguage.VIETNAMESE))

        assertTrue(ProductNameSanitizer.looksLikeProductName("Clony"))
        assertEquals("Clony", ProductNameSanitizer.displayProductName("Clony", AppLanguage.VIETNAMESE))

        assertTrue(ProductNameSanitizer.looksLikeProductName("Sữa Tươi TH True Milk"))
        assertEquals("Sữa Tươi TH True Milk", ProductNameSanitizer.displayProductName("Sữa Tươi TH True Milk", AppLanguage.VIETNAMESE))
    }

    @Test
    fun testScanSummaryLocalizer() {
        // Base food scan stored in Vietnamese -> display in English
        val viStoredResult = LabelAnalysisResult(
            productName = "Sample",
            category = ProductCategory.FOOD,
            scanSource = ScanSource.PHOTO,
            verdict = "Công thức thực phẩm đóng gói tiêu chuẩn với thành phần quen thuộc và tỷ lệ cân bằng.",
            rawInputText = "...",
        )
        val enSummary = ScanSummaryLocalizer.localizedSummary(viStoredResult, AppLanguage.ENGLISH)
        assertEquals(
            "Standard packaged food formulation with familiar ingredients and balanced recipe composition.",
            enSummary,
        )

        // Supplement scan stored in Vietnamese -> display in English
        val supplementResult = LabelAnalysisResult(
            productName = "Supp",
            category = ProductCategory.SUPPLEMENT,
            scanSource = ScanSource.PHOTO,
            verdict = "Công thức thực phẩm bổ sung tiêu chuẩn cung cấp các vitamin hoạt tính cùng chất kết dính thông thường. Chỉ mang tính chất giáo dục - đây không phải lời khuyên y tế.",
            rawInputText = "...",
        )
        val enSuppSummary = ScanSummaryLocalizer.localizedSummary(supplementResult, AppLanguage.ENGLISH)
        assertTrue(enSuppSummary.contains("Standard dietary supplement formula"))
        assertTrue(enSuppSummary.contains("Education only - this is not medical advice."))

        // Personal watch-out alert scan
        val alertResult = LabelAnalysisResult(
            productName = "Alert Food",
            category = ProductCategory.FOOD,
            scanSource = ScanSource.PHOTO,
            verdict = "Vui lòng xem lại các lưu ý cá nhân phía trên trùng khớp với hồ sơ dinh dưỡng hoặc cảnh báo dị ứng của bạn.",
            watchOutForYou = listOf(
                FlaggedItem(title = "Peanuts", detail = "Allergen", severity = FlagSeverity.PERSONAL_ALLERGEN),
            ),
            rawInputText = "...",
        )
        val enAlertSummary = ScanSummaryLocalizer.localizedSummary(alertResult, AppLanguage.ENGLISH)
        assertEquals(
            "Review the personal flags above that match your specific dietary preferences or allergen warnings.",
            enAlertSummary,
        )

        // English stored scan -> display in Vietnamese
        val enStoredResult = LabelAnalysisResult(
            productName = "Sample",
            category = ProductCategory.FOOD,
            scanSource = ScanSource.PHOTO,
            verdict = "Standard packaged food formulation with familiar ingredients and balanced recipe composition.",
            rawInputText = "...",
        )
        val viSummary = ScanSummaryLocalizer.localizedSummary(enStoredResult, AppLanguage.VIETNAMESE)
        assertEquals(
            "Công thức thực phẩm đóng gói tiêu chuẩn với thành phần quen thuộc và tỷ lệ cân bằng.",
            viSummary,
        )
    }
}
