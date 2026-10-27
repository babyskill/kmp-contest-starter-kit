package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class KnowledgeIngredient(
    val canonicalName: String,
    val aliases: List<String>,
    val category: String,
    val plainExplanation: String,
    val whyUsed: String,
    val doseContextNote: String,
    val avoidForTags: List<String> = emptyList(), // e.g. "soy", "gluten", "added-sugar", "artificial-sweetener", "msg", etc.
    val allergenMatch: Allergen? = null,
    val avoidMatch: AvoidPreference? = null,
    val isGenerallyPositive: Boolean = false,
)
