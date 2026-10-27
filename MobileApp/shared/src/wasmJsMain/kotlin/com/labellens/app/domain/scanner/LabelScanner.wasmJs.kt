package com.labellens.app.domain.scanner

class WasmJsLabelScanner : LabelScanner {
    override suspend fun scanBarcodeFromImage(imagePathOrBytes: Any): String? = null
    override suspend fun extractTextFromImage(imagePathOrBytes: Any): String? = null
}

actual fun getPlatformLabelScanner(): LabelScanner = WasmJsLabelScanner()
