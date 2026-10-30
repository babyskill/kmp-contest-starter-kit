package com.labellens.app.util

import com.labellens.app.domain.model.AppLanguage
import org.koin.core.context.GlobalContext
import java.util.Locale

actual fun applyPlatformLanguage(language: AppLanguage) {
    val locale = Locale(language.code)
    Locale.setDefault(locale)
    try {
        val context = GlobalContext.get().get<android.content.Context>()
        val res = context.resources
        val config = android.content.res.Configuration(res.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    } catch (_: Throwable) {
    }
}
