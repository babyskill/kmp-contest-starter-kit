package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.PortionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortionMathTest {

    @Test
    fun testParsePortionInfoWithFractionalServings() {
        val labelText = """
            Nutrition Facts: 2.5 servings per container. Serving size 1 bar (24g).
            Calories: 100. Total Sugars 7g. Sodium: 75mg.
        """.trimIndent()

        val info = PortionMath.parsePortionInfo(labelText)
        assertEquals(2.5, info.servingsPerContainer)
        assertEquals("1 bar (24g)", info.servingSize)
        assertTrue(info.hasMultipleServings)
        assertTrue(PortionMath.shouldShowToggle(info.servingsPerContainer))
        assertEquals(PortionMode.WHOLE_PACK, PortionMath.defaultPortionMode(info.servingsPerContainer))
    }

    @Test
    fun testParsePortionInfoMissingServings() {
        val labelText = """
            Nutrition Facts: Serving size 1 cup (40g).
            Calories: 160. Total Sugars 9g. Sodium: 140mg.
        """.trimIndent()

        val info = PortionMath.parsePortionInfo(labelText)
        assertNull(info.servingsPerContainer)
        assertEquals("1 cup (40g)", info.servingSize)
        assertFalse(info.hasMultipleServings)
        assertFalse(PortionMath.shouldShowToggle(info.servingsPerContainer))
        assertEquals(PortionMode.SINGLE_SERVING, PortionMath.defaultPortionMode(info.servingsPerContainer))
    }

    @Test
    fun testDefaultPortionModeThresholds() {
        // Below 1.5 -> SINGLE_SERVING
        assertEquals(PortionMode.SINGLE_SERVING, PortionMath.defaultPortionMode(1.0))
        assertEquals(PortionMode.SINGLE_SERVING, PortionMath.defaultPortionMode(1.2))

        // Between 1.5 and 3.5 inclusive -> WHOLE_PACK
        assertEquals(PortionMode.WHOLE_PACK, PortionMath.defaultPortionMode(1.5))
        assertEquals(PortionMode.WHOLE_PACK, PortionMath.defaultPortionMode(2.0))
        assertEquals(PortionMode.WHOLE_PACK, PortionMath.defaultPortionMode(2.5))
        assertEquals(PortionMode.WHOLE_PACK, PortionMath.defaultPortionMode(3.5))

        // Greater than 3.5 -> SINGLE_SERVING
        assertEquals(PortionMode.SINGLE_SERVING, PortionMath.defaultPortionMode(4.0))
        assertEquals(PortionMode.SINGLE_SERVING, PortionMath.defaultPortionMode(8.0))
    }

    @Test
    fun testSugarTeaspoonMath() {
        // grams / 4.0, 1 decimal
        assertEquals(1.5, PortionMath.calculateSugarTeaspoons(6.0))
        assertEquals(2.3, PortionMath.calculateSugarTeaspoons(9.0))
        assertEquals(2.5, PortionMath.calculateSugarTeaspoons(10.0))
        assertEquals(6.0, PortionMath.calculateSugarTeaspoons(24.0))
        assertEquals(0.0, PortionMath.calculateSugarTeaspoons(0.0))
    }

    @Test
    fun testSodiumPercentDvAndSaltEquivalentMath() {
        // Reference: 2,300 mg daily reference. Salt eq: mg * 2.5 / 1000
        // 230 mg -> 10% DV, 0.6g salt eq
        assertEquals(10, PortionMath.calculateSodiumPercentDailyValue(230.0))
        assertEquals(0.6, PortionMath.calculateSaltEquivalentGrams(230.0))

        // 1150 mg -> 50% DV, 2.9g salt eq
        assertEquals(50, PortionMath.calculateSodiumPercentDailyValue(1150.0))
        assertEquals(2.9, PortionMath.calculateSaltEquivalentGrams(1150.0))

        // 2300 mg -> 100% DV, 5.8g salt eq
        assertEquals(100, PortionMath.calculateSodiumPercentDailyValue(2300.0))
        assertEquals(5.8, PortionMath.calculateSaltEquivalentGrams(2300.0))
    }

    @Test
    fun testScaleNutritionFactsWholePackVsSingleServing() {
        val facts = listOf(
            NutritionFact(name = "Calories", amount = "100 kcal"),
            NutritionFact(name = "Sugars", amount = "8 g"),
            NutritionFact(name = "Sodium", amount = "100 mg"),
        )

        // 1. Single serving mode (factor = 1.0)
        val single = PortionMath.scaleNutritionFacts(facts, PortionMode.SINGLE_SERVING, 2.5, AppLanguage.ENGLISH)
        assertEquals("100 kcal", single[0].amount)
        assertEquals("8 g", single[1].amount)
        assertEquals("2.0 tsp", single[1].secondaryDetail)
        assertEquals("100 mg", single[2].amount)
        assertEquals("4% DV • 0.3g salt eq", single[2].secondaryDetail)

        // 2. Whole pack mode (2.5 servings)
        val whole = PortionMath.scaleNutritionFacts(facts, PortionMode.WHOLE_PACK, 2.5, AppLanguage.ENGLISH)
        assertEquals("250 kcal", whole[0].amount)
        assertEquals("20 g", whole[1].amount)
        assertEquals("5.0 tsp", whole[1].secondaryDetail)
        assertEquals("250 mg", whole[2].amount)
        assertEquals("11% DV • 0.6g salt eq", whole[2].secondaryDetail)

        // 3. Vietnamese secondary detail
        val wholeVi = PortionMath.scaleNutritionFacts(facts, PortionMode.WHOLE_PACK, 2.5, AppLanguage.VIETNAMESE)
        assertTrue(wholeVi[1].secondaryDetail!!.contains("muỗng cà phê đường"))
        assertTrue(wholeVi[2].secondaryDetail!!.contains("Nhu cầu hằng ngày"))
        assertTrue(wholeVi[2].secondaryDetail!!.contains("muối quy đổi"))
    }
}
