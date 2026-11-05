package com.labellens.app.data.source.remote.response.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BackendAnalyzeRequest(
    @SerialName("ocrText") val ocrText: String,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("profile") val profile: BackendUserProfile? = null,
)

@Serializable
data class BackendUserProfile(
    @SerialName("allergens") val allergens: List<String>? = null,
    @SerialName("flags") val flags: List<String>? = null,
)

@Serializable
data class BackendAnalyzeResponse(
    @SerialName("tierUsed") val tierUsed: Int,
    @SerialName("escalated") val escalated: Boolean = false,
    @SerialName("tier3") val tier3: String? = null,
    @SerialName("model") val model: String,
    @SerialName("confidence") val confidence: Double = 0.0,
    @SerialName("data") val data: BackendAnalyzeData,
)

@Serializable
data class BackendAnalyzeData(
    @SerialName("fields") val fields: Map<String, JsonElement> = emptyMap(),
    @SerialName("sections") val sections: BackendSections = BackendSections(),
    @SerialName("disclaimer") val disclaimer: String? = null,
)

@Serializable
data class BackendSections(
    @SerialName("goodToKnow") val goodToKnow: List<String> = emptyList(),
    @SerialName("worthNoting") val worthNoting: List<String> = emptyList(),
    @SerialName("watchOut") val watchOut: List<String> = emptyList(),
)
