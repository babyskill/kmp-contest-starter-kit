package com.labellens.app.util

import com.labellens.app.domain.model.AppLanguage
import org.koin.core.context.GlobalContext
import java.util.Locale

actual fun applyPlatformLanguage(language: AppLanguage) {
    val locale = Locale(language.code)
    Locale.setDefault(locale)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        val localeList = android.os.LocaleList(locale)
        android.os.LocaleList.setDefault(localeList)
    }
    try {
        val context = GlobalContext.get().get<android.content.Context>()
        val res = context.resources
        val config = android.content.res.Configuration(res.configuration)
        config.setLocale(locale)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.setLocales(android.os.LocaleList(locale))
        }
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    } catch (_: Throwable) {
    }
}
