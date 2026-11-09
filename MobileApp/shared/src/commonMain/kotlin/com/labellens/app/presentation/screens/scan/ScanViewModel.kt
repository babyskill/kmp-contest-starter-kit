package com.labellens.app.presentation.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labellens.app.data.repository.ScanRepository
import com.labellens.app.data.source.remote.apiservices.barcode.BarcodeLookupService
import com.labellens.app.data.source.remote.response.barcode.toLabelText
import com.labellens.app.domain.analyzer.ProductNameSanitizer
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
    val rawOcrResult: String? = null,
)

class ScanViewModel(
    private val scanRepository: ScanRepository,
    private val barcodeLookupService: BarcodeLookupService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val scanner = getPlatformLabelScanner()

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun onScanBarcode(barcodeValue: String, onFinished: (rawText: String, name: String, barcode: String, sourceName: String?) -> Unit) {
        val trimmed = barcodeValue.trim()
        val matchedSample = BuiltInSamples.all.firstOrNull { it.barcode == trimmed }
        if (matchedSample != null) {
            onFinished(matchedSample.fullLabelText, matchedSample.name, trimmed, null)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentStep = "Looking up product in Open Food Facts...",
                completedSteps = emptyList(),
                error = null,
            )
            try {
                val response = barcodeLookupService.lookupBarcode(trimmed)
                val labelText = response.toLabelText()
                val productName = response.product?.productName ?: "Scanned Product ($trimmed)"

                if (labelText.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                    onFinished(labelText, productName, trimmed, "Open Food Facts")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Barcode not found in Open Food Facts database. Please snap a photo of the label or paste label text below.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Could not connect to Open Food Facts (${e.message ?: "network error"}). Try photographing the label or paste text directly.",
                )
            }
        }
    }

    fun processImageSource(
        imageSource: Any,
        onFinished: (rawText: String, name: String, barcode: String?, sourceName: String?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentStep = "Acquiring label image...",
                completedSteps = emptyList(),
                error = null,
            )

            // Step 1: Scan for barcode in image
            _uiState.value = _uiState.value.copy(
                currentStep = "Scanning for barcodes in image...",
                completedSteps = listOf("Image acquired"),
            )
            val detectedBarcode = try {
                scanner.scanBarcodeFromImage(imageSource)
            } catch (e: Exception) {
                null
            }

            if (!detectedBarcode.isNullOrBlank()) {
                // If barcode found, attempt lookup
                _uiState.value = _uiState.value.copy(
                    currentStep = "Barcode found: $detectedBarcode. Looking up product...",
                    completedSteps = listOf("Image acquired", "Barcode detected: $detectedBarcode"),
                )
                try {
                    val offResponse = barcodeLookupService.lookupBarcode(detectedBarcode)
                    val labelText = offResponse.toLabelText()
                    if (labelText.isNotBlank()) {
                        scanRepository.incrementPhotoAnalysisCount()
                        _uiState.value = _uiState.value.copy(isProcessing = false)
                        onFinished(
                            labelText,
                            offResponse.product?.productName ?: "Scanned Product ($detectedBarcode)",
                            detectedBarcode,
                            "Open Food Facts",
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    // Fallthrough to OCR if barcode lookup fails
                }
            }

            // Step 2: Run On-device OCR
            _uiState.value = _uiState.value.copy(
                currentStep = "Recognising text & multilingual characters (ML Kit OCR)...",
                completedSteps = if (detectedBarcode != null) listOf("Image acquired", "Barcode checked") else listOf("Image acquired"),
            )

            val extractedText = try {
                scanner.extractTextFromImage(imageSource)
            } catch (e: Exception) {
                null
            }

            if (extractedText.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Could not detect clear text on this image. You can paste the raw label text directly or try one of the built-in samples below.",
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                currentStep = "Parsing ingredients & nutrition metrics...",
                completedSteps = listOf("Image acquired", "Text recognized (${extractedText.length} chars)"),
                rawOcrResult = extractedText,
            )

            scanRepository.incrementPhotoAnalysisCount()
            _uiState.value = _uiState.value.copy(isProcessing = false)

            // First line or snippet as product name only if it looks like a valid product name
            val candidateName = extractedText.lines().map { it.trim() }.firstOrNull { ProductNameSanitizer.looksLikeProductName(it) } ?: ""
            onFinished(extractedText, candidateName, detectedBarcode, null)
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
                error = null,
            )
            kotlinx.coroutines.delay(100)

            _uiState.value = _uiState.value.copy(
                currentStep = "Enhancing text contrast & orientation...",
                completedSteps = listOf("Acquiring label image"),
            )
            kotlinx.coroutines.delay(100)

            _uiState.value = _uiState.value.copy(
                currentStep = "Recognising text & multilingual characters...",
                completedSteps = listOf("Acquiring label image", "Enhancing contrast"),
            )
            kotlinx.coroutines.delay(100)

            _uiState.value = _uiState.value.copy(
                currentStep = "Parsing ingredients & nutrition metrics...",
                completedSteps = listOf("Acquiring label image", "Enhancing contrast", "Text recognition"),
            )
            kotlinx.coroutines.delay(100)

            scanRepository.incrementPhotoAnalysisCount()
            _uiState.value = _uiState.value.copy(isProcessing = false)
            onFinished(imageLabelText, productName)
        }
    }
}
