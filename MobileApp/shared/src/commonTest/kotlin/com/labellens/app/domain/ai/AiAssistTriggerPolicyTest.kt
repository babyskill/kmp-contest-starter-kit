package com.labellens.app.domain.ai

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.FitHeaderInfo
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.ScanSource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiAssistTriggerPolicyTest {

    private fun createBaseResult(
        state: ProfileFitState = ProfileFitState.NO_MATCHES,
        breakdownCount: Int = 5,
        hasNutrition: Boolean = true,
        scanSource: ScanSource = ScanSource.PHOTO,
    ): LabelAnalysisResult {
        val breakdown = (1..breakdownCount).map {
            IngredientBreakdown(
                rawName = "Ingredient $it",
                canonicalName = "Ingredient $it",
                category = "General",
                plainExplanation = "Test",
                whyUsed = "",
                doseContextNote = "",
            )
        }
        val nutrition = if (hasNutrition) {
            listOf(NutritionFact(name = "Calories", amount = "100 kcal"))
        } else {
            emptyList()
        }
        return LabelAnalysisResult(
            productName = "Test Product",
            category = ProductCategory.FOOD,
            scanSource = scanSource,
            verdict = "Safe",
            fullBreakdown = breakdown,
            nutritionHighlights = nutrition,
            rawInputText = "Water, Sugar, Salt",
            fitHeader = FitHeaderInfo(
                state = state,
                title = "Fit Header",
                subtitle = "Subtitle",
            ),
        )
    }

    @Test
    fun `toggle off disables AI assist regardless of triggers`() {
        val result = createBaseResult(state = ProfileFitState.NOT_ENOUGH_INFO)
        val shouldTrigger = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = false,
            appKey = "test-key",
            isSample = false,
            localResult = result,
            rawOcrText = "some long text",
        )
        assertFalse(shouldTrigger)
    }

    @Test
    fun `blank app key disables AI assist`() {
        val result = createBaseResult(state = ProfileFitState.NOT_ENOUGH_INFO)
        val shouldTrigger = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "",
            isSample = false,
            localResult = result,
            rawOcrText = "some long text",
        )
        assertFalse(shouldTrigger)
    }

    @Test
    fun `built in samples are never sent to cloud AI`() {
        val result = createBaseResult(state = ProfileFitState.NOT_ENOUGH_INFO, scanSource = ScanSource.SAMPLE)
        val shouldTriggerSampleFlag = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "test-key",
            isSample = true,
            localResult = result,
            rawOcrText = "Nutrition Facts Calories 100",
        )
        assertFalse(shouldTriggerSampleFlag)

        val shouldTriggerScanSourceSample = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "test-key",
            isSample = false,
            localResult = result.copy(scanSource = ScanSource.SAMPLE),
            rawOcrText = "Nutrition Facts Calories 100",
        )
        assertFalse(shouldTriggerScanSourceSample)
    }

    @Test
    fun `trigger a - fit state NOT_ENOUGH_INFO triggers AI assist`() {
        val result = createBaseResult(state = ProfileFitState.NOT_ENOUGH_INFO)
        val shouldTrigger = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "test-key",
            isSample = false,
            localResult = result,
            rawOcrText = "Short text",
        )
        assertTrue(shouldTrigger)
    }

    @Test
    fun `trigger b - fewer than 3 ingredients with text longer than 200 chars triggers assist`() {
        val longOcrText = "A".repeat(250)
        val result = createBaseResult(breakdownCount = 2, hasNutrition = true)
        val shouldTrigger = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "test-key",
            isSample = false,
            localResult = result,
            rawOcrText = longOcrText,
        )
        assertTrue(shouldTrigger)

        // 3 or more ingredients does NOT trigger (b)
        val resultWith3 = createBaseResult(breakdownCount = 3, hasNutrition = true)
        val shouldNotTrigger = AiAssistTriggerPolicy.shouldTrigger(
            isAiAssistEnabled = true,
            appKey = "test-key",
            isSample = false,
            localResult = resultWith3,
            rawOcrText = longOcrText,
        )
        assertFalse(shouldNotTrigger)
    }

    @Test
    fun `trigger c - no nutrition facts parsed while text has nutrition markers in EN and VI`() {
        val resultNoNutrition = createBaseResult(hasNutrition = false)

        val enText = "Ingredients: Oats, Honey. Serving size: 30g. Calories: 150."
        assertTrue(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = resultNoNutrition,
                rawOcrText = enText,
            ),
        )

        val viText = "Thành phần: Yến mạch, Mật ong. Thông tin dinh dưỡng: Năng lượng 150 kcal."
        assertTrue(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = resultNoNutrition,
                rawOcrText = viText,
            ),
        )

        // If nutrition facts were already parsed locally, trigger (c) does not fire
        val resultWithNutrition = createBaseResult(hasNutrition = true)
        assertFalse(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = resultWithNutrition,
                rawOcrText = enText,
            ),
        )
    }

    @Test
    fun `trigger d - non-Latin script detected in OCR text`() {
        val result = createBaseResult(hasNutrition = true)

        val cjkText = "原材料名: 小麦粉、砂糖、植物油脂、食塩"
        assertTrue(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = result,
                rawOcrText = cjkText,
            ),
        )

        val koreanText = "원재료명: 밀가루, 백설탕, 팜유"
        assertTrue(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = result,
                rawOcrText = koreanText,
            ),
        )

        val cyrillicText = "Состав: мука пшеничная, сахар, соль"
        assertTrue(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = result,
                rawOcrText = cyrillicText,
            ),
        )

        val plainLatinText = "Ingredients: Wheat flour, sugar, salt, vegetable oil."
        assertFalse(
            AiAssistTriggerPolicy.shouldTrigger(
                isAiAssistEnabled = true,
                appKey = "test-key",
                isSample = false,
                localResult = result,
                rawOcrText = plainLatinText,
            ),
        )
    }
}
