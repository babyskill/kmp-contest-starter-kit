package com.labellens.app.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.domain.model.LabelAnalysisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val allScans: List<LabelAnalysisResult> = emptyList(),
    val favoriteScans: List<LabelAnalysisResult> = emptyList(),
    val filterFavoritesOnly: Boolean = false,
    val isLoading: Boolean = false,
)

class HistoryViewModel(
    private val scanRepository: ScanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            scanRepository.getAllScans().collect { list ->
                _uiState.value = _uiState.value.copy(allScans = list)
            }
        }
        viewModelScope.launch {
            scanRepository.getFavoriteScans().collect { favs ->
                _uiState.value = _uiState.value.copy(favoriteScans = favs)
            }
        }
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(filterFavoritesOnly = !_uiState.value.filterFavoritesOnly)
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            scanRepository.deleteScan(id)
        }
    }

    fun toggleFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            scanRepository.toggleFavorite(id, isFavorite)
        }
    }
}
