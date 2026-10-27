package com.labellens.app.ads.config

import org.koin.core.module.Module
import org.koin.dsl.module

actual val activeAdsModule: Module = module {
    // On iOS, AdsManager is provided via SwiftLibDependencyFactory
}
