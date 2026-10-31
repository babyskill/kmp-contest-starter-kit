package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.PortionInfo
import com.labellens.app.domain.model.PortionMode
import kotlin.math.roundToInt

object PortionMath {

    private val servingSizeRegex = Regex(
        "(?i)(?:serving\\s*size|per\\s*serving|khẩu\\s*phần(?:\\s*mỗi\\s*lần)?)\\s*[:\\-]?\\s*([0-9a-zA-Z\\s()/,.\"-]+?)(?=\\s*\\.\\s+|\\s*\\.\$|\\n|;|,?\\s*(?:calories|servings|amount|năng\\s*lượng)|\$)",
    )

    private val servingsPerContainerRegex = Regex(
        "(?i)(?:about|approx\\.?|khoảng)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:servings\\s*per\\s*container|servings\\s*per\\s*package|servings|khẩu\\s*phần\\s*mỗi\\s*hộp)",
    )

    private val altServingsPerContainerRegex = Regex(
        "(?i)servings\\s*per\\s*container\\s*[:\\-]?\\s*(?:about|approx\\.?)?\\s*([0-9]+(?:\\.[0-9]+)?)",
    )

    /**
     * Extracts serving size and servings per container from raw label text.
     */
    fun parsePortionInfo(rawText: String): PortionInfo {
        var size: String? = null
        val sizeMatch = servingSizeRegex.find(rawText)
        if (sizeMatch != null) {
            val candidate = sizeMatch.groupValues[1].trim().trimEnd('.', ';', ',')
            if (candidate.length in 2..40) {
                size = candidate
            }
        }

        var servings: Double? = null
        val sMatch = altServingsPerContainerRegex.find(rawText) ?: servingsPerContainerRegex.find(rawText)
        if (sMatch != null) {
            servings = sMatch.groupValues[1].toDoubleOrNull()
        }

        val hasMultiple = servings != null && servings > 1.0
        return PortionInfo(
            servingSize = size,
            servingsPerContainer = servings,
            hasMultipleServings = hasMultiple,
        )
    }

    /**
     * Determines whether the [1 Serving] / [Whole Pack] toggle should be visible.
     * Shown ONLY when servings per container > 1.
     */
    fun shouldShowToggle(servingsPerContainer: Double?): Boolean = servingsPerContainer != null && servingsPerContainer > 1.0

    /**
     * Default selection:
     * Whole Pack when servings per container is between 1.5 and 3.5 inclusive;
     * otherwise 1 Serving.
     */
    fun defaultPortionMode(servingsPerContainer: Double?): PortionMode {
        if (servingsPerContainer == null || servingsPerContainer <= 1.0) {
            return PortionMode.SINGLE_SERVING
        }
        return if (servingsPerContainer in 1.5..3.5) {
            PortionMode.WHOLE_PACK
        } else {
            PortionMode.SINGLE_SERVING
        }
    }

    /**
     * Calculates added sugar / sugar details: grams and teaspoons (grams / 4, one decimal).
     */
    fun calculateSugarTeaspoons(grams: Double): Double {
        val tsp = grams / 4.0
        return (tsp * 10.0).roundToInt() / 10.0
    }

    /**
     * Calculates sodium percentage of the 2,300 mg daily reference.
     */
    fun calculateSodiumPercentDailyValue(mg: Double): Int = ((mg / 2300.0) * 100.0).roundToInt()

    /**
     * Calculates grams of salt equivalent: sodium mg * 2.5 / 1000, one decimal.
     */
    fun calculateSaltEquivalentGrams(mg: Double): Double {
        val saltG = (mg * 2.5) / 1000.0
        return (saltG * 10.0).roundToInt() / 10.0
    }

    /**
     * Formats secondary detail for sugar.
     */
    fun formatSugarSecondaryDetail(grams: Double, language: AppLanguage): String {
        val tsp = calculateSugarTeaspoons(grams)
        val tspFormatted = if (tsp % 1.0 == 0.0) "${tsp.toInt()}.0" else "$tsp"
        return when (language) {
            AppLanguage.VIETNAMESE -> "$tspFormatted muỗng cà phê đường"
            AppLanguage.ENGLISH -> "$tspFormatted tsp"
        }
    }

    /**
     * Formats secondary detail for sodium: % DV and salt equivalent.
     */
    fun formatSodiumSecondaryDetail(mg: Double, language: AppLanguage): String {
        val pct = calculateSodiumPercentDailyValue(mg)
        val saltEq = calculateSaltEquivalentGrams(mg)
        val saltEqFormatted = if (saltEq % 1.0 == 0.0) "${saltEq.toInt()}.0" else "$saltEq"
        return when (language) {
            AppLanguage.VIETNAMESE -> "$pct% Nhu cầu hằng ngày • ${saltEqFormatted}g muối quy đổi"
            AppLanguage.ENGLISH -> "$pct% DV • ${saltEqFormatted}g salt eq"
        }
    }

    /**
     * Scales nutrition facts based on the selected PortionMode and servingsPerContainer factor.
     */
    fun scaleNutritionFacts(
        facts: List<NutritionFact>,
        mode: PortionMode,
        servingsPerContainer: Double?,
        language: AppLanguage,
    ): List<NutritionFact> {
        val factor = if (mode == PortionMode.WHOLE_PACK && servingsPerContainer != null && servingsPerContainer > 1.0) {
            servingsPerContainer
        } else {
            1.0
        }

        return facts.map { fact ->
            scaleFact(fact, factor, language)
        }
    }

    private fun scaleFact(fact: NutritionFact, factor: Double, language: AppLanguage): NutritionFact {
        val amountStr = fact.amount.trim()
        val numMatch = Regex("^([0-9]+(?:\\.[0-9]+)?)\\s*(.*)$").find(amountStr)
        if (numMatch == null) return fact

        val baseVal = numMatch.groupValues[1].toDoubleOrNull() ?: return fact
        val unit = numMatch.groupValues[2].trim()
        val scaledVal = baseVal * factor

        val formattedAmount = if (unit.equals("kcal", ignoreCase = true) || unit.equals("cal", ignoreCase = true)) {
            "${scaledVal.roundToInt()} $unit"
        } else if (unit.equals("mg", ignoreCase = true)) {
            "${scaledVal.roundToInt()} $unit"
        } else {
            val rounded1 = (scaledVal * 10.0).roundToInt() / 10.0
            if (rounded1 % 1.0 == 0.0) {
                "${rounded1.toInt()} $unit"
            } else {
                "$rounded1 $unit"
            }
        }

        // Secondary details for sugar and sodium
        val secondary = when {
            isSugarFact(fact.name) -> {
                formatSugarSecondaryDetail(scaledVal, language)
            }

            isSodiumFact(fact.name) -> {
                val mg = if (unit.equals("g", ignoreCase = true)) scaledVal * 1000.0 else scaledVal
                formatSodiumSecondaryDetail(mg, language)
            }

            else -> null
        }

        return fact.copy(
            amount = formattedAmount,
            secondaryDetail = secondary,
        )
    }

    private fun isSugarFact(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("sugar") || lower.contains("đường")
    }

    private fun isSodiumFact(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("sodium") || lower.contains("natri")
    }
}
