package com.labellens.app.data.source.remote.apiservices

import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeRequest
import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeResponse
import com.labellens.app.data.source.remote.response.ai.BackendSectionItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LabelLensBackendAnalyzeServiceTest {

    private val fakeSuccessLegacyJson = """
    {
      "tierUsed": 2,
      "escalated": false,
      "tier3": "skipped_low_confidence_ok",
      "model": "cf-meta-llama-3-8b",
      "confidence": 0.88,
      "data": {
        "fields": {
          "brand": "SampleBrand"
        },
        "sections": {
          "watchOut": ["Artificial Sweetener (Aspartame)"],
          "worthNoting": ["Modified Corn Starch"],
          "goodToKnow": ["No Added Sugars"]
        },
        "disclaimer": "AI generated label analysis"
      }
    }
    """.trimIndent()

    private val fakeSuccessStructuredJson = """
    {
      "tierUsed": 2,
      "escalated": false,
      "tier3": "skipped_low_confidence_ok",
      "model": "deepseek-chat",
      "confidence": 0.95,
      "data": {
        "summary": "Sữa tươi tiệt trùng nguyên chất, không đường — nhưng có sữa.",
        "fields": {
          "brand": "TH true MILK"
        },
        "sections": {
          "goodToKnow": [
            {
              "title": "Sữa bò tươi 100%",
              "value": "100%",
              "compare": "",
              "detail": "Sản phẩm được làm hoàn toàn từ sữa bò tươi nguyên chất.",
              "ingredient": "sữa bò tươi"
            }
          ],
          "worthNoting": [
            {
              "title": "Năng lượng",
              "value": "60,1 kcal",
              "compare": "≈ 1/4 bát cơm",
              "detail": "Mức năng lượng vừa phải cho bữa phụ.",
              "ingredient": ""
            }
          ],
          "watchOut": [
            {
              "title": "Chứa sữa (dị ứng)",
              "value": "",
              "compare": "",
              "detail": "Sản phẩm chứa sữa bò, người dị ứng cần tránh.",
              "ingredient": "sữa bò tươi"
            }
          ]
        }
      }
    }
    """.trimIndent()

    private val fakeMalformedJson = """
    {
      "tierUsed": "not-a-number",
      "data": {
        "sections": "not-an-object"
      }
    }
    """.trimIndent()

    private fun createMockClient(
        responseBody: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient = HttpClient(MockEngine) {
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
    fun `successful response 200 parses legacy DTO correctly`() = runTest {
        val client = createMockClient(fakeSuccessLegacyJson, HttpStatusCode.OK)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Water, Aspartame, Starch")
        val response = service.analyze(request, "valid-app-key")

        assertNotNull(response)
        assertEquals(2, response.tierUsed)
        assertEquals("cf-meta-llama-3-8b", response.model)
        assertEquals(0.88, response.confidence)
        assertEquals("Artificial Sweetener (Aspartame)", response.data.sections.watchOut.first().title)
        assertEquals("Modified Corn Starch", response.data.sections.worthNoting.first().title)
        assertEquals("No Added Sugars", response.data.sections.goodToKnow.first().title)
    }

    @Test
    fun `successful response 200 parses new structured objects with summary correctly`() = runTest {
        val client = createMockClient(fakeSuccessStructuredJson, HttpStatusCode.OK)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Sua tuoi TH")
        val response = service.analyze(request, "valid-app-key")

        assertNotNull(response)
        assertEquals("deepseek-chat", response.model)
        assertEquals("Sữa tươi tiệt trùng nguyên chất, không đường — nhưng có sữa.", response.data.summary)

        val goodItem = response.data.sections.goodToKnow.first()
        assertEquals("Sữa bò tươi 100%", goodItem.title)
        assertEquals("100%", goodItem.value)
        assertEquals("", goodItem.compare)
        assertEquals("Sản phẩm được làm hoàn toàn từ sữa bò tươi nguyên chất.", goodItem.detail)
        assertEquals("sữa bò tươi", goodItem.ingredient)

        val worthItem = response.data.sections.worthNoting.first()
        assertEquals("Năng lượng", worthItem.title)
        assertEquals("60,1 kcal", worthItem.value)
        assertEquals("≈ 1/4 bát cơm", worthItem.compare)

        val watchItem = response.data.sections.watchOut.first()
        assertEquals("Chứa sữa (dị ứng)", watchItem.title)
        assertEquals("sữa bò tươi", watchItem.ingredient)
    }

    @Test
    fun `legacy string item renders with title as first clause and detail as full string`() {
        val item1 = BackendSectionItem.fromLegacyString("High dietary fiber. Good for gut health and digestion.")
        assertEquals("High dietary fiber", item1.title)
        assertEquals("High dietary fiber. Good for gut health and digestion.", item1.detail)

        val item2 = BackendSectionItem.fromLegacyString("Sữa tươi nguyên chất 100% — Chỉ chứa sữa tươi nguyên chất.")
        assertEquals("Sữa tươi nguyên chất 100%", item2.title)
        assertEquals("Sữa tươi nguyên chất 100% — Chỉ chứa sữa tươi nguyên chất.", item2.detail)

        val item3 = BackendSectionItem.fromLegacyString("High Sodium; may increase blood pressure risks.")
        assertEquals("High Sodium", item3.title)
        assertEquals("High Sodium; may increase blood pressure risks.", item3.detail)
    }

    @Test
    fun `sentence title guard ensures a full sentence can never again become a row title`() {
        val longSentence = "Sữa tươi tiệt trùng nguyên chất thơm ngon bổ dưỡng được sản xuất theo quy trình hiện đại khép kín."
        val shortTitle = BackendSectionItem.extractFirstClause(longSentence, maxWords = 6)
        assertTrue(shortTitle.split(Regex("\\s+")).size <= 6)

        // When deserializing from JSON with an overlong sentence title
        val jsonString = """
        {
          "tierUsed": 2,
          "model": "test",
          "data": {
            "sections": {
              "goodToKnow": [
                {
                  "title": "This is a full sentence explaining the entire benefit of the nutrient. It has multiple sentences.",
                  "detail": "Full explanation text"
                }
              ]
            }
          }
        }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val parsed = json.decodeFromString<BackendAnalyzeResponse>(jsonString)
        val title = parsed.data.sections.goodToKnow.first().title
        assertTrue(title.split(Regex("\\s+")).size <= 6)
        assertTrue(!title.contains("."))
    }

    @Test
    fun `malformed json response returns null silently`() = runTest {
        val client = createMockClient(fakeMalformedJson, HttpStatusCode.OK)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Some text")
        val response = service.analyze(request, "valid-app-key")

        assertNull(response)
    }

    @Test
    fun `http 401 unauthorized returns null silently`() = runTest {
        val client = createMockClient("""{"error": "Unauthorized"}""", HttpStatusCode.Unauthorized)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Some text")
        val response = service.analyze(request, "invalid-key")

        assertNull(response)
    }

    @Test
    fun `http 500 server error returns null silently`() = runTest {
        val client = createMockClient("""{"error": "Internal Error"}""", HttpStatusCode.InternalServerError)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Some text")
        val response = service.analyze(request, "valid-key")

        assertNull(response)
    }

    @Test
    fun `blank app key returns null without making network request`() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { _ ->
                    throw IllegalStateException("Should not be called")
                }
            }
        }
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val response = service.analyze(BackendAnalyzeRequest(ocrText = "Text"), "")
        assertNull(response)
    }

    @Test
    fun `BackendAnalyzeRequest serializes language field for vi`() {
        val json = Json { explicitNulls = false }
        val request = BackendAnalyzeRequest(ocrText = "Tra xanh", language = "vi")
        val serialized = json.encodeToString(BackendAnalyzeRequest.serializer(), request)
        assertTrue(serialized.contains("\"language\":\"vi\""))
    }

    @Test
    fun `BackendAnalyzeRequest serializes language field for en`() {
        val json = Json { explicitNulls = false }
        val request = BackendAnalyzeRequest(ocrText = "Green tea", language = "en")
        val serialized = json.encodeToString(BackendAnalyzeRequest.serializer(), request)
        assertTrue(serialized.contains("\"language\":\"en\""))
    }

    @Test
    fun `analyze passes language into network request body`() = runTest {
        var recordedBody = ""
        val client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { requestData ->
                    recordedBody = requestData.body.toByteArray().decodeToString()
                    respond(
                        content = fakeSuccessLegacyJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        }
        val service = DefaultLabelLensBackendAnalyzeService(client)
        val request = BackendAnalyzeRequest(ocrText = "Tra xanh", language = "vi")
        val response = service.analyze(request, "valid-key")

        assertNotNull(response)
        assertTrue(recordedBody.contains("\"language\":\"vi\""))
    }

    @Test
    fun `analyze with explicit language parameter sets language in request body`() = runTest {
        var recordedBody = ""
        val client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { requestData ->
                    recordedBody = requestData.body.toByteArray().decodeToString()
                    respond(
                        content = fakeSuccessLegacyJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        }
        val service = DefaultLabelLensBackendAnalyzeService(client)
        val request = BackendAnalyzeRequest(ocrText = "Green tea")
        val response = service.analyze(request, "valid-key", language = "en")

        assertNotNull(response)
        assertTrue(recordedBody.contains("\"language\":\"en\""))
    }
}
