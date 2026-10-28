package com.labellens.app.domain.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class AndroidLabelScanner : LabelScanner {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()

    override suspend fun scanBarcodeFromImage(imagePathOrBytes: Any): String? {
        val inputImage = createInputImage(imagePathOrBytes) ?: return null
        return suspendCancellableCoroutine { continuation ->
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val rawValue = barcodes.firstOrNull()?.rawValue
                    continuation.resume(rawValue)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    override suspend fun extractTextFromImage(imagePathOrBytes: Any): String? {
        val inputImage = createInputImage(imagePathOrBytes) ?: return null
        return suspendCancellableCoroutine { continuation ->
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.trim()
                    continuation.resume(if (text.isEmpty()) null else text)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    private suspend fun createInputImage(source: Any): InputImage? {
        return try {
            when (source) {
                is InputImage -> source

                is Bitmap -> InputImage.fromBitmap(source, 0)

                is ByteArray -> {
                    val bmp = BitmapFactory.decodeByteArray(source, 0, source.size) ?: return null
                    InputImage.fromBitmap(bmp, 0)
                }

                is File -> {
                    val bmp = BitmapFactory.decodeFile(source.absolutePath) ?: return null
                    InputImage.fromBitmap(bmp, 0)
                }

                is String -> {
                    val file = File(source)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(file.absolutePath) ?: return null
                        InputImage.fromBitmap(bmp, 0)
                    } else {
                        null
                    }
                }

                is PlatformFile -> {
                    val bytes = source.readBytes()
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
                    InputImage.fromBitmap(bmp, 0)
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getPlatformLabelScanner(): LabelScanner = AndroidLabelScanner()
