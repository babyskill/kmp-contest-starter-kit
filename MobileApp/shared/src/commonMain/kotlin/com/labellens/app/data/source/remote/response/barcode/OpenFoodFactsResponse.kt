package com.labellens.app.data.source.remote.response.barcode

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    val code: String? = null,
    val status: Int? = null,
    @SerialName("status_verbose")
    val statusVerbose: String? = null,
    val product: OpenFoodFactsProduct? = null,
)

@Serializable
data class OpenFoodFactsProduct(
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("ingredients_text")
    val ingredientsText: String? = null,
    val allergens: String? = null,
    val nutriments: OpenFoodFactsNutriments? = null,
    val brands: String? = null,
)

@Serializable
data class OpenFoodFactsNutriments(
    @SerialName("energy-kcal")
    val energyKcal: Double? = null,
    val sugars: Double? = null,
    @SerialName("added-sugars")
    val addedSugars: Double? = null,
    val sodium: Double? = null,
    @SerialName("saturated-fat")
    val saturatedFat: Double? = null,
    val proteins: Double? = null,
    val fiber: Double? = null,
)
