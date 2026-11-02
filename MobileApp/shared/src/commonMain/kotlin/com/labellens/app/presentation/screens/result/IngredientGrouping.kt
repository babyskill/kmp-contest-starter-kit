package com.labellens.app.presentation.screens.result

import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult

enum class IngredientDetailGroup {
    WATCH_OUT,
    WORTH_NOTING,
    GOOD_TO_KNOW,
    REMAINING,
}

data class GroupedIngredient(
    val ingredient: IngredientBreakdown,
    val group: IngredientDetailGroup,
)

internal fun matchesFlag(ingredient: IngredientBreakdown, flag: FlaggedItem): Boolean {
    val related = flag.relatedIngredient?.trim()?.lowercase()
    val title = flag.title.trim().lowercase()
    val verbatim = flag.verbatimSourceLine?.trim()?.lowercase()
    val canonical = ingredient.canonicalName.trim().lowercase()
    val raw = ingredient.rawName.trim().lowercase()
    val aliases = ingredient.matchedAliases.map { it.trim().lowercase() }

    if (!related.isNullOrBlank()) {
        if (related == canonical || related == raw || aliases.contains(related)) return true
        if (canonical.contains(related) || related.contains(canonical)) return true
        if (raw.isNotEmpty() && (raw.contains(related) || related.contains(raw))) return true
    }

    if (title.isNotBlank()) {
        if (title == canonical || title == raw || aliases.contains(title)) return true
        if (canonical.contains(title) || title.contains(canonical)) return true
        if (raw.isNotEmpty() && (raw.contains(title) || title.contains(raw))) return true
    }

    if (!verbatim.isNullOrBlank()) {
        if (verbatim == raw || verbatim == canonical || aliases.contains(verbatim)) return true
        if (raw.isNotEmpty() && (raw.contains(verbatim) || verbatim.contains(raw))) return true
    }

    return false
}

fun groupAndOrderIngredients(
    ingredients: List<IngredientBreakdown>,
    watchOut: List<FlaggedItem> = emptyList(),
    worthNoting: List<FlaggedItem> = emptyList(),
    goodToKnow: List<FlaggedItem> = emptyList(),
): List<GroupedIngredient> {
    val seenKeys = mutableSetOf<String>()
    val uniqueIngredients = mutableListOf<IngredientBreakdown>()
    for (item in ingredients) {
        val key = item.canonicalName.ifBlank { item.rawName }.trim().lowercase()
        if (key.isNotBlank()) {
            if (seenKeys.add(key)) {
                uniqueIngredients.add(item)
            }
        } else {
            uniqueIngredients.add(item)
        }
    }

    val watchOutList = mutableListOf<GroupedIngredient>()
    val worthNotingList = mutableListOf<GroupedIngredient>()
    val goodToKnowList = mutableListOf<GroupedIngredient>()
    val remainingList = mutableListOf<GroupedIngredient>()

    for (item in uniqueIngredients) {
        when {
            watchOut.any { matchesFlag(item, it) } -> {
                watchOutList.add(GroupedIngredient(item, IngredientDetailGroup.WATCH_OUT))
            }

            worthNoting.any { matchesFlag(item, it) } -> {
                worthNotingList.add(GroupedIngredient(item, IngredientDetailGroup.WORTH_NOTING))
            }

            goodToKnow.any { matchesFlag(item, it) } -> {
                goodToKnowList.add(GroupedIngredient(item, IngredientDetailGroup.GOOD_TO_KNOW))
            }

            else -> {
                remainingList.add(GroupedIngredient(item, IngredientDetailGroup.REMAINING))
            }
        }
    }

    return watchOutList + worthNotingList + goodToKnowList + remainingList
}

fun groupAndOrderIngredients(result: LabelAnalysisResult): List<GroupedIngredient> = groupAndOrderIngredients(
    ingredients = result.fullBreakdown,
    watchOut = result.watchOutForYou,
    worthNoting = result.worthNoting,
    goodToKnow = result.goodToKnow,
)
