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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.btn_back
import com.labellens.app.generated.resources.scan_analyzing_subtitle
import com.labellens.app.generated.resources.scan_analyzing_title
import com.labellens.app.generated.resources.scan_barcode_lookup_btn
import com.labellens.app.generated.resources.scan_barcode_placeholder
import com.labellens.app.generated.resources.scan_barcode_subtitle
import com.labellens.app.generated.resources.scan_barcode_title
import com.labellens.app.generated.resources.scan_btn_from_gallery
import com.labellens.app.generated.resources.scan_btn_take_photo
import com.labellens.app.generated.resources.scan_btn_test_sample
import com.labellens.app.generated.resources.scan_header_subtitle
import com.labellens.app.generated.resources.scan_header_title
import com.labellens.app.generated.resources.scan_notice_dismiss
import com.labellens.app.generated.resources.scan_notice_title
import com.labellens.app.generated.resources.scan_paste_analyze_btn
import com.labellens.app.generated.resources.scan_paste_placeholder
import com.labellens.app.generated.resources.scan_paste_subtitle
import com.labellens.app.generated.resources.scan_paste_title
import com.labellens.app.generated.resources.scan_photo_subtitle
import com.labellens.app.generated.resources.scan_photo_title
import com.labellens.app.generated.resources.scan_sample_run_btn
import com.labellens.app.generated.resources.scan_samples_section_title
import com.labellens.app.presentation.screens.camera.isInAppCameraSupported
import com.labellens.app.util.file.openCameraPicker
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (rawText: String, productName: String, barcode: String?, sampleId: String?, sourceName: String?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCamera: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    var pastedText by remember { mutableStateOf("") }
    var enteredBarcode by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { platformFile ->
        if (platformFile != null) {
            viewModel.processImageSource(platformFile) { raw, name, barcode, sourceName ->
                onNavigateToResult(raw, name, barcode, null, sourceName)
            }
        }
    }

    if (state.isProcessing) {
        // Honest step checklist during processing
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            ScanProcessingCard(state = state)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Error card if any
        state.error?.let { err ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF3DF)),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.scan_notice_title),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB7791F),
                                style = AppTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = err,
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.text.primary,
                            )
                        }
                        IconButton(onClick = { viewModel.dismissError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.scan_notice_dismiss),
                                tint = Color(0xFFB7791F),
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.btn_back),
                        tint = AppTheme.colors.text.primary,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.scan_header_title),
                        style = AppTheme.typography.headlineLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.scan_header_subtitle),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }
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
                        text = stringResource(Res.string.scan_photo_title),
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.scan_photo_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppButton(
                            text = stringResource(Res.string.scan_btn_take_photo),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isInAppCameraSupported() && onNavigateToCamera != null) {
                                    onNavigateToCamera()
                                } else {
                                    coroutineScope.launch {
                                        val captured = FileKit.openCameraPicker()
                                        if (captured != null) {
                                            viewModel.processImageSource(captured) { raw, name, barcode, sourceName ->
                                                onNavigateToResult(raw, name, barcode, null, sourceName)
                                            }
                                        }
                                    }
                                }
                            },
                        )
                        AppButton(
                            text = stringResource(Res.string.scan_btn_from_gallery),
                            style = ButtonStyle.ALTERNATIVE,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                galleryLauncher.launch()
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    AppButton(
                        text = stringResource(Res.string.scan_btn_test_sample),
                        style = ButtonStyle.ALTERNATIVE,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.processImageSource("/data/data/com.labellens.app/files/held_out_label_almond_butter.png") { raw, name, barcode, sourceName ->
                                onNavigateToResult(raw, name, barcode, null, sourceName)
                            }
                        },
                    )
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
                        text = stringResource(Res.string.scan_barcode_title),
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.scan_barcode_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredBarcode,
                        onValueChange = { enteredBarcode = it },
                        placeholder = { Text(stringResource(Res.string.scan_barcode_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppButton(
                        text = stringResource(Res.string.scan_barcode_lookup_btn),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (enteredBarcode.isNotBlank()) {
                                viewModel.onScanBarcode(enteredBarcode) { raw, name, bc, src ->
                                    onNavigateToResult(raw, name, bc, null, src)
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
                        text = stringResource(Res.string.scan_paste_title),
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.scan_paste_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        placeholder = { Text(stringResource(Res.string.scan_paste_placeholder)) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppButton(
                        text = stringResource(Res.string.scan_paste_analyze_btn),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (pastedText.isNotBlank()) {
                                onNavigateToResult(pastedText, "Custom Label Text", null, null, null)
                            }
                        },
                    )
                }
            }
        }

        // Quick Test with Built-in Samples
        item {
            Text(
                text = stringResource(Res.string.scan_samples_section_title),
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
                            onNavigateToResult(raw, name, sample.barcode, sample.id, null)
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
                        text = stringResource(Res.string.scan_sample_run_btn),
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun ScanProcessingCard(
    state: ScanUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = stringResource(Res.string.scan_analyzing_title),
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text.primary,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.scan_analyzing_subtitle),
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
