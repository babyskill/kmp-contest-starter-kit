package com.labellens.app.presentation.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.SampleProduct

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (rawText: String, productName: String, barcode: String?, sampleId: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var pastedText by remember { mutableStateOf("") }
    var enteredBarcode by remember { mutableStateOf("") }
    var isPastingMode by remember { mutableStateOf(false) }

    if (state.isProcessing) {
        // Honest step checklist during processing
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.primary,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Analyzing Label",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Running on-device OCR and matching safe food standards.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Step checklist
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.completedSteps.forEach { step ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = step,
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.text.primary,
                                )
                            }
                        }

                        if (state.currentStep.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = AppTheme.colors.primary,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.currentStep,
                                    style = AppTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = AppTheme.colors.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Scan or Input Label",
                style = AppTheme.typography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Use camera, enter a barcode, or select a built-in test sample.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
        }

        // Camera / Gallery Photo Scan Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Photo Capture",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Snap a photo of the ingredients list or pick an image from gallery.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppButton(
                            text = "Take Photo",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                // Fallback simulation for emulator testing
                                val sample = BuiltInSamples.sample2
                                viewModel.onSimulateImageScan(
                                    imageLabelText = sample.fullLabelText,
                                    productName = sample.name,
                                ) { raw, name ->
                                    onNavigateToResult(raw, name, null, sample.id)
                                }
                            },
                        )
                        AppButton(
                            text = "From Gallery",
                            style = ButtonStyle.ALTERNATIVE,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val sample = BuiltInSamples.sample3
                                viewModel.onSimulateImageScan(
                                    imageLabelText = sample.fullLabelText,
                                    productName = sample.name,
                                ) { raw, name ->
                                    onNavigateToResult(raw, name, null, sample.id)
                                }
                            },
                        )
                    }
                }
            }
        }

        // Barcode Entry Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Barcode Scanner & Fallback",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter or scan a barcode (e.g. 8801043014853, 016000275263).",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredBarcode,
                        onValueChange = { enteredBarcode = it },
                        placeholder = { Text("Enter barcode digits") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppButton(
                        text = "Lookup Barcode",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (enteredBarcode.isNotBlank()) {
                                viewModel.onScanBarcode(enteredBarcode) { raw, name, bc ->
                                    onNavigateToResult(raw, name, bc, null)
                                }
                            }
                        },
                    )
                }
            }
        }

        // Paste Label Text Path (Guarantees emulator verification)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Paste Raw Label Text",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste English, Korean, Japanese, or Chinese ingredients text directly.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        placeholder = { Text("Paste ingredients and nutrition facts here...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppButton(
                        text = "Analyze Pasted Text",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (pastedText.isNotBlank()) {
                                onNavigateToResult(pastedText, "Custom Label Text", null, null)
                            }
                        },
                    )
                }
            }
        }

        // Quick Test with Built-in Samples
        item {
            Text(
                text = "Built-in Test Samples",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(state.sampleProducts) { sample ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.onSimulateImageScan(
                            imageLabelText = sample.fullLabelText,
                            productName = sample.name,
                        ) { raw, name ->
                            onNavigateToResult(raw, name, sample.barcode, sample.id)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sample.name,
                            fontWeight = FontWeight.Bold,
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.text.primary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sample.frontDescription,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run ->",
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
