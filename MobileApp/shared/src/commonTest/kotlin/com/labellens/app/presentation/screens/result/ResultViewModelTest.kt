@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.labellens.app.presentation.screens.result

import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ResultViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()
    private lateinit var fakeScanRepository: FakeScanRepository
    private lateinit var fakeUserProfileRepository: FakeUserProfileRepository
    private lateinit var analyzer: LabelAnalyzer
    private lateinit var viewModel: ResultViewModel

    private class FakeScanRepository : ScanRepository {
        val savedScans = mutableListOf<LabelAnalysisResult>()
        var saveScanCallCount = 0

        override fun getAllScans(): Flow<List<LabelAnalysisResult>> = flowOf(savedScans.toList())
        override fun getRecentScans(limit: Int): Flow<List<LabelAnalysisResult>> = flowOf(savedScans.take(limit))
        override fun getFavoriteScans(): Flow<List<LabelAnalysisResult>> = flowOf(emptyList())

        override suspend fun getScanById(id: String): LabelAnalysisResult? = savedScans.firstOrNull { it.id == id || it.barcode == id }

        override suspend fun getScanByIdReanalyzed(id: String, profile: UserProfile): LabelAnalysisResult? = savedScans.firstOrNull { it.id == id || it.barcode == id }

        override suspend fun saveScan(result: LabelAnalysisResult): String {
            saveScanCallCount++
            val existingIndex = savedScans.indexOfFirst {
                (!result.id.isNullOrBlank() && it.id == result.id) ||
                    (!result.barcode.isNullOrBlank() && it.barcode == result.barcode) ||
                    it.rawInputText == result.rawInputText
            }
            return if (existingIndex >= 0) {
                val existing = savedScans[existingIndex]
                savedScans[existingIndex] = result.copy(id = existing.id)
                existing.id ?: "saved_id_$saveScanCallCount"
            } else {
                val newId = result.id ?: result.barcode ?: "saved_id_$saveScanCallCount"
                savedScans.add(result.copy(id = newId))
                newId
            }
        }

        override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {}
        override suspend fun setFeedback(id: String, feedback: String) {}
        override suspend fun deleteScan(id: String) {
            savedScans.removeAll { it.id == id || it.barcode == id }
        }
        override suspend fun getPhotoAnalysisCount(): Int = 0
        override suspend fun incrementPhotoAnalysisCount() {}
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        private var profile = UserProfile()
        private var language = AppLanguage.DEFAULT

        override val profileFlow: Flow<UserProfile> = flowOf(profile)
        override val languageFlow: StateFlow<AppLanguage> = MutableStateFlow(language)
        override suspend fun getProfile(): UserProfile = profile
        override suspend fun getLanguage(): AppLanguage = language
        override suspend fun setLanguage(language: AppLanguage) {
            this.language = language
        }
        override suspend fun updateAllergies(allergies: Set<Allergen>) {}
        override suspend fun updateAvoidList(avoidList: Set<AvoidPreference>) {}
        override suspend fun updateGoals(goals: Set<DietaryGoal>) {}
        override suspend fun updateMarket(market: Market) {}
        override suspend fun updateImportedFoodMode(imported: Boolean) {}
        override suspend fun updateCustomAllergens(customAllergens: List<String>) {}
        override suspend fun updateAttentionFlags(flags: Set<AttentionFlag>) {}
        override suspend fun addIngredientException(ingredient: String) {}
        override suspend fun removeIngredientException(ingredient: String) {}
        override suspend fun updateProfile(profile: UserProfile) {
            this.profile = profile
        }
        override suspend fun isConsentGiven(): Boolean = true
        override suspend fun setConsentGiven(consent: Boolean) {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        fakeScanRepository = FakeScanRepository()
        fakeUserProfileRepository = FakeUserProfileRepository()
        analyzer = LabelAnalyzer()
        viewModel = ResultViewModel(
            scanRepository = fakeScanRepository,
            userProfileRepository = fakeUserProfileRepository,
            analyzer = analyzer,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `auto-save happens once for camera photo result and sets isSaved to true`() = runTest {
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Almonds, Sea Salt",
            productName = "Almond Butter",
            barcode = null,
            sourceName = "PHOTO",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertTrue(state.isSaved, "Screen saved-state must reflect auto-save (isSaved=true)")
        assertEquals(1, fakeScanRepository.saveScanCallCount, "Auto-save must happen exactly once")
        assertEquals(1, fakeScanRepository.savedScans.size, "Repository must contain 1 saved entry")
        assertEquals("Almond Butter", fakeScanRepository.savedScans[0].productName)
        assertEquals("Ingredients: Almonds, Sea Salt", fakeScanRepository.savedScans[0].rawInputText)
    }

    @Test
    fun `re-entry and recomposition does not duplicate entries in history`() = runTest {
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Roasted Peanuts, Salt",
            productName = "Peanut Butter",
            barcode = null,
            sourceName = "PHOTO",
        )
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.savedScans.size)
        val initialCallCount = fakeScanRepository.saveScanCallCount

        // Re-display same result (e.g. recomposition, navigating back/forth)
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Roasted Peanuts, Salt",
            productName = "Peanut Butter",
            barcode = null,
            sourceName = "PHOTO",
        )
        advanceUntilIdle()

        assertEquals(initialCallCount, fakeScanRepository.saveScanCallCount, "Re-displaying same result must not trigger second save")
        assertEquals(1, fakeScanRepository.savedScans.size, "Repository size must remain 1 without duplicates")
    }

    @Test
    fun `opening an item from History does not create a new entry`() = runTest {
        // Pre-populate repository with an existing scan
        val initialResult = analyzer.analyze(
            productName = "Organic Honey",
            rawLabelText = "Ingredients: 100% Pure Raw Honey",
            userProfile = UserProfile(),
        )
        val scanId = fakeScanRepository.saveScan(initialResult)
        assertEquals(1, fakeScanRepository.savedScans.size)
        val baselineCallCount = fakeScanRepository.saveScanCallCount

        // Load item FROM history using scanId
        viewModel.loadAnalysis(
            scanId = scanId,
            sampleId = null,
            rawText = "Ingredients: 100% Pure Raw Honey",
            productName = "Organic Honey",
            barcode = null,
            sourceName = null,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertTrue(state.isSaved, "History item must show as already saved")
        assertEquals(baselineCallCount, fakeScanRepository.saveScanCallCount, "Opening from history must NOT call saveScan")
        assertEquals(1, fakeScanRepository.savedScans.size, "Opening from history must not create duplicate entry")
    }

    @Test
    fun `built-in sample previews are not saved to history`() = runTest {
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = com.labellens.app.domain.model.BuiltInSamples.sample1.id,
            rawText = null,
            productName = null,
            barcode = null,
            sourceName = null,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertFalse(state.isSaved, "Sample preview must have isSaved=false")
        assertEquals(0, fakeScanRepository.saveScanCallCount, "Samples must never be auto-saved")
        assertEquals(0, fakeScanRepository.savedScans.size, "Repository must be empty for sample previews")
    }

    @Test
    fun `barcode scan result is auto-saved once and idempotent`() = runTest {
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Whole Milk, Vitamin D3",
            productName = "Organic Whole Milk",
            barcode = "012345678901",
            sourceName = "Open Food Facts",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertTrue(state.isSaved)
        assertEquals(1, fakeScanRepository.savedScans.size)
        assertEquals("012345678901", fakeScanRepository.savedScans[0].barcode)

        // Load again with same barcode
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Whole Milk, Vitamin D3",
            productName = "Organic Whole Milk",
            barcode = "012345678901",
            sourceName = "Open Food Facts",
        )
        advanceUntilIdle()

        assertEquals(1, fakeScanRepository.savedScans.size)
    }

    @Test
    fun `pasted text scan result is auto-saved once`() = runTest {
        viewModel.loadAnalysis(
            scanId = null,
            sampleId = null,
            rawText = "Ingredients: Rolled Oats, Cane Sugar, Canola Oil",
            productName = "Custom Label Text",
            barcode = null,
            sourceName = null,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertTrue(state.isSaved)
        assertEquals(1, fakeScanRepository.savedScans.size)
        assertEquals("Custom Label Text", fakeScanRepository.savedScans[0].productName)
    }
}
