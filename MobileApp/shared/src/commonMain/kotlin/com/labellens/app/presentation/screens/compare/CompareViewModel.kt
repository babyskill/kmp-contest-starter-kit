package com.labellens.app.presentation.screens.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompareUiState(
    val availableItems: List<LabelAnalysisResult> = emptyList(),
    val firstItem: LabelAnalysisResult? = null,
    val secondItem: LabelAnalysisResult? = null,
)

class CompareViewModel(
    private val scanRepository: ScanRepository,
    private val analyzer: LabelAnalyzer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        loadAvailableItems()
    }

    private fun loadAvailableItems() {
        viewModelScope.launch {
            // Default sample items
            val sampleResults = BuiltInSamples.all.map { s ->
                analyzer.analyze(
                    productName = s.name,
                    rawLabelText = s.fullLabelText,
                    userProfile = UserProfile(),
                    category = s.category,
                    scanSource = ScanSource.SAMPLE,
                    barcode = s.barcode,
                )
            }

            scanRepository.getAllScans().collect { savedList ->
                val combined = (savedList + sampleResults).distinctBy { it.productName }
                _uiState.value = _uiState.value.copy(
                    availableItems = combined,
                    firstItem = _uiState.value.firstItem ?: combined.getOrNull(0),
                    secondItem = _uiState.value.secondItem ?: combined.getOrNull(1),
                )
            }
        }
    }

    fun selectFirst(item: LabelAnalysisResult) {
        _uiState.value = _uiState.value.copy(firstItem = item)
    }

    fun selectSecond(item: LabelAnalysisResult) {
        _uiState.value = _uiState.value.copy(secondItem = item)
    }
}
