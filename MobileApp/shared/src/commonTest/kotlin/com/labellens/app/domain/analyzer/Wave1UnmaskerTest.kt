package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Wave1UnmaskerTest {

    private val analyzer = LabelAnalyzer()

    @Test
    fun testSyntheticLabelWithExactly4SweetenerAliasesReportsExactly4() {
        // Label with exactly 4 sweetener aliases: Sugar, Corn Syrup, Honey, Molasses
        val text = """
            INGREDIENTS: Whole Wheat Flour, Sugar, Corn Syrup, Honey, Molasses, Canola Oil.
            Nutrition Facts: Serving size 1 bar. Calories: 120. Total Sugars: 12g.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Sweet Bar",
            rawLabelText = text,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val rollUp = result.sweetenerRollUp
        assertNotNull(rollUp, "Must report sweetener roll-up")
        assertEquals(4, rollUp.count, "Must report exactly 4 sweetener aliases")
        assertEquals(4, rollUp.items.size)
        val names = rollUp.items.map { it.rawName.lowercase() }
        assertTrue(names.any { it.contains("sugar") })
        assertTrue(names.any { it.contains("corn syrup") })
        assertTrue(names.any { it.contains("honey") })
        assertTrue(names.any { it.contains("molasses") })
    }

    @Test
    fun testSyntheticLabelWithExactly3SodiumSourcesReportsExactly3() {
        // Label with exactly 3 sodium sources: Salt, Baking Soda, Monosodium Glutamate
        val text = """
            INGREDIENTS: Enriched Wheat Flour, Salt, Baking Soda, Monosodium Glutamate, Soybean Oil.
            Nutrition Facts: Serving size 1 serving. Calories: 150. Sodium: 420mg.
        """.trimIndent()

        val result = analyzer.analyze(
            productName = "Savory Cracker",
            rawLabelText = text,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val rollUp = result.sodiumRollUp
        assertNotNull(rollUp, "Must report sodium roll-up")
        assertEquals(3, rollUp.count, "Must report exactly 3 sodium sources")
        assertEquals(3, rollUp.items.size)
        val names = rollUp.items.map { it.rawName.lowercase() }
        assertTrue(names.any { it.contains("salt") })
        assertTrue(names.any { it.contains("baking soda") })
        assertTrue(names.any { it.contains("monosodium glutamate") })
    }

    @Test
    fun testFalsePositiveSuiteNutmegCocoaButterLacticAcidOatFlourUnsaltedButter() {
        // Must prove NO false positives on: nutmeg, cocoa butter, lactic acid, oat flour, unsalted butter
        val testTokens = listOf(
            "Nutmeg",
            "Cocoa Butter",
            "Lactic Acid",
            "Oat Flour",
            "Unsalted Butter",
        )

        for (token in testTokens) {
            val sweetMatch = KnowledgeClusters.matchSweetener(token)
            assertNull(sweetMatch, "Token '$token' must NOT match any sweetener alias")

            val sodiumMatch = KnowledgeClusters.matchSodiumSource(token)
            assertNull(sodiumMatch, "Token '$token' must NOT match any sodium source")
        }

        val text = "INGREDIENTS: Nutmeg, Cocoa Butter, Lactic Acid, Oat Flour, Unsalted Butter."
        val result = analyzer.analyze(
            productName = "Spiced Pastry",
            rawLabelText = text,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        assertNull(result.sweetenerRollUp, "Must have 0 sweetener aliases for false positive test label")
        assertNull(result.sodiumRollUp, "Must have 0 sodium sources for false positive test label")
    }

    @Test
    fun testCutLineDetectionPresentAbsentAndOcrSpaced() {
        // Case 1: Standard cut-line present (English and Vietnamese)
        val text1 = "INGREDIENTS: Whole Grain Oats, Sugar, Canola Oil. Contains 2% or less of: Salt, Natural Flavor."
        val result1En = analyzer.analyze("Oat Mix", text1, UserProfile(), language = AppLanguage.ENGLISH)
        assertNotNull(result1En.cutLineIndex)
        assertEquals(3, result1En.cutLineIndex) // Oats, Sugar, Canola Oil are before cut-line
        assertNotNull(result1En.cutLineLabel)
        assertEquals("Tiny amounts / Flavourings (≤ 2%)", result1En.cutLineLabel)
        assertFalse(result1En.fullBreakdown[0].isAfterCutLine)
        assertTrue(result1En.fullBreakdown[3].isAfterCutLine)

        val result1Vi = analyzer.analyze("Oat Mix", text1, UserProfile(), language = AppLanguage.VIETNAMESE)
        assertEquals(3, result1Vi.cutLineIndex)
        assertEquals("Hàm lượng rất nhỏ / Hương liệu (≤ 2%)", result1Vi.cutLineLabel)

        // Case 2: OCR spaced variant "contains 2 % or less of"
        val text2 = "INGREDIENTS: Flour, Water, Yeast. Contains  2  %   or   less   of: Salt, Enzyme."
        val result2 = analyzer.analyze("Bread", text2, UserProfile(), language = AppLanguage.ENGLISH)
        assertNotNull(result2.cutLineIndex)
        assertEquals(3, result2.cutLineIndex)

        // Case 3: "contains less than 2% of"
        val text3 = "INGREDIENTS: Potatoes, Vegetable Oil. Contains less than 2% of: Sea Salt."
        val result3 = analyzer.analyze("Chips", text3, UserProfile(), language = AppLanguage.ENGLISH)
        assertNotNull(result3.cutLineIndex)
        assertEquals(2, result3.cutLineIndex)

        // Case 4: Absent cut-line
        val text4 = "INGREDIENTS: Whole Grain Oats, Honey, Canola Oil, Sea Salt."
        val result4 = analyzer.analyze("Granola", text4, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(result4.cutLineIndex)
        assertNull(result4.cutLineLabel)
        assertTrue(result4.fullBreakdown.none { it.isAfterCutLine })
    }

    @Test
    fun testCatchAllBadgesAndInformationNotes() {
        val text = "INGREDIENTS: Rice, Natural Flavors, Spices, Canola Oil."
        val resultEn = analyzer.analyze("Rice Snack", text, UserProfile(), language = AppLanguage.ENGLISH)

        val naturalFlavor = resultEn.fullBreakdown.firstOrNull { it.rawName.contains("Natural Flavors", ignoreCase = true) }
        assertNotNull(naturalFlavor)
        assertTrue(naturalFlavor.isCatchAll)
        assertEquals("The label does not disclose which specific substances are inside this term.", naturalFlavor.catchAllNote)

        val spices = resultEn.fullBreakdown.firstOrNull { it.rawName.contains("Spices", ignoreCase = true) }
        assertNotNull(spices)
        assertTrue(spices.isCatchAll)

        // Vietnamese mode
        val resultVi = analyzer.analyze("Rice Snack", text, UserProfile(), language = AppLanguage.VIETNAMESE)
        val naturalFlavorVi = resultVi.fullBreakdown.firstOrNull { it.rawName.contains("Natural Flavors", ignoreCase = true) }
        assertNotNull(naturalFlavorVi)
        assertTrue(naturalFlavorVi.isCatchAll)
        assertEquals("Nhãn không tiết lộ các chất cụ thể nào được sử dụng trong danh mục chung này.", naturalFlavorVi.catchAllNote)
    }

    @Test
    fun testHaloPatternAEngineeredFibreHaloFiringAndNotFiring() {
        // Fire Case: Added sugar present + inulin (isolated fiber) present
        val fireText = """
            INGREDIENTS: Rolled Oats, Cane Sugar, Inulin (Chicory Root Fiber), Palm Oil.
            Nutrition Facts: Serving size 1 bar. Calories: 100. Includes 5g Added Sugars.
        """.trimIndent()
        val fireResult = analyzer.analyze("Fiber Bar", fireText, UserProfile(), language = AppLanguage.ENGLISH)
        val haloA = fireResult.worthNoting.firstOrNull { it.title == "Added Fiber & Sweeteners" }
        assertNotNull(haloA, "Halo Pattern A must fire when added sugar + isolated fiber are present")
        assertTrue(haloA.detail.contains("isolated ingredient"))
        assertFalse(haloA.detail.contains("toxic") || haloA.detail.contains("bad"))

        // Not-Fire Case 1: Inulin present, but zero added sugars and 0 sweetener aliases
        val noSugarText = """
            INGREDIENTS: Rolled Oats, Inulin, Water, Salt.
            Nutrition Facts: Serving size 1 bar. Calories: 80. Includes 0g Added Sugars.
        """.trimIndent()
        val noSugarResult = analyzer.analyze("Plain Fiber Bar", noSugarText, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(noSugarResult.worthNoting.firstOrNull { it.title == "Added Fiber & Sweeteners" })

        // Not-Fire Case 2: Sugar present, but no isolated fiber (only whole grain oats fiber)
        val noFiberText = """
            INGREDIENTS: Rolled Oats, Cane Sugar, Canola Oil, Salt.
            Nutrition Facts: Serving size 1 bar. Calories: 100. Includes 6g Added Sugars. Dietary Fiber: 2g.
        """.trimIndent()
        val noFiberResult = analyzer.analyze("Oat Bar", noFiberText, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(noFiberResult.worthNoting.firstOrNull { it.title == "Added Fiber & Sweeteners" })
    }

    @Test
    fun testHaloPatternBFruitInNameConcentrateInFactFiringAndNotFiring() {
        // Fire Case: Fruit in product name ("Strawberry Fruit Bites"), fruit in ingredients ONLY as fruit juice concentrate
        val fireText = """
            INGREDIENTS: Apple Juice Concentrate, Tapioca Syrup, Fruit Juice Concentrate (Strawberry), Pectin.
            Nutrition Facts: Serving size 1 pouch. Calories: 90. Total Sugars: 14g.
        """.trimIndent()
        val fireResult = analyzer.analyze("Strawberry Fruit Chews", fireText, UserProfile(), language = AppLanguage.ENGLISH)
        val haloB = fireResult.worthNoting.firstOrNull { it.title == "Fruit from Concentrate" }
        assertNotNull(haloB, "Halo Pattern B must fire when fruit-named product uses concentrate exclusively")
        assertEquals(
            "The fruit content in this product appears in the ingredient list as fruit juice concentrate rather than whole fruit.",
            haloB.detail,
        )

        // Vietnamese mode
        val fireResultVi = analyzer.analyze("Strawberry Fruit Chews", fireText, UserProfile(), language = AppLanguage.VIETNAMESE)
        val haloBVi = fireResultVi.worthNoting.firstOrNull { it.title == "Trái cây từ nước ép cô đặc" }
        assertNotNull(haloBVi)
        assertEquals(
            "Thành phần trái cây trong sản phẩm này xuất hiện dưới dạng nước ép cô đặc thay vì trái cây nguyên quả.",
            haloBVi.detail,
        )

        // Not-Fire Case 1: Product has real whole fruit / dried fruit
        val wholeFruitText = """
            INGREDIENTS: Whole Grain Oats, Dried Strawberries, Fresh Apples, Honey.
            Nutrition Facts: Serving size 1 cup. Calories: 150.
        """.trimIndent()
        val wholeResult = analyzer.analyze("Strawberry Apple Granola", wholeFruitText, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(wholeResult.worthNoting.firstOrNull { it.title == "Fruit from Concentrate" })

        // Not-Fire Case 2: Product has no fruit in name
        val nonFruitNameText = """
            INGREDIENTS: Corn Syrup, Apple Juice Concentrate, Gelatin.
            Nutrition Facts: Serving size 1 pouch. Calories: 80.
        """.trimIndent()
        val nonFruitResult = analyzer.analyze("Gummy Bears", nonFruitNameText, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(nonFruitResult.worthNoting.firstOrNull { it.title == "Fruit from Concentrate" })
    }

    @Test
    fun testMarqueeIngredientPositionalNoteAfterCutLineAndAfterSalt() {
        // Case 1: Marquee ingredient (Honey) appears AFTER 2% cut-line
        val cutLineText = "INGREDIENTS: Oats, Sugar, Canola Oil. Contains 2% or less of: Honey, Salt, Natural Flavor."
        val cutLineResult = analyzer.analyze("Honey Granola", cutLineText, UserProfile(), language = AppLanguage.ENGLISH)
        val marqueeNote1 = cutLineResult.worthNoting.firstOrNull { it.title == "Position on Label" }
        assertNotNull(marqueeNote1)
        assertTrue(marqueeNote1.detail.contains("Listed in the 2%-or-less section"))
        assertTrue(marqueeNote1.detail.contains("Honey"))

        // Case 2: Marquee ingredient appears AFTER salt when there is NO cut-line
        val noCutLineText = "INGREDIENTS: Whole Grain Oats, Brown Rice, Canola Oil, Salt, Olive Oil."
        val noCutLineResult = analyzer.analyze("Olive Oil Crisp", noCutLineText, UserProfile(), language = AppLanguage.ENGLISH)
        val marqueeNote2 = noCutLineResult.worthNoting.firstOrNull { it.title == "Position on Label" }
        assertNotNull(marqueeNote2)
        assertTrue(marqueeNote2.detail.contains("Listed after salt in the ingredient list"))
        assertTrue(marqueeNote2.detail.contains("Olive Oil"))

        // Case 3: Marquee ingredient appears BEFORE salt and before cut-line -> does NOT fire
        val beforeText = "INGREDIENTS: Olive Oil, Whole Grain Oats, Salt."
        val beforeResult = analyzer.analyze("Olive Oil Snack", beforeText, UserProfile(), language = AppLanguage.ENGLISH)
        assertNull(beforeResult.worthNoting.firstOrNull { it.title == "Position on Label" })
    }

    @Test
    fun testSample7FullIntegrationVerification() {
        val sample = BuiltInSamples.sample7
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = UserProfile(),
            category = sample.category,
            language = AppLanguage.ENGLISH,
        )

        // 1. Roll-up clusters
        assertNotNull(result.sweetenerRollUp)
        assertTrue(result.sweetenerRollUp!!.count >= 4, "Must detect Cane Sugar, Corn Syrup, Honey, Molasses")
        assertNotNull(result.sodiumRollUp)
        assertTrue(result.sodiumRollUp!!.count >= 2, "Must detect Salt and Baking Soda")

        // 2. Portion Math
        assertEquals(2.5, result.portionInfo.servingsPerContainer)
        assertEquals("1 bar (24g)", result.portionInfo.servingSize)
        assertTrue(result.portionInfo.hasMultipleServings)

        // 3. Cut-line
        assertNotNull(result.cutLineIndex)
        assertEquals("Tiny amounts / Flavourings (≤ 2%)", result.cutLineLabel)

        // 4. Badges & Halos
        val naturalFlavors = result.fullBreakdown.firstOrNull { it.rawName.contains("Natural Flavors") }
        assertNotNull(naturalFlavors)
        assertTrue(naturalFlavors.isCatchAll)

        val haloA = result.worthNoting.firstOrNull { it.title == "Added Fiber & Sweeteners" }
        assertNotNull(haloA, "Inulin + Added sugars must fire Halo A")

        val marqueeHoney = result.worthNoting.firstOrNull { it.title == "Position on Label" }
        assertNotNull(marqueeHoney, "Honey in 2% section must fire marquee note")
    }
}
