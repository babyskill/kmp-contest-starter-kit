package com.labellens.app.data.source.remote.apiservices.barcode

import com.labellens.app.data.source.remote.response.barcode.toLabelText
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenFoodFactsBarcodeLookupServiceTest {

    private val fakeSuccessJson = """
    {
      "code": "737628064502",
      "status": 1,
      "status_verbose": "product found",
      "product": {
        "product_name": "Thai Peanut Noodle Kit",
        "ingredients_text": "Rice Noodles (rice, water), seasoning packet (peanut, sugar, salt).",
        "allergens": "peanuts, soybeans",
        "nutriments": {
          "energy-kcal": 385.0,
          "sugars": 13.5,
          "added-sugars": 12.0,
          "sodium": 0.288,
          "saturated-fat": 1.9,
          "proteins": 9.6,
          "fiber": 1.9
        }
      }
    }
    """.trimIndent()

    private val fakeNotFoundJson = """
    {
      "code": "0000000000000",
      "status": 0,
      "status_verbose": "product not found"
    }
    """.trimIndent()

    private fun createMockClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        engine {
            addHandler { _ ->
                respond(
                    content = responseBody,
                    status = statusCode,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
    }

    @Test
    fun testLookupBarcodeSuccess() = runTest {
        val client = createMockClient(fakeSuccessJson)
        val service = OpenFoodFactsBarcodeLookupService(client)

        val response = service.lookupBarcode("737628064502")
        assertEquals(1, response.status)
        assertEquals("Thai Peanut Noodle Kit", response.product?.productName)

        val labelText = response.toLabelText()
        assertTrue(labelText.contains("Rice Noodles"))
        assertTrue(labelText.contains("Calories: 385 kcal"))
        assertTrue(labelText.contains("Sodium: 288 mg"))
        assertTrue(labelText.contains("CONTAINS: peanuts, soybeans"))
    }

    @Test
    fun testLookupBarcodeNotFound() = runTest {
        val client = createMockClient(fakeNotFoundJson)
        val service = OpenFoodFactsBarcodeLookupService(client)

        val response = service.lookupBarcode("0000000000000")
        assertEquals(0, response.status)
        assertEquals("", response.toLabelText())
    }
}
