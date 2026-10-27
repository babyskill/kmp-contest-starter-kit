package com.labellens.app.util

import com.labellens.app.data.source.featureflag.FeatureFlagManager
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.growth.analytics.mmp.NoImplMmpTracker
import com.labellens.app.presentation.components.ads.AdsManager
import com.labellens.app.presentation.components.ads.IosAdsDisplayer
import com.labellens.app.presentation.components.ads.NoImplAdsManager
import com.labellens.app.presentation.components.ads.NoImplIosAdsDisplayer
import com.labellens.app.util.analytics.Analytics

/**
This factory is used to help to use swift libraries in KMP. Actual implementations are provided in swift.
 */
interface SwiftLibDependencyFactory {
    fun provideFeatureFlagManagerImpl(): FeatureFlagManager
    fun provideFirebaseAnalyticsImpl(): Analytics
    fun provideAdsManagerImpl(): AdsManager = NoImplAdsManager
    fun provideIosAdsDisplayer(): IosAdsDisplayer = NoImplIosAdsDisplayer
    fun provideMmpTracker(): MmpTracker = NoImplMmpTracker
}
