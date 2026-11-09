package com.labellens.app.data.source.remote.apiservices

import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeRequest
import com.labellens.app.data.source.remote.response.ai.BackendAnalyzeResponse
import com.labellens.app.root.AppConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.withTimeoutOrNull

interface LabelLensBackendAnalyzeService {
    suspend fun analyze(
        request: BackendAnalyzeRequest,
        appKey: String,
        language: String? = null,
    ): BackendAnalyzeResponse?
}

class DefaultLabelLensBackendAnalyzeService(
    private val httpClient: HttpClient,
    private val defaultLanguage: String? = null,
) : LabelLensBackendAnalyzeService {

    override suspend fun analyze(
        request: BackendAnalyzeRequest,
        appKey: String,
        language: String?,
    ): BackendAnalyzeResponse? {
        if (appKey.isBlank()) return null
        val effectiveLanguage = language ?: request.language ?: defaultLanguage
        val finalRequest = if (request.language == null && effectiveLanguage != null) {
            request.copy(language = effectiveLanguage)
        } else {
            request
        }
        return try {
            val response = httpClient.post(AppConfiguration.LABELLENS_BACKEND_URL) {
                header("x-app-key", appKey)
                contentType(ContentType.Application.Json)
                setBody(finalRequest)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<BackendAnalyzeResponse>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
