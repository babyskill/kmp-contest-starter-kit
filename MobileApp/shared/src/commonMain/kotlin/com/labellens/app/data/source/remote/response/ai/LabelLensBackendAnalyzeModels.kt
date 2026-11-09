package com.labellens.app.data.source.remote.response.ai

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class BackendAnalyzeRequest(
    @SerialName("ocrText") val ocrText: String,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("profile") val profile: BackendUserProfile? = null,
    @SerialName("language") val language: String? = null,
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
    @SerialName("summary") val summary: String? = null,
    @SerialName("fields") val fields: Map<String, JsonElement> = emptyMap(),
    @SerialName("sections") val sections: BackendSections = BackendSections(),
    @SerialName("disclaimer") val disclaimer: String? = null,
)

@Serializable(with = BackendSectionItemSerializer::class)
data class BackendSectionItem(
    val title: String,
    val value: String = "",
    val compare: String = "",
    val detail: String = "",
    val ingredient: String = "",
) {
    companion object {
        fun extractFirstClause(text: String, maxWords: Int = 6): String {
            if (text.isBlank()) return ""
            val trimmed = text.trim()
            val segments = trimmed.split(Regex("\\.\\s+|—|–|;|\\s+-\\s+"))
            val first = segments.firstOrNull()?.trim()?.replace(Regex("^[•\\-*\\s]+"), "")?.trim() ?: trimmed
            val words = first.split(Regex("\\s+")).filter { it.isNotBlank() }
            return if (words.size <= maxWords) first else words.take(maxWords).joinToString(" ")
        }

        fun fromLegacyString(legacy: String): BackendSectionItem {
            val shortTitle = extractFirstClause(legacy)
            return BackendSectionItem(
                title = shortTitle,
                value = "",
                compare = "",
                detail = legacy.trim(),
                ingredient = "",
            )
        }
    }
}

object BackendSectionItemSerializer : KSerializer<BackendSectionItem> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BackendSectionItem") {
            element<String>("title")
            element<String>("value", isOptional = true)
            element<String>("compare", isOptional = true)
            element<String>("detail", isOptional = true)
            element<String>("ingredient", isOptional = true)
        }

    override fun deserialize(decoder: Decoder): BackendSectionItem {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("BackendSectionItem requires JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> {
                val str = element.contentOrNull.orEmpty()
                BackendSectionItem.fromLegacyString(str)
            }

            is JsonObject -> {
                val rawTitle = element["title"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val rawValue = element["value"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val rawCompare = element["compare"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val rawDetail = element["detail"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val rawIngredient = element["ingredient"]?.jsonPrimitive?.contentOrNull.orEmpty()

                val effectiveDetail = rawDetail.ifBlank { rawTitle }
                val titleToClean = rawTitle.ifBlank { rawIngredient.ifBlank { effectiveDetail } }
                val cleanTitle = BackendSectionItem.extractFirstClause(titleToClean)

                BackendSectionItem(
                    title = cleanTitle,
                    value = rawValue,
                    compare = rawCompare,
                    detail = effectiveDetail,
                    ingredient = rawIngredient,
                )
            }

            else -> BackendSectionItem(title = "", value = "", compare = "", detail = "", ingredient = "")
        }
    }

    override fun serialize(encoder: Encoder, value: BackendSectionItem) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("BackendSectionItem requires JsonEncoder")
        val obj = buildJsonObject {
            put("title", value.title)
            put("value", value.value)
            put("compare", value.compare)
            put("detail", value.detail)
            put("ingredient", value.ingredient)
        }
        jsonEncoder.encodeJsonElement(obj)
    }
}

@Serializable
data class BackendSections(
    @SerialName("goodToKnow") val goodToKnow: List<BackendSectionItem> = emptyList(),
    @SerialName("worthNoting") val worthNoting: List<BackendSectionItem> = emptyList(),
    @SerialName("watchOut") val watchOut: List<BackendSectionItem> = emptyList(),
) {
    companion object {
        fun fromStrings(
            goodToKnow: List<String> = emptyList(),
            worthNoting: List<String> = emptyList(),
            watchOut: List<String> = emptyList(),
        ): BackendSections = BackendSections(
            goodToKnow = goodToKnow.map { BackendSectionItem.fromLegacyString(it) },
            worthNoting = worthNoting.map { BackendSectionItem.fromLegacyString(it) },
            watchOut = watchOut.map { BackendSectionItem.fromLegacyString(it) },
        )
    }
}
