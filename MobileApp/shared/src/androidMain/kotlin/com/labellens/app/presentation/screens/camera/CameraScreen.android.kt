package com.labellens.app.presentation.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.labellens.app.designsystem.components.AppButton
import com.labellens.app.designsystem.components.ButtonStyle
import com.labellens.app.designsystem.theme.AppTheme
import com.labellens.app.generated.resources.Res
import com.labellens.app.generated.resources.camera_capture
import com.labellens.app.generated.resources.camera_close
import com.labellens.app.generated.resources.camera_flash_toggle
import com.labellens.app.generated.resources.camera_guide_hint
import com.labellens.app.generated.resources.camera_permission_allow
import com.labellens.app.generated.resources.camera_permission_fallback
import com.labellens.app.generated.resources.camera_permission_rationale
import com.labellens.app.generated.resources.camera_permission_title
import com.labellens.app.generated.resources.ic_flash
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun CameraScreen(
    onCapture: (String) -> Unit,
    onClose: () -> Unit,
    onFallbackToFilePicker: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            hasCameraPermission = true
        } else {
            // Permission denied -> fall back directly to FileKit picker
            onFallbackToFilePicker()
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionRationale(
            onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onFallback = onFallbackToFilePicker,
            modifier = modifier,
        )
    } else {
        CameraViewfinder(
            onCapture = onCapture,
            onClose = onClose,
            modifier = modifier,
        )
    }
}

@Composable
private fun CameraPermissionRationale(
    onAllow: () -> Unit,
    onFallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121413))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.camera_permission_title),
                    style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.text.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.camera_permission_rationale),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    text = stringResource(Res.string.camera_permission_allow),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAllow,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppButton(
                    text = stringResource(Res.string.camera_permission_fallback),
                    style = ButtonStyle.ALTERNATIVE,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFallback,
                )
            }
        }
    }
}

@Composable
private fun CameraViewfinder(
    onCapture: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Live camera viewfinder
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val boundCamera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture,
                            )
                            camera = boundCamera
                        } catch (e: Exception) {
                            // Camera bind failed
                        }
                    },
                    ContextCompat.getMainExecutor(ctx),
                )
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Guide Overlay and Controls
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val guideWidth = (maxWidth * 0.85f).coerceAtMost(360.dp)
            val guideHeight = (guideWidth * 1.25f).coerceAtMost(maxHeight * 0.58f)

            // Centered guide frame
            Box(
                modifier = Modifier
                    .size(width = guideWidth, height = guideHeight)
                    .align(Alignment.Center)
                    .border(2.dp, Color(0xFF16794C), RoundedCornerShape(20.dp)),
            )

            // Hint chip below guide frame
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = guideHeight + 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.camera_guide_hint),
                    color = Color.White,
                    style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
            }

            // Top action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.camera_close),
                        tint = Color.White,
                    )
                }

                IconButton(
                    onClick = {
                        val newTorchState = !isTorchOn
                        camera?.cameraControl?.enableTorch(newTorchState)
                        isTorchOn = newTorchState
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                ) {
                    Icon(
                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_flash),
                        contentDescription = stringResource(Res.string.camera_flash_toggle),
                        tint = if (isTorchOn) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Bottom capture button bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.LightGray else Color.White)
                        .clickable(enabled = !isCapturing) {
                            isCapturing = true
                            val cacheDir = context.cacheDir
                            val photoFile = File(cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        if (isEmulator()) {
                                            injectSampleLabelOnEmulator(photoFile)
                                        }
                                        isCapturing = false
                                        onCapture(photoFile.absolutePath)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFF16794C),
                            strokeWidth = 3.dp,
                        )
                    }
                }
            }
        }
    }
}

actual fun isInAppCameraSupported(): Boolean = true

private fun isEmulator(): Boolean = Build.FINGERPRINT.startsWith("generic") ||
    Build.FINGERPRINT.startsWith("unknown") ||
    Build.MODEL.contains("google_sdk") ||
    Build.MODEL.contains("Emulator") ||
    Build.MODEL.contains("Android SDK built for x86") ||
    Build.MANUFACTURER.contains("Genymotion") ||
    Build.HARDWARE.contains("goldfish") ||
    Build.HARDWARE.contains("ranchu") ||
    Build.PRODUCT.contains("sdk_gphone")

private fun injectSampleLabelOnEmulator(photoFile: File) {
    try {
        val bitmap = Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 44f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Oat Crunch Cereal", 80f, 160f, paint)
        paint.textSize = 34f
        paint.typeface = Typeface.DEFAULT
        val lines = listOf(
            "Nutrition Facts",
            "Calories: 150 kcal",
            "Total Fat: 2g",
            "Sodium: 180mg",
            "Total Sugars: 9g",
            "Protein: 3g",
            "INGREDIENTS: Whole grain oats, cane sugar, modified corn starch,",
            "natural flavor, sea salt, tripotassium phosphate.",
            "CONTAINS: Oats. May contain wheat and soy.",
        )
        var y = 250f
        for (line in lines) {
            canvas.drawText(line, 80f, y, paint)
            y += 65f
        }
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    } catch (_: Exception) {
    }
}
