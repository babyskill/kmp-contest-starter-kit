package com.labellens.app.domain.ai

import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeData
import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeResponse
import com.labellens.app.data.source.remote.response.ai.BackendSections
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.FitHeaderInfo
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiAssistMergePolicyTest {

    private fun createLocalResult(
        watchOut: List<FlaggedItem> = emptyList(),
        worthNoting: List<FlaggedItem> = emptyList(),
        goodToKnow: List<FlaggedItem> = emptyList(),
        breakdown: List<IngredientBreakdown> = emptyList(),
        fitState: ProfileFitState = ProfileFitState.NOT_ENOUGH_INFO,
    ): LabelAnalysisResult = LabelAnalysisResult(
        productName = "Test Product",
        category = ProductCategory.FOOD,
        scanSource = ScanSource.PHOTO,
        verdict = "Verdict",
        watchOutForYou = watchOut,
        worthNoting = worthNoting,
        goodToKnow = goodToKnow,
        fullBreakdown = breakdown,
        rawInputText = "Raw label text",
        fitHeader = FitHeaderInfo(
            state = fitState,
            title = "Initial Title",
            subtitle = "Initial Subtitle",
        ),
    )

    private fun createCloudResponse(
        watchOut: List<String> = emptyList(),
        worthNoting: List<String> = emptyList(),
        goodToKnow: List<String> = emptyList(),
        tier: Int = 2,
        model: String = "llama-3.1-70b",
    ): BackendAnalyzeResponse = BackendAnalyzeResponse(
        tierUsed = tier,
        model = model,
        data = BackendAnalyzeData(
            sections = BackendSections.fromStrings(
                goodToKnow = goodToKnow,
                worthNoting = worthNoting,
                watchOut = watchOut,
            ),
        ),
    )

    @Test
    fun `hard invariant 1 & 2 - local items and personal allergen warnings are never removed`() {
        val localAllergen = FlaggedItem(
            title = "Contains Peanut",
            detail = "Direct allergen alert",
            severity = FlagSeverity.PERSONAL_ALLERGEN,
            verbatimSourceLine = "peanut oil",
        )
        val localBreakdown = IngredientBreakdown(
            rawName = "peanut oil",
            canonicalName = "Peanut Oil",
            category = "Fats & Oils",
            plainExplanation = "Cold pressed oil",
            whyUsed = "",
            doseContextNote = "",
        )
        val local = createLocalResult(
            watchOut = listOf(localAllergen),
            breakdown = listOf(localBreakdown),
            fitState = ProfileFitState.CONTAINS_YOURS,
        )

        val cloud = createCloudResponse(
            watchOut = listOf("Added Palm Oil"),
            goodToKnow = listOf("Organic Whole Wheat"),
        )

        val merged = AiAssistMergePolicy.merge(
            localResult = local,
            cloudResponse = cloud,
            userProfile = UserProfile(allergies = setOf(Allergen.PEANUT)),
            language = AppLanguage.ENGLISH,
        )

        // Local items still present
        assertTrue(merged.watchOutForYou.contains(localAllergen))
        assertTrue(merged.fullBreakdown.contains(localBreakdown))
        // Cloud items also added
        assertTrue(merged.watchOutForYou.any { it.title == "Added Palm Oil" })
        assertTrue(merged.goodToKnow.any { it.title == "Organic Whole Wheat" })
    }

    @Test
    fun `hard invariant 3 - normalized duplicates are deduped case and diacritic insensitively`() {
        val localItem = FlaggedItem(
            title = "Dầu cọ",
            detail = "Local note",
            severity = FlagSeverity.WORTH_NOTING,
        )
        val local = createLocalResult(worthNoting = listOf(localItem))

        // Cloud sends "dau co" without diacritics and different case
        val cloud = createCloudResponse(
            worthNoting = listOf("Dau Co", "High Fructose Corn Syrup"),
        )

        val merged = AiAssistMergePolicy.merge(
            localResult = local,
            cloudResponse = cloud,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        // "Dau Co" should be deduplicated against "Dầu cọ"
        val palmOilOccurrences = merged.worthNoting.filter {
            AiAssistMergePolicy.normalize(it.title).contains("dau co")
        }
        assertEquals(1, palmOilOccurrences.size)
        // High Fructose Corn Syrup added
        assertTrue(merged.worthNoting.any { it.title == "High Fructose Corn Syrup" })
    }

    @Test
    fun `hard invariant 4 - cloud items carry no verbatim line and have isAiGenerated true`() {
        val local = createLocalResult()
        val cloud = createCloudResponse(
            watchOut = listOf("Soy lecithin"),
            goodToKnow = listOf("Vitamin D3"),
        )

        val merged = AiAssistMergePolicy.merge(
            localResult = local,
            cloudResponse = cloud,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val cloudSoy = merged.watchOutForYou.first { it.title == "Soy lecithin" }
        assertNull(cloudSoy.verbatimSourceLine)
        assertTrue(cloudSoy.isAiGenerated)

        val cloudVit = merged.goodToKnow.first { it.title == "Vitamin D3" }
        assertNull(cloudVit.verbatimSourceLine)
        assertTrue(cloudVit.isAiGenerated)

        val cloudBreakdown = merged.fullBreakdown.first { it.canonicalName == "Soy lecithin" }
        assertTrue(cloudBreakdown.isAiGenerated)
    }

    @Test
    fun `hard invariant 5 - fit header state moves UP if cloud matches allergen, never down`() {
        val localNotEnoughInfo = createLocalResult(fitState = ProfileFitState.NOT_ENOUGH_INFO)
        val cloudWithPeanut = createCloudResponse(watchOut = listOf("Contains Peanut Butter"))

        val userWithPeanut = UserProfile(allergies = setOf(Allergen.PEANUT))

        // State moves UP from NOT_ENOUGH_INFO to CONTAINS_YOURS
        val mergedUp = AiAssistMergePolicy.merge(
            localResult = localNotEnoughInfo,
            cloudResponse = cloudWithPeanut,
            userProfile = userWithPeanut,
            language = AppLanguage.ENGLISH,
        )
        assertEquals(ProfileFitState.CONTAINS_YOURS, mergedUp.fitHeader?.state)

        // State NEVER moves down: if local was already CONTAINS_YOURS, cloud with no allergens doesn't downgrade it
        val localContainsYours = createLocalResult(fitState = ProfileFitState.CONTAINS_YOURS)
        val cloudClean = createCloudResponse(goodToKnow = listOf("Purified Water"))
        val mergedNoDowngrade = AiAssistMergePolicy.merge(
            localResult = localContainsYours,
            cloudResponse = cloudClean,
            userProfile = userWithPeanut,
            language = AppLanguage.ENGLISH,
        )
        assertEquals(ProfileFitState.CONTAINS_YOURS, mergedNoDowngrade.fitHeader?.state)
    }

    @Test
    fun `hard invariant 6 - source line under product card indicates AI assist model and tier`() {
        val local = createLocalResult()
        val cloud = createCloudResponse(
            worthNoting = listOf("Artificial Sweetener (Sucralose)"),
            tier = 2,
            model = "cf-meta-llama-3-8b",
        )

        val mergedEn = AiAssistMergePolicy.merge(
            localResult = local,
            cloudResponse = cloud,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )
        assertEquals("AI assist - Tier 2 - cf-meta-llama-3-8b", mergedEn.sourceAttribution)

        val mergedVi = AiAssistMergePolicy.merge(
            localResult = local,
            cloudResponse = cloud,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )
        assertEquals("Trợ lý AI - Tier 2 - cf-meta-llama-3-8b", mergedVi.sourceAttribution)
    }
}
