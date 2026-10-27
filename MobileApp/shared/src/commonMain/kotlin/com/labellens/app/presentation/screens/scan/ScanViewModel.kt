package com.labellens.app.presentation.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.SampleProduct
import com.labellens.app.domain.scanner.getPlatformLabelScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanProcessingStep {
    data class Active(val stepName: String) : ScanProcessingStep
    data object Completed : ScanProcessingStep
}

data class ScanUiState(
    val isProcessing: Boolean = false,
    val currentStep: String = "",
    val completedSteps: List<String> = emptyList(),
    val rawExtractedText: String = "",
    val detectedBarcode: String = "",
    val sampleProducts: List<SampleProduct> = BuiltInSamples.all,
    val error: String? = null,
)

class ScanViewModel(
    private val scanRepository: ScanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val scanner = getPlatformLabelScanner()

    fun onScanBarcode(barcodeValue: String, onFinished: (rawText: String, name: String, barcode: String) -> Unit) {
        val trimmed = barcodeValue.trim()
        val matchedSample = BuiltInSamples.all.firstOrNull { it.barcode == trimmed }
        if (matchedSample != null) {
            onFinished(matchedSample.fullLabelText, matchedSample.name, trimmed)
        } else {
            // General barcode: simulate barcode lookup or prompt
            onFinished(
                "INGREDIENTS: Water, Sugar, Natural Flavor, Salt.\nNutrition Facts: Calories: 100. Sodium: 50mg. Sugars: 12g.",
                "Scanned Product ($trimmed)",
                trimmed,
            )
        }
    }

    fun onSimulateImageScan(
        imageLabelText: String,
        productName: String,
        onFinished: (rawText: String, name: String) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentStep = "Acquiring label image...",
                completedSteps = emptyList(),
            )
            kotlinx.coroutines.delay(200)

            _uiState.value = _uiState.value.copy(
                currentStep = "Enhancing text contrast & orientation...",
                completedSteps = listOf("Acquiring label image"),
            )
            kotlinx.coroutines.delay(250)

            _uiState.value = _uiState.value.copy(
                currentStep = "Recognising text & multilingual characters...",
                completedSteps = listOf("Acquiring label image", "Enhancing contrast"),
            )
            kotlinx.coroutines.delay(250)

            _uiState.value = _uiState.value.copy(
                currentStep = "Parsing ingredients & nutrition metrics...",
                completedSteps = listOf("Acquiring label image", "Enhancing contrast", "Text recognition"),
            )
            kotlinx.coroutines.delay(200)

            scanRepository.incrementPhotoAnalysisCount()
            _uiState.value = _uiState.value.copy(isProcessing = false)
            onFinished(imageLabelText, productName)
        }
    }
}
