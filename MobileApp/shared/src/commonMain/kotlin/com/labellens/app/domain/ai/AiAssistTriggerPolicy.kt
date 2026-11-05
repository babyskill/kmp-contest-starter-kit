package com.labellens.app.domain.ai

import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.ScanSource

object AiAssistTriggerPolicy {

    private val NUTRITION_MARKERS = listOf(
        "nutrition facts",
        "nutrition information",
        "supplement facts",
        "calories",
        "serving size",
        "per serving",
        "thong tin dinh duong",
        "thanh phan dinh duong",
        "gia tri dinh duong",
    )

    fun stripDiacritics(text: String): String {
        val map = mapOf(
            'à' to 'a', 'á' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
            'ă' to 'a', 'ằ' to 'a', 'ắ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
            'â' to 'a', 'ầ' to 'a', 'ấ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
            'đ' to 'd',
            'è' to 'e', 'é' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
            'ê' to 'e', 'ề' to 'e', 'ế' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
            'ì' to 'i', 'í' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
            'ò' to 'o', 'ó' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
            'ô' to 'o', 'ồ' to 'o', 'ố' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
            'ơ' to 'o', 'ờ' to 'o', 'ớ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
            'ù' to 'u', 'ú' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
            'ư' to 'u', 'ừ' to 'u', 'ứ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
            'ỳ' to 'y', 'ý' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
        )
        val sb = StringBuilder(text.length)
        for (ch in text.lowercase()) {
            sb.append(map[ch] ?: ch)
        }
        return sb.toString()
    }

    fun containsNutritionMarkers(text: String): Boolean {
        val normalized = stripDiacritics(text)
        return NUTRITION_MARKERS.any { marker -> normalized.contains(marker) }
    }

    fun containsNonLatinScript(text: String): Boolean {
        for (ch in text) {
            val code = ch.code
            if (code in 0x4E00..0x9FFF || code in 0x3400..0x4DBF) return true
            if (code in 0x3040..0x309F || code in 0x30A0..0x30FF) return true
            if (code in 0xAC00..0xD7AF || code in 0x1100..0x11FF || code in 0x3130..0x318F) return true
            if (code in 0x0400..0x04FF) return true
            if (code in 0x0600..0x06FF) return true
            if (code in 0x0E00..0x0E7F) return true
            if (code in 0x0900..0x097F) return true
            if (code in 0x0590..0x05FF) return true
            if (code in 0x0370..0x03FF) return true
        }
        return false
    }

    fun shouldTrigger(
        isAiAssistEnabled: Boolean,
        appKey: String,
        isSample: Boolean,
        localResult: LabelAnalysisResult,
        rawOcrText: String,
    ): Boolean {
        if (!isAiAssistEnabled) return false
        if (appKey.isBlank()) return false
        if (isSample || localResult.scanSource == ScanSource.SAMPLE) return false

        // (a) fit state NOT_ENOUGH_INFO
        if (localResult.fitHeader?.state == ProfileFitState.NOT_ENOUGH_INFO) {
            return true
        }

        // (b) fewer than 3 ingredients parsed while OCR text is longer than 200 chars
        if (localResult.fullBreakdown.size < 3 && rawOcrText.length > 200) {
            return true
        }

        // (c) no nutrition facts parsed while OCR text contains nutrition markers
        if (localResult.nutritionHighlights.isEmpty() && containsNutritionMarkers(rawOcrText)) {
            return true
        }

        // (d) OCR text contains non-Latin script
        if (containsNonLatinScript(rawOcrText)) {
            return true
        }

        return false
    }
}
