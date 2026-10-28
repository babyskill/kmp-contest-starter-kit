package com.labellens.app.data.source.remote.apiservices.barcode

import com.labellens.app.data.source.remote.response.barcode.OpenFoodFactsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

interface BarcodeLookupService {
    suspend fun lookupBarcode(barcode: String): OpenFoodFactsResponse
}

class OpenFoodFactsBarcodeLookupService(
    private val httpClient: HttpClient,
) : BarcodeLookupService {

    override suspend fun lookupBarcode(barcode: String): OpenFoodFactsResponse {
        val sanitized = barcode.trim()
        val url = "https://world.openfoodfacts.org/api/v2/product/$sanitized.json"
        return httpClient.get(url) {
            header("User-Agent", "LabelLens - Android/KMP - Version 2.0 (support@labellens.app)")
        }.body()
    }
}
