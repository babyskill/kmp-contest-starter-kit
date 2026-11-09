package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory

object ScanSummaryLocalizer {

    /**
     * Localizes the summary / verdict at display time so that History entries and
     * Home "Recent Scans" render in the current app language rather than the scan-time language.
     *
     * If structured data is available or verdict matches a known verdict pattern, it regenerates
     * the verdict in [language]. Otherwise falls back to a localized generic subtitle.
     */
    fun localizedSummary(result: LabelAnalysisResult, language: AppLanguage): String {
        val verdict = result.verdict

        // Check if verdict matches any known verdict pattern (Vietnamese or English)
        val normVerdict = DiacriticUtils.stripDiacritics(verdict.lowercase())
        val isSupplement = result.category == ProductCategory.SUPPLEMENT ||
            normVerdict.contains("thuc pham bo sung") ||
            normVerdict.contains("supplement formula")

        if (isSupplement) {
            return "${AnalysisStrings.verdictSupplement(language)} ${AnalysisStrings.verdictDisclaimer(language)}"
        }

        if (normVerdict.contains("luu y ca nhan") || normVerdict.contains("personal flags above") || result.watchOutForYou.isNotEmpty()) {
            return AnalysisStrings.verdictWatchOut(language)
        }

        if (normVerdict.contains("tien loi voi huong vi") || normVerdict.contains("convenient formulated") || result.worthNoting.size > 2) {
            return AnalysisStrings.verdictWorthNoting(language)
        }

        if (normVerdict.contains("lanh manh tap trung") || normVerdict.contains("wholesome formulation") ||
            (result.goodToKnow.size >= 2 && result.worthNoting.size <= 1)
        ) {
            return AnalysisStrings.verdictGoodToKnow(language)
        }

        if (normVerdict.contains("dong goi tieu chuan") || normVerdict.contains("standard packaged food")) {
            return AnalysisStrings.verdictBaseFood(language)
        }

        // Fallback: localized generic subtitle
        return AnalysisStrings.verdictBaseFood(language)
    }
}
