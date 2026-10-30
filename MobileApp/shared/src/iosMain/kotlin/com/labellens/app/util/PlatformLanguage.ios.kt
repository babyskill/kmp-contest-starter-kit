package com.labellens.app.util

import com.labellens.app.domain.model.AppLanguage
import platform.Foundation.NSUserDefaults

actual fun applyPlatformLanguage(language: AppLanguage) {
    try {
        NSUserDefaults.standardUserDefaults.setObject(
            listOf(language.code),
            forKey = "AppleLanguages",
        )
        NSUserDefaults.standardUserDefaults.synchronize()
    } catch (_: Throwable) {
    }
}
