@file:OptIn(ExperimentalTime::class)

package com.labellens.app.devtools

import com.labellens.app.identity.SessionManager
import com.labellens.app.util.getPlatform
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object DiagnosticsHelper {
    private val json = Json { prettyPrint = true }

    fun generateDiagnosticsReport(
        sessionManager: SessionManager,
        environment: String = "DEV",
        appVersion: String = "1.0.0",
    ): String {
        val identity = sessionManager.userIdentity.value
        val report = buildJsonObject {
            put("app_name", "Koko")
            put("app_version", appVersion)
            put(
                "platform",
                try {
                    getPlatform().toString()
                } catch (_: Throwable) {
                    "Unknown"
                },
            )
            put("environment", environment)
            put("anonymous_id", sessionManager.getAnonymousId())
            put("firebase_uid", identity.firebaseUid)
            put("is_premium", identity.isPremium)
            put("timestamp", Clock.System.now().toString())
        }
        return json.encodeToString(report)
    }
}

fun generateDiagnosticsReport(
    sessionManager: SessionManager,
    environment: String = "DEV",
    appVersion: String = "1.0.0",
): String = DiagnosticsHelper.generateDiagnosticsReport(
    sessionManager = sessionManager,
    environment = environment,
    appVersion = appVersion,
)
