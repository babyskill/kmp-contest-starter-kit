package com.labellens.app.presentation.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.analyzer.PortionMath
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.PortionMode
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val analysisResult: LabelAnalysisResult? = null,
    val isSaved: Boolean = false,
    val isFavorite: Boolean = false,
    val feedbackSubmitted: Boolean = false,
    val isLabelTextExpanded: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val language: AppLanguage = AppLanguage.DEFAULT,
    val portionMode: PortionMode = PortionMode.SINGLE_SERVING,
)

class ResultViewModel(
    private val scanRepository: ScanRepository,
    private val userProfileRepository: UserProfileRepository,
    private val analyzer: LabelAnalyzer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadAnalysis(
        scanId: String?,
        sampleId: String?,
        rawText: String?,
        productName: String?,
        barcode: String?,
        sourceName: String?,
    ) {
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
                        portionMode = PortionMath.defaultPortionMode(analysis.portionInfo.servingsPerContainer),
                    )
                    return@launch
                }
            }

            // Fallback: analyze rawText
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
            _uiState.value = _uiState.value.copy(
                analysisResult = analysis,
                portionMode = PortionMath.defaultPortionMode(analysis.portionInfo.servingsPerContainer),
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
            scanRepository.saveScan(current)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.analysisResult ?: return
        val newFav = !_uiState.value.isFavorite
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFavorite = newFav)
            current.barcode?.let { scanRepository.toggleFavorite(it, newFav) }
        }
    }

    fun submitFeedback(helpful: Boolean) {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(feedbackSubmitted = true)
            current.barcode?.let {
                scanRepository.setFeedback(it, if (helpful) "helpful" else "needs_fix")
            }
        }
    }
}
