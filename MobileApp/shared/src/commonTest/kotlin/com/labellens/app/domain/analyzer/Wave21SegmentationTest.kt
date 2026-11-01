package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Wave21SegmentationTest {

    private val analyzer = LabelAnalyzer()

    companion object {
        val EXPECTED_CLONY_TOKENS = listOf(
            "Aqua/Waters",
            "Sodium Lauryl Sulfate",
            "Sodium Laureth Sulfate",
            "CAPB",
            "CDE",
            "Polyquats",
            "Plantacare",
            "EDGS",
            "Cetyl Alcohol",
            "Stearyl Alcohol",
            "PEG-7 Glyceryl Co-coat",
            "Tocopheryl Acetate",
            "Aminodimethicone",
            "Olive oil",
            "Emulsional Silicons",
            "Fragrance",
            "Sodium EDTA",
            "Preservatives",
            "Cosmetic Colors",
        )

        // Fixture A: Full back-label text with proper Vietnamese diacritics as printed on Clony label.
        // Sodium Laureth Sulfate is wrapped across lines to test line joining.
        val FIXTURE_A_CLONY_PROPER_DIACRITICS = """
            MƯỢT TÓC
            KHÔNG CẦN DẦU XẢ
            Thế hệ mới

            Dầu gội với công thức cải tiến chứa tinh dầu olive tự nhiên và hạt silicon protein nuôi dưỡng sâu từ gốc, giúp tóc suôn mượt và chắc khỏe.

            Hướng dẫn sử dụng: Làm ướt tóc, thoa một lượng dầu gội vừa đủ lên tóc, mát xa nhẹ nhàng da đầu rồi xả sạch với nước.

            Thành phần: Aqua/Waters, Sodium Lauryl Sulfate, Sodium
            Laureth Sulfate, CAPB, CDE, Polyquats, Plantacare, EDGS, Cetyl Alcohol, Stearyl Alcohol, PEG-7 Glyceryl Co-coat, Tocopheryl Acetate, Aminodimethicone, Olive oil, Emulsional Silicons, Fragrance, Sodium EDTA, Preservatives, Cosmetic Colors.

            Bảo quản: Tránh nhiệt độ cao và ánh sáng trực tiếp.
        """.trimIndent()

        // Fixture B: Same text with marker diacritic-stripped ("Thanh phan:")
        val FIXTURE_B_CLONY_STRIPPED_MARKER = """
            MƯỢT TÓC
            KHÔNG CẦN DẦU XẢ
            Thế hệ mới

            Dầu gội với công thức cải tiến chứa tinh dầu olive tự nhiên và hạt silicon protein nuôi dưỡng sâu từ gốc, giúp tóc suôn mượt và chắc khỏe.

            Hướng dẫn sử dụng: Làm ướt tóc, thoa một lượng dầu gội vừa đủ lên tóc, mát xa nhẹ nhàng da đầu rồi xả sạch với nước.

            Thanh phan: Aqua/Waters, Sodium Lauryl Sulfate, Sodium
            Laureth Sulfate, CAPB, CDE, Polyquats, Plantacare, EDGS, Cetyl Alcohol, Stearyl Alcohol, PEG-7 Glyceryl Co-coat, Tocopheryl Acetate, Aminodimethicone, Olive oil, Emulsional Silicons, Fragrance, Sodium EDTA, Preservatives, Cosmetic Colors.

            Bao quan: Tranh nhiet do cao va anh sang truc tiep.
        """.trimIndent()

        // Fixture C: Same text with the marker line removed entirely
        val FIXTURE_C_NO_MARKER = """
            MƯỢT TÓC
            KHÔNG CẦN DẦU XẢ
            Thế hệ mới

            Dầu gội với công thức cải tiến chứa tinh dầu olive tự nhiên và hạt silicon protein nuôi dưỡng sâu từ gốc, giúp tóc suôn mượt và chắc khỏe.

            Hướng dẫn sử dụng: Làm ướt tóc, thoa một lượng dầu gội vừa đủ lên tóc, mát xa nhẹ nhàng da đầu rồi xả sạch với nước.

            Aqua/Waters, Sodium Lauryl Sulfate, Sodium
            Laureth Sulfate, CAPB, CDE, Polyquats, Plantacare, EDGS, Cetyl Alcohol, Stearyl Alcohol, PEG-7 Glyceryl Co-coat, Tocopheryl Acetate, Aminodimethicone, Olive oil, Emulsional Silicons, Fragrance, Sodium EDTA, Preservatives, Cosmetic Colors.

            Bảo quản: Tránh nhiệt độ cao và ánh sáng trực tiếp.
        """.trimIndent()

        // Fixture D: English food label regression guard
        val FIXTURE_D_ENGLISH_FOOD = """
            INGREDIENTS: Whole Grain Oats, Sugar, Honey, Canola Oil, Sea Salt, Molasses, Tocopherols.
            CONTAINS: May contain Wheat.
            Nutrition Facts: Serving size 1 cup (40g). Calories: 160. Total Fat 3g.
        """.trimIndent()
    }

    // =========================================================================
    // 1. Fixture A: Full back-label Clony shampoo (Proper Vietnamese diacritics)
    // =========================================================================
    @Test
    fun testFixtureAClonyProperDiacritics() {
        val result = analyzer.analyze(
            productName = "Dầu gội Clony",
            rawLabelText = FIXTURE_A_CLONY_PROPER_DIACRITICS,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        val rawTokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(EXPECTED_CLONY_TOKENS, rawTokenTexts, "Parsed tokens must match exact 19 expected ingredients")

        // Sodium Laureth Sulfate must be ONE token (line wrap joined with space)
        assertTrue(rawTokenTexts.contains("Sodium Laureth Sulfate"), "Sodium Laureth Sulfate must be a single unbroken token")
        assertFalse(rawTokenTexts.contains("Sodium"), "Should not contain fragmented 'Sodium'")
        assertFalse(rawTokenTexts.contains("Laureth Sulfate"), "Should not contain fragmented 'Laureth Sulfate'")

        // Zero marketing / usage / storage / header tokens
        assertFalse(rawTokenTexts.any { it.contains("MƯỢT TÓC", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("KHÔNG CẦN DẦU XẢ", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Thế hệ mới", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Hướng dẫn sử dụng", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Thành phần", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Bảo quản", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Tránh nhiệt độ cao", ignoreCase = true) })

        // Check fallback category honesty in Vietnamese: "Chưa có trong cơ sở dữ liệu"
        val unmatchedTokens = result.fullBreakdown.filter { !it.isKnown }
        assertTrue(unmatchedTokens.isNotEmpty())
        for (item in unmatchedTokens) {
            assertEquals("Chưa có trong cơ sở dữ liệu", item.category)
            assertFalse(item.category.contains("phổ biến", ignoreCase = true))
            assertFalse(item.category.contains("Common ingredient", ignoreCase = true))
        }
    }

    // =========================================================================
    // 2. Fixture B: Same text with diacritic-stripped marker ("Thanh phan:")
    // =========================================================================
    @Test
    fun testFixtureBClonyStrippedMarker() {
        val result = analyzer.analyze(
            productName = "Dau goi Clony",
            rawLabelText = FIXTURE_B_CLONY_STRIPPED_MARKER,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val rawTokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(EXPECTED_CLONY_TOKENS, rawTokenTexts, "Diacritic-stripped marker must produce identical 19 tokens")

        // Check fallback category honesty in English: "Not in the database yet"
        val unmatchedTokens = result.fullBreakdown.filter { !it.isKnown }
        assertTrue(unmatchedTokens.isNotEmpty())
        for (item in unmatchedTokens) {
            assertEquals("Not in the database yet", item.category)
            assertFalse(item.category.contains("Common ingredient", ignoreCase = true))
            assertFalse(item.category.contains("Standard ingredient", ignoreCase = true))
        }
    }

    // =========================================================================
    // 3. Fixture C: Marker line removed entirely (Comma-dense paragraph fallback)
    // =========================================================================
    @Test
    fun testFixtureCNoMarkerCommaDenseFallback() {
        val result = analyzer.analyze(
            productName = "Clony Shampoo",
            rawLabelText = FIXTURE_C_NO_MARKER,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        val rawTokenTexts = result.fullBreakdown.map { it.rawName }
        // Comma density threshold (0.25) picks the 18-comma ingredients paragraph:
        assertEquals(EXPECTED_CLONY_TOKENS, rawTokenTexts, "No-marker fallback must select the comma-dense paragraph")
        // NEVER uses the whole text list
        assertFalse(rawTokenTexts.any { it.contains("MƯỢT TÓC", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Bảo quản", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Hướng dẫn", ignoreCase = true) })
    }

    // =========================================================================
    // 4. No-marker fallback: Branch 2 (No paragraph qualifies -> zero tokens -> NOT_ENOUGH_INFO)
    // =========================================================================
    @Test
    fun testNoMarkerFallbackBranch2NoQualifyingParagraph() {
        val proseOnlyText = """
            MƯỢT TÓC VÀ KHÔNG CẦN DẦU XẢ.
            Dầu gội thảo dược cao cấp nuôi dưỡng mái tóc suôn mượt và bồng bềnh tự nhiên suốt 24 giờ.
            Hướng dẫn sử dụng: Làm ướt tóc, thoa đều và mát xa nhẹ nhàng rồi xả sạch với nước.
            Bảo quản nơi khô ráo, tránh ánh sáng trực tiếp.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Prose Only Product",
            rawLabelText = proseOnlyText,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        assertTrue(result.fullBreakdown.isEmpty(), "When no paragraph meets comma density threshold, return zero tokens")
        // Existing Wave 2 profile fit yields NOT_ENOUGH_INFO
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    // =========================================================================
    // 5. Fixture D: English food label regression guard
    // =========================================================================
    @Test
    fun testFixtureDEnglishFoodRegressionGuard() {
        val result = analyzer.analyze(
            productName = "Oat Crunch",
            rawLabelText = FIXTURE_D_ENGLISH_FOOD,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val expectedTokens = listOf(
            "Whole Grain Oats",
            "Sugar",
            "Honey",
            "Canola Oil",
            "Sea Salt",
            "Molasses",
            "Tocopherols",
        )
        val rawTokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(expectedTokens, rawTokenTexts)
        assertFalse(rawTokenTexts.any { it.contains("CONTAINS", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Nutrition Facts", ignoreCase = true) })
        assertFalse(rawTokenTexts.any { it.contains("Calories", ignoreCase = true) })
    }

    // =========================================================================
    // 6. Deduplication case- and diacritic-insensitively
    // =========================================================================
    @Test
    fun testTokenDeduplicationCaseAndDiacriticInsensitive() {
        val textWithDuplicates = """
            Thành phần: Aqua, Sodium Laureth Sulfate, aqua, AQUA, Đậu nành, Dau nanh, DAU NANH, Fragrance.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Dup Test",
            rawLabelText = textWithDuplicates,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        val tokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(4, tokenTexts.size, "Duplicates must be eliminated: Aqua, Sodium Laureth Sulfate, Đậu nành, Fragrance")
        assertEquals(listOf("Aqua", "Sodium Laureth Sulfate", "Đậu nành", "Fragrance"), tokenTexts)
    }

    // =========================================================================
    // 7. Parenthesised groups kept intact & internal dots not mangled
    // =========================================================================
    @Test
    fun testParenthesisedGroupsKeptIntactAndDotsPreserved() {
        val textWithParens = """
            Ingredients: Water, Enriched Flour (Wheat Flour, Niacin, Reduced Iron, Thiamine Mononitrate), Vitamin E (dl-alpha Tocopheryl Acetate), Salt.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Paren Test",
            rawLabelText = textWithParens,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val tokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(
            listOf(
                "Water",
                "Enriched Flour (Wheat Flour, Niacin, Reduced Iron, Thiamine Mononitrate)",
                "Vitamin E (dl-alpha Tocopheryl Acetate)",
                "Salt",
            ),
            tokenTexts,
        )
    }

    // =========================================================================
    // 8. End-cutters: Expiry, Manufacturer, Address, Net Volume, Directions
    // =========================================================================
    @Test
    fun testExtendedEndCutters() {
        val textWithCutters = """
            Thành phần: Aqua, Glycerin, Niacinamide, Sodium Hyaluronate.
            Hạn sử dụng: 3 năm kể từ ngày sản xuất.
            Sản xuất tại: Công ty Dược Mỹ Phẩm ABC.
            Địa chỉ: 123 Đường Nguyễn Huệ, TP. HCM.
            Thể tích thực: 200 ml.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Lotion ABC",
            rawLabelText = textWithCutters,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        val tokenTexts = result.fullBreakdown.map { it.rawName }
        assertEquals(listOf("Aqua", "Glycerin", "Niacinamide", "Sodium Hyaluronate"), tokenTexts)
    }
}
