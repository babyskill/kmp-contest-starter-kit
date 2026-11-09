package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import kotlin.math.round

/**
 * Deterministic local comparisons according to strict approved rules:
 * - sugar grams per serving -> teaspoons (g/4, 0.5 steps, both locales)
 * - sodium -> "% of daily needs" from mg / 2300 when label gives no %DV
 * - protein and others -> no comparison unless matching a small fixed reference table
 * - progress bar only for percentage comparisons (cap 0..100)
 */
object LocalComparisonHelper {

    // Small fixed reference table for honest everyday comparisons.
    // Keep it tiny and honest: only well-known, uncontroversial references.
    private val REFERENCE_COMPARISONS_VI = mapOf(
        "calcium_120mg" to "≈ nửa ly sữa",
        "calcium_240mg" to "≈ 1 ly sữa",
        "protein_3g" to "≈ nửa hũ sữa chua",
        "protein_6g" to "≈ 1 quả trứng gà",
    )

    private val REFERENCE_COMPARISONS_EN = mapOf(
        "calcium_120mg" to "≈ half a glass of milk",
        "calcium_240mg" to "≈ 1 glass of milk",
        "protein_3g" to "≈ half a cup of yogurt",
        "protein_6g" to "≈ 1 large egg",
    )

    fun extractNumber(text: String?): Double? {
        if (text.isNullOrBlank()) return null
        val match = Regex("""([0-9]+(?:[.,][0-9]+)?)""").find(text) ?: return null
        val normalized = match.groupValues[1].replace(',', '.')
        return normalized.toDoubleOrNull()
    }

    fun sugarTeaspoons(grams: Double, language: AppLanguage): String {
        val roundedHalf = round(grams / 4.0 * 2.0) / 2.0
        if (roundedHalf <= 0.0) {
            return if (language == AppLanguage.VIETNAMESE) "0 thìa cà phê" else "0 teaspoons of sugar"
        }
        val formatted = if (roundedHalf % 1.0 == 0.0) {
            roundedHalf.toInt().toString()
        } else {
            val dec = roundedHalf.toString()
            if (language == AppLanguage.VIETNAMESE) dec.replace('.', ',') else dec
        }
        return if (language == AppLanguage.VIETNAMESE) {
            "≈ $formatted thìa cà phê đường"
        } else {
            "≈ $formatted teaspoon${if (roundedHalf > 1.0) "s" else ""} of sugar"
        }
    }

    fun sodiumDailyPercent(mg: Double, language: AppLanguage): Pair<String, Int> {
        val pct = round((mg / 2300.0) * 100.0).toInt().coerceIn(0, 100)
        val text = if (language == AppLanguage.VIETNAMESE) {
            "≈ $pct% nhu cầu/ngày"
        } else {
            "≈ $pct% of daily needs"
        }
        return Pair(text, pct)
    }

    fun fixedReference(name: String, amountText: String?, language: AppLanguage): String? {
        val num = extractNumber(amountText) ?: return null
        val normName = name.lowercase().trim()

        val key = when {
            normName.contains("canxi") || normName.contains("calcium") -> {
                if (num in 100.0..150.0) {
                    "calcium_120mg"
                } else if (num in 200.0..300.0) {
                    "calcium_240mg"
                } else {
                    null
                }
            }

            normName.contains("dam") || normName.contains("protein") || normName.contains("chất đạm") -> {
                if (num in 2.5..4.0) {
                    "protein_3g"
                } else if (num in 5.5..7.5) {
                    "protein_6g"
                } else {
                    null
                }
            }

            else -> null
        } ?: return null

        return if (language == AppLanguage.VIETNAMESE) {
            REFERENCE_COMPARISONS_VI[key]
        } else {
            REFERENCE_COMPARISONS_EN[key]
        }
    }

    fun parsePercentage(text: String?): Int? {
        if (text.isNullOrBlank()) return null
        val match = Regex("""(\d{1,3})\s*%""").find(text) ?: return null
        val value = match.groupValues[1].toIntOrNull() ?: return null
        return value.coerceIn(0, 100)
    }

    fun computeComparison(name: String, amountText: String?, language: AppLanguage): Pair<String?, Int?> {
        val norm = name.lowercase().trim()
        val num = extractNumber(amountText)

        if (num != null) {
            if (norm.contains("duong") || norm.contains("sugar") || norm.contains("đường")) {
                return Pair(sugarTeaspoons(num, language), null)
            }
            if (norm.contains("natri") || norm.contains("sodium")) {
                val (str, pct) = sodiumDailyPercent(num, language)
                return Pair(str, pct)
            }
            val ref = fixedReference(name, amountText, language)
            if (ref != null) {
                return Pair(ref, null)
            }
        }
        return Pair(null, null)
    }
}
