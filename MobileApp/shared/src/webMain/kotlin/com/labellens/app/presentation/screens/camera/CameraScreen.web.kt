package com.labellens.app.presentation.screens.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
actual fun CameraScreen(
    onCapture: (String) -> Unit,
    onClose: () -> Unit,
    onFallbackToFilePicker: () -> Unit,
    modifier: Modifier,
) {
    LaunchedEffect(Unit) {
        onFallbackToFilePicker()
    }
}

actual fun isInAppCameraSupported(): Boolean = false
