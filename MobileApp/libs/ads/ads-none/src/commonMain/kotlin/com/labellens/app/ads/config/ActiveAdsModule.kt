package com.labellens.app.ads.config

import com.labellens.app.presentation.components.ads.AdsManager
import com.labellens.app.presentation.components.ads.NoImplAdsManager
import org.koin.core.module.Module
import org.koin.dsl.module

val activeAdsModule: Module = module {
    single<AdsManager> { NoImplAdsManager }
}
