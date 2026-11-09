package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalComparisonHelperTest {

    @Test
    fun `sugarTeaspoons computes correctly for 0g in both locales`() {
        assertEquals("0 thìa cà phê", LocalComparisonHelper.sugarTeaspoons(0.0, AppLanguage.VIETNAMESE))
        assertEquals("0 teaspoons of sugar", LocalComparisonHelper.sugarTeaspoons(0.0, AppLanguage.ENGLISH))
    }

    @Test
    fun `sugarTeaspoons computes 0_5 step rounding for non-zero sugar`() {
        // 6g / 4 = 1.5 teaspoons
        assertEquals("≈ 1,5 thìa cà phê đường", LocalComparisonHelper.sugarTeaspoons(6.0, AppLanguage.VIETNAMESE))
        assertEquals("≈ 1.5 teaspoons of sugar", LocalComparisonHelper.sugarTeaspoons(6.0, AppLanguage.ENGLISH))

        // 4g / 4 = 1.0 teaspoon
        assertEquals("≈ 1 thìa cà phê đường", LocalComparisonHelper.sugarTeaspoons(4.0, AppLanguage.VIETNAMESE))
        assertEquals("≈ 1 teaspoon of sugar", LocalComparisonHelper.sugarTeaspoons(4.0, AppLanguage.ENGLISH))

        // 7g / 4 = 1.75 -> rounded to 2.0
        assertEquals("≈ 2 thìa cà phê đường", LocalComparisonHelper.sugarTeaspoons(7.0, AppLanguage.VIETNAMESE))
    }

    @Test
    fun `sodiumDailyPercent computes percentage relative to 2300mg`() {
        // 42mg / 2300mg = 1.8% -> 2%
        val (textVi, pctVi) = LocalComparisonHelper.sodiumDailyPercent(42.0, AppLanguage.VIETNAMESE)
        assertEquals(2, pctVi)
        assertEquals("≈ 2% nhu cầu/ngày", textVi)

        val (textEn, pctEn) = LocalComparisonHelper.sodiumDailyPercent(42.0, AppLanguage.ENGLISH)
        assertEquals(2, pctEn)
        assertEquals("≈ 2% of daily needs", textEn)
    }

    @Test
    fun `fixed reference table matches honest reference comparisons`() {
        // Calcium ~120mg
        val calVi = LocalComparisonHelper.fixedReference("Canxi", "104 mg", AppLanguage.VIETNAMESE)
        assertEquals("≈ nửa ly sữa", calVi)
        val calEn = LocalComparisonHelper.fixedReference("Calcium", "104 mg", AppLanguage.ENGLISH)
        assertEquals("≈ half a glass of milk", calEn)

        // Protein ~3g
        val proVi = LocalComparisonHelper.fixedReference("Chất đạm", "3.0 g", AppLanguage.VIETNAMESE)
        assertEquals("≈ nửa hũ sữa chua", proVi)
        val proEn = LocalComparisonHelper.fixedReference("Protein", "3.0 g", AppLanguage.ENGLISH)
        assertEquals("≈ half a cup of yogurt", proEn)

        // Unknown nutrient returns null (no invention)
        val unknown = LocalComparisonHelper.fixedReference("Vitamin K", "10 mcg", AppLanguage.VIETNAMESE)
        assertNull(unknown)
    }

    @Test
    fun `parsePercentage extracts number and caps between 0 and 100`() {
        assertEquals(2, LocalComparisonHelper.parsePercentage("≈ 2% nhu cầu/ngày"))
        assertEquals(15, LocalComparisonHelper.parsePercentage("15% DV"))
        assertEquals(100, LocalComparisonHelper.parsePercentage("150% daily value"))
        assertNull(LocalComparisonHelper.parsePercentage("0 thìa cà phê"))
    }
}
