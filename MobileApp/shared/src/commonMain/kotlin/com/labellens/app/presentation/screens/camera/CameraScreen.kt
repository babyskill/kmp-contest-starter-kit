package com.labellens.app.presentation.screens.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraScreen(
    onCapture: (String) -> Unit,
    onClose: () -> Unit,
    onFallbackToFilePicker: () -> Unit,
    modifier: Modifier = Modifier,
)

expect fun isInAppCameraSupported(): Boolean
