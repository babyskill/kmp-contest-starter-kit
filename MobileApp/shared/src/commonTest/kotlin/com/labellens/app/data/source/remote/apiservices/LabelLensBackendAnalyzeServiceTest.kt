package com.labellens.app.data.source.remote.apiservices

import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeRequest
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

    private val fakeSuccessJson = """
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
    fun `successful response 200 parses DTO correctly`() = runTest {
        val client = createMockClient(fakeSuccessJson, HttpStatusCode.OK)
        val service = DefaultLabelLensBackendAnalyzeService(client)

        val request = BackendAnalyzeRequest(ocrText = "Water, Aspartame, Starch")
        val response = service.analyze(request, "valid-app-key")

        assertNotNull(response)
        assertEquals(2, response.tierUsed)
        assertEquals("cf-meta-llama-3-8b", response.model)
        assertEquals(0.88, response.confidence)
        assertEquals(listOf("Artificial Sweetener (Aspartame)"), response.data.sections.watchOut)
        assertEquals(listOf("Modified Corn Starch"), response.data.sections.worthNoting)
        assertEquals(listOf("No Added Sugars"), response.data.sections.goodToKnow)
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
        // Mock client throws exception if called
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
                        content = fakeSuccessJson,
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
                        content = fakeSuccessJson,
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
