package com.labellens.app.presentation.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.data.repository.UserProfileRepository
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
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
            _uiState.value = _uiState.value.copy(userProfile = profile)

            if (!scanId.isNullOrBlank()) {
                val reanalyzed = scanRepository.getScanByIdReanalyzed(scanId, profile)
                if (reanalyzed != null) {
                    _uiState.value = _uiState.value.copy(
                        analysisResult = reanalyzed,
                        isSaved = true,
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
                    )
                    _uiState.value = _uiState.value.copy(analysisResult = analysis)
                    return@launch
                }
            }

            // Fallback: analyze rawText
            val text = rawText ?: BuiltInSamples.sample2.fullLabelText
            val name = productName ?: "Scanned Product"
            val category = if (text.contains("SUPPLEMENT", ignoreCase = true)) ProductCategory.SUPPLEMENT else ProductCategory.FOOD
            val src = try {
                ScanSource.valueOf(sourceName ?: "PHOTO")
            } catch (e: Exception) {
                ScanSource.PHOTO
            }

            val analysis = analyzer.analyze(
                productName = name,
                rawLabelText = text,
                userProfile = profile,
                category = category,
                scanSource = src,
                barcode = barcode,
            )
            _uiState.value = _uiState.value.copy(analysisResult = analysis)
        }
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
            val id = current.barcode ?: current.productName
            scanRepository.toggleFavorite(id, newFav)
            _uiState.value = _uiState.value.copy(isFavorite = newFav)
        }
    }

    fun submitFeedback(isAccurate: Boolean) {
        val current = _uiState.value.analysisResult ?: return
        viewModelScope.launch {
            val feedback = if (isAccurate) "accurate" else "inaccurate"
            val id = current.barcode ?: current.productName
            scanRepository.setFeedback(id, feedback)
            _uiState.value = _uiState.value.copy(feedbackSubmitted = true)
        }
    }
}
