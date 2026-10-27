package com.labellens.app.ads.config

import com.labellens.app.ads.admob.AdsManagerImpl
import com.labellens.app.presentation.components.ads.AdsManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val activeAdsModule: Module = module {
    single<AdsManager> {
        AdsManagerImpl(
            context = get(),
            adPaidEventListener = getOrNull(),
        )
    }
}
