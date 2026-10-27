package com.labellens.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.SampleProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LabelLensHomeUiState(
    val recentScans: List<LabelAnalysisResult> = emptyList(),
    val sampleProducts: List<SampleProduct> = BuiltInSamples.all,
    val isLoading: Boolean = false,
    val calmTip: String = "Imported Asian packaging often groups allergens under '알레르기' or 'アレルギー' at the very bottom.",
)

class HomeViewModel(
    private val scanRepository: ScanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabelLensHomeUiState())
    val uiState: StateFlow<LabelLensHomeUiState> = _uiState.asStateFlow()

    init {
        loadRecents()
    }

    private fun loadRecents() {
        viewModelScope.launch {
            scanRepository.getRecentScans(5).collect { list ->
                _uiState.value = _uiState.value.copy(recentScans = list)
            }
        }
    }
}
