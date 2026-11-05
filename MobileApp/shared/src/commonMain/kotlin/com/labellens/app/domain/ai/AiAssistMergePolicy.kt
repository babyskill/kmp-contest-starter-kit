package com.labellens.app.domain.ai

import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeResponse
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.FitHeaderInfo
import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.UserProfile

object AiAssistMergePolicy {

    fun normalize(text: String): String = AiAssistTriggerPolicy.stripDiacritics(text)
        .replace(Regex("[^a-z0-9 ]"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

    private fun matchesUserAllergen(itemTitle: String, userProfile: UserProfile): Boolean {
        val normTitle = normalize(itemTitle)
        if (normTitle.isBlank()) return false

        // Check custom allergens
        for (custom in userProfile.customAllergens) {
            val normCustom = normalize(custom)
            if (normCustom.isNotBlank() && (normTitle == normCustom || normTitle.contains(normCustom))) {
                return true
            }
        }

        // Check standard user allergies
        for (allergen in userProfile.allergies) {
            val names = listOf(
                allergen.displayName,
                allergen.localizedName(AppLanguage.ENGLISH),
                allergen.localizedName(AppLanguage.VIETNAMESE),
                allergen.name,
            )
            for (name in names) {
                // Split multi-names like "Wheat / Gluten" -> "wheat", "gluten"
                val tokens = name.split("/", "•", "(", ")", ",")
                for (token in tokens) {
                    val normToken = normalize(token)
                    if (normToken.length >= 3 && normTitle.contains(normToken)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun merge(
        localResult: LabelAnalysisResult,
        cloudResponse: BackendAnalyzeResponse,
        userProfile: UserProfile,
        language: AppLanguage,
    ): LabelAnalysisResult {
        // Collect existing normalized titles to prevent duplicates
        val existingTitles = (localResult.watchOutForYou + localResult.worthNoting + localResult.goodToKnow)
            .map { normalize(it.title) }
            .toMutableSet()

        val existingBreakdownNames = localResult.fullBreakdown
            .flatMap { listOf(normalize(it.canonicalName), normalize(it.rawName)) }
            .toMutableSet()

        val newWatchOut = mutableListOf<FlaggedItem>()
        val newWorthNoting = mutableListOf<FlaggedItem>()
        val newGoodToKnow = mutableListOf<FlaggedItem>()
        val newBreakdown = mutableListOf<IngredientBreakdown>()

        var hasAllergenMatch = false

        // 1. Process watchOut
        for (item in cloudResponse.data.sections.watchOut) {
            val norm = normalize(item)
            if (norm.isNotBlank() && existingTitles.add(norm)) {
                val isAllergen = matchesUserAllergen(item, userProfile)
                if (isAllergen) {
                    hasAllergenMatch = true
                }
                val severity = if (isAllergen) FlagSeverity.PERSONAL_ALLERGEN else FlagSeverity.PERSONAL_AVOID
                newWatchOut.add(
                    FlaggedItem(
                        title = item,
                        detail = item,
                        severity = severity,
                        verbatimSourceLine = null,
                        isAiGenerated = true,
                    ),
                )
                if (existingBreakdownNames.add(norm)) {
                    newBreakdown.add(
                        IngredientBreakdown(
                            rawName = item,
                            canonicalName = item,
                            category = if (language == AppLanguage.VIETNAMESE) "Trợ lý AI" else "AI Assist",
                            plainExplanation = item,
                            whyUsed = "",
                            doseContextNote = "",
                            isKnown = true,
                            isAiGenerated = true,
                        ),
                    )
                }
            }
        }

        // 2. Process worthNoting
        for (item in cloudResponse.data.sections.worthNoting) {
            val norm = normalize(item)
            if (norm.isNotBlank() && existingTitles.add(norm)) {
                newWorthNoting.add(
                    FlaggedItem(
                        title = item,
                        detail = item,
                        severity = FlagSeverity.WORTH_NOTING,
                        verbatimSourceLine = null,
                        isAiGenerated = true,
                    ),
                )
                if (existingBreakdownNames.add(norm)) {
                    newBreakdown.add(
                        IngredientBreakdown(
                            rawName = item,
                            canonicalName = item,
                            category = if (language == AppLanguage.VIETNAMESE) "Trợ lý AI" else "AI Assist",
                            plainExplanation = item,
                            whyUsed = "",
                            doseContextNote = "",
                            isKnown = true,
                            isAiGenerated = true,
                        ),
                    )
                }
            }
        }

        // 3. Process goodToKnow
        for (item in cloudResponse.data.sections.goodToKnow) {
            val norm = normalize(item)
            if (norm.isNotBlank() && existingTitles.add(norm)) {
                newGoodToKnow.add(
                    FlaggedItem(
                        title = item,
                        detail = item,
                        severity = FlagSeverity.GOOD_TO_KNOW,
                        verbatimSourceLine = null,
                        isAiGenerated = true,
                    ),
                )
                if (existingBreakdownNames.add(norm)) {
                    newBreakdown.add(
                        IngredientBreakdown(
                            rawName = item,
                            canonicalName = item,
                            category = if (language == AppLanguage.VIETNAMESE) "Trợ lý AI" else "AI Assist",
                            plainExplanation = item,
                            whyUsed = "",
                            doseContextNote = "",
                            isKnown = true,
                            isAiGenerated = true,
                        ),
                    )
                }
            }
        }

        val totalContributed = newWatchOut.size + newWorthNoting.size + newGoodToKnow.size
        if (totalContributed == 0) {
            return localResult
        }

        // Fit header adjustment: only move UP in severity if cloud matched user's own allergens, never down
        val currentFitHeader = localResult.fitHeader
        val currentState = currentFitHeader?.state ?: ProfileFitState.NO_MATCHES

        val updatedFitHeader = if (hasAllergenMatch) {
            if (currentState.priority < ProfileFitState.CONTAINS_YOURS.priority) {
                val title = if (language == AppLanguage.VIETNAMESE) {
                    "Chứa thành phần dị ứng của bạn"
                } else {
                    "Contains your allergen"
                }
                val subtitle = if (language == AppLanguage.VIETNAMESE) {
                    "Trợ lý AI phát hiện thành phần phù hợp với hồ sơ dị ứng của bạn."
                } else {
                    "AI assist identified an ingredient matching your allergy profile."
                }
                FitHeaderInfo(
                    state = ProfileFitState.CONTAINS_YOURS,
                    title = title,
                    subtitle = subtitle,
                    matchedTerms = currentFitHeader?.matchedTerms.orEmpty() + newWatchOut.filter { it.severity == FlagSeverity.PERSONAL_ALLERGEN }.map { it.title },
                    badgeLabel = currentFitHeader?.badgeLabel.orEmpty(),
                )
            } else {
                currentFitHeader
            }
        } else {
            // Severity never moves down
            currentFitHeader
        }

        // Source line under product card: 'AI assist - Tier 2/3 - <model>' (EN+VI)
        val aiSourceLine = if (language == AppLanguage.VIETNAMESE) {
            "Trợ lý AI - Tier ${cloudResponse.tierUsed} - ${cloudResponse.model}"
        } else {
            "AI assist - Tier ${cloudResponse.tierUsed} - ${cloudResponse.model}"
        }

        return localResult.copy(
            watchOutForYou = localResult.watchOutForYou + newWatchOut,
            worthNoting = localResult.worthNoting + newWorthNoting,
            goodToKnow = localResult.goodToKnow + newGoodToKnow,
            fullBreakdown = localResult.fullBreakdown + newBreakdown,
            fitHeader = updatedFitHeader,
            sourceAttribution = aiSourceLine,
        )
    }
}
