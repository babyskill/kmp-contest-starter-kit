package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val code: String, val displayName: String) {
    VIETNAMESE("vi", "Tiếng Việt"),
    ENGLISH("en", "English"),
    ;

    companion object {
        val DEFAULT: AppLanguage = VIETNAMESE

        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
}
