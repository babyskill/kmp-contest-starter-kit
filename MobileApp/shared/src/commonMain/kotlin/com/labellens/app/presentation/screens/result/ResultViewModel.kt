package com.labellens.app.presentation.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.data.source.remote.apiservices.LabelLensBackendAnalyzeService
import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeRequest
import com.labellens.app.data.source.remote.response.ai.BackendUserProfile
import com.labellens.app.domain.ai.AiAssistMergePolicy
import com.labellens.app.domain.ai.AiAssistTriggerPolicy
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.analyzer.PortionMath
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.PortionMode
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile
import com.labellens.app.root.AppConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val analysisResult: LabelAnalysisResult? = null,
    val isSaved: Boolean = false,
    val isDeletable: Boolean = false,
    val isFavorite: Boolean = false,
    val feedbackSubmitted: Boolean = false,
    val isLabelTextExpanded: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val language: AppLanguage = AppLanguage.DEFAULT,
    val portionMode: PortionMode = PortionMode.SINGLE_SERVING,
    val isFixDialogOpen: Boolean = false,
)

class ResultViewModel(
    private val scanRepository: ScanRepository,
    private val userProfileRepository: UserProfileRepository,
    private val analyzer: LabelAnalyzer,
    private val backendAnalyzeService: LabelLensBackendAnalyzeService? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    val isDeletable: Boolean get() = _uiState.value.isDeletable

    private var lastLoadedKey: String? = null

    fun loadAnalysis(
        scanId: String?,
        sampleId: String?,
        rawText: String?,
        productName: String?,
        barcode: String?,
        sourceName: String?,
    ) {
        val requestKey = "${scanId.orEmpty()}|${sampleId.orEmpty()}|${barcode.orEmpty()}|${productName.orEmpty()}|${rawText.orEmpty()}"
        if (lastLoadedKey == requestKey && _uiState.value.isSaved) {
            return
        }
        lastLoadedKey = requestKey

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val lang = userProfileRepository.getLanguage()
            _uiState.value = _uiState.value.copy(userProfile = profile, language = lang)

            if (!scanId.isNullOrBlank()) {
                val reanalyzed = scanRepository.getScanByIdReanalyzed(scanId, profile)
                if (reanalyzed != null) {
                    _uiState.value = _uiState.value.copy(
                        analysisResult = reanalyzed,
                        isSaved = true,
                        isDeletable = true,
                        portionMode = PortionMath.defaultPortionMode(reanalyzed.portionInfo.servingsPerContainer),
                    )
                    return@launch
                }
            }

            // If sampleId provided, match against BuiltInSamples
            if (!sampleId.isNullOrBlank()) {
                val sample = BuiltInSamples.all.firstOrNull { it.id == sampleId }
                if (sample != null) {
                    val analysis = analyzer.analyze(
                        productName = sample.name,
                        rawLabelText = sample.fullLabelText,
                        userProfile = profile,
                        category = sample.category,
                        scanSource = ScanSource.SAMPLE,
                        barcode = sample.barcode,
                        language = lang,
                    )
                    _uiState.value = _uiState.value.copy(
                        analysisResult = analysis,
                        isSaved = false,
                        isDeletable = false,
                        portionMode = PortionMath.defaultPortionMode(analysis.portionInfo.servingsPerContainer),
                    )
                    return@launch
                }
            }

            // Fallback: analyze rawText
            val isSampleFallback = rawText == null && sampleId == null && scanId == null
            val text = rawText ?: BuiltInSamples.sample2.fullLabelText
            val name = productName ?: if (lang == AppLanguage.VIETNAMESE) "Sản phẩm quét" else "Scanned Product"
            val category = if (text.contains("SUPPLEMENT", ignoreCase = true)) ProductCategory.SUPPLEMENT else ProductCategory.FOOD
            val src = when (sourceName) {
                "Open Food Facts" -> ScanSource.BARCODE

                else -> try {
                    ScanSource.valueOf(sourceName ?: "PHOTO")
                } catch (e: Exception) {
                    ScanSource.PHOTO
                }
            }

            val sourceAttr = if (sourceName == "Open Food Facts" || (barcode != null && sourceName != null)) {
                if (lang == AppLanguage.VIETNAMESE) "Nguồn: $sourceName" else "Source: $sourceName"
            } else {
                null
            }

            val analysis = analyzer.analyze(
                productName = name,
                rawLabelText = text,
                userProfile = profile,
                category = category,
                scanSource = src,
                barcode = barcode,
                sourceAttribution = sourceAttr,
                language = lang,
            )

            val finalAnalysis = if (isSampleFallback || src == ScanSource.SAMPLE) {
                analysis
            } else {
                val shouldTrigger = AiAssistTriggerPolicy.shouldTrigger(
                    isAiAssistEnabled = profile.aiAssistEnabled,
                    appKey = AppConfiguration.LABELLENS_APP_KEY,
                    isSample = false,
                    localResult = analysis,
                    rawOcrText = text,
                )
                if (shouldTrigger && backendAnalyzeService != null) {
                    val catString = if (category == ProductCategory.SUPPLEMENT) "supplement" else "food"
                    val userAllergensList = profile.allergies.map { it.displayName } + profile.customAllergens
                    val userFlagsList = profile.enabledAttentionFlags.map { it.displayName }
                    val req = BackendAnalyzeRequest(
                        ocrText = text,
                        barcode = barcode,
                        category = catString,
                        profile = BackendUserProfile(
                            allergens = userAllergensList.takeIf { it.isNotEmpty() },
                            flags = userFlagsList.takeIf { it.isNotEmpty() },
                        ),
                    )
                    val cloudResponse = try {
                        backendAnalyzeService.analyze(req, AppConfiguration.LABELLENS_APP_KEY)
                    } catch (e: Exception) {
                        null
                    }
                    if (cloudResponse != null) {
                        AiAssistMergePolicy.merge(analysis, cloudResponse, profile, lang)
                    } else {
                        analysis
                    }
                } else {
                    analysis
                }
            }

            if (isSampleFallback || src == ScanSource.SAMPLE) {
                _uiState.value = _uiState.value.copy(
                    analysisResult = finalAnalysis,
                    isSaved = false,
                    isDeletable = false,
                    portionMode = PortionMath.defaultPortionMode(finalAnalysis.portionInfo.servingsPerContainer),
                )
            } else {
                // Completed user scan result (camera photo, barcode lookup, pasted text) -> Auto-save!
                val savedId = scanRepository.saveScan(finalAnalysis)
                val persistedResult = finalAnalysis.copy(id = savedId)
                _uiState.value = _uiState.value.copy(
                    analysisResult = persistedResult,
                    isSaved = true,
                    isDeletable = true,
                    portionMode = PortionMath.defaultPortionMode(persistedResult.portionInfo.servingsPerContainer),
                )
            }
        }
    }

    fun openFixDialog() {
        _uiState.value = _uiState.value.copy(isFixDialogOpen = true)
    }

    fun dismissFixDialog() {
        _uiState.value = _uiState.value.copy(isFixDialogOpen = false)
    }

    /**
     * Correction loop (Scope 5):
     * Re-runs the SAME on-device analysis with edited text and updates the header immediately.
     */
    fun fixMisreadLabel(editedText: String) {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val lang = userProfileRepository.getLanguage()
            val reanalyzed = analyzer.analyze(
                productName = current.productName,
                rawLabelText = editedText,
                userProfile = profile,
                category = current.category,
                scanSource = current.scanSource,
                barcode = current.barcode,
                sourceAttribution = current.sourceAttribution,
                language = lang,
            )
            val finalResult = if (_uiState.value.isSaved) {
                val savedId = scanRepository.saveScan(reanalyzed.copy(id = current.id))
                reanalyzed.copy(id = savedId)
            } else {
                reanalyzed
            }
            _uiState.value = _uiState.value.copy(
                analysisResult = finalResult,
                isFixDialogOpen = false,
                portionMode = PortionMath.defaultPortionMode(finalResult.portionInfo.servingsPerContainer),
            )
        }
    }

    /**
     * Stores a personal exception for a non-allergen item and re-runs analysis immediately.
     */
    fun addPersonalException(ingredient: String) {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            userProfileRepository.addIngredientException(ingredient)
            val updatedProfile = userProfileRepository.getProfile()
            val lang = userProfileRepository.getLanguage()
            val reanalyzed = analyzer.analyze(
                productName = current.productName,
                rawLabelText = current.rawInputText,
                userProfile = updatedProfile,
                category = current.category,
                scanSource = current.scanSource,
                barcode = current.barcode,
                sourceAttribution = current.sourceAttribution,
                language = lang,
            )
            val finalResult = if (_uiState.value.isSaved) {
                val savedId = scanRepository.saveScan(reanalyzed.copy(id = current.id))
                reanalyzed.copy(id = savedId)
            } else {
                reanalyzed
            }
            _uiState.value = _uiState.value.copy(
                userProfile = updatedProfile,
                analysisResult = finalResult,
                portionMode = PortionMath.defaultPortionMode(finalResult.portionInfo.servingsPerContainer),
            )
        }
    }

    fun setPortionMode(mode: PortionMode) {
        _uiState.value = _uiState.value.copy(portionMode = mode)
    }

    fun toggleLabelTextExpansion() {
        _uiState.value = _uiState.value.copy(isLabelTextExpanded = !_uiState.value.isLabelTextExpanded)
    }

    fun saveToHistory() {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            val savedId = scanRepository.saveScan(current)
            _uiState.value = _uiState.value.copy(
                analysisResult = current.copy(id = savedId),
                isSaved = true,
                isDeletable = true,
            )
        }
    }

    fun deleteResult(onDeleted: (() -> Unit)? = null) {
        val current = _uiState.value.analysisResult ?: return
        val key = current.id ?: current.barcode ?: current.productName
        viewModelScope.launch {
            scanRepository.deleteScan(key)
            _uiState.value = _uiState.value.copy(
                isSaved = false,
                isDeletable = false,
            )
            onDeleted?.invoke()
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.analysisResult ?: return
        val newFav = !_uiState.value.isFavorite
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFavorite = newFav)
            val key = current.id ?: current.barcode
            key?.let { scanRepository.toggleFavorite(it, newFav) }
        }
    }

    fun submitFeedback(helpful: Boolean) {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(feedbackSubmitted = true)
            val key = current.id ?: current.barcode
            key?.let {
                scanRepository.setFeedback(it, if (helpful) "helpful" else "needs_fix")
            }
        }
    }
}
