package com.labellens.app.util

import com.labellens.app.domain.model.AppLanguage
import java.util.Locale

actual fun applyPlatformLanguage(language: AppLanguage) {
    try {
        Locale.setDefault(Locale(language.code))
    } catch (_: Throwable) {
    }
}
