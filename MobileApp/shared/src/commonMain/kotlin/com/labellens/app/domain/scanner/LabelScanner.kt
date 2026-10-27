package com.labellens.app.domain.scanner

interface LabelScanner {
    suspend fun scanBarcodeFromImage(imagePathOrBytes: Any): String?
    suspend fun extractTextFromImage(imagePathOrBytes: Any): String?
}

expect fun getPlatformLabelScanner(): LabelScanner
