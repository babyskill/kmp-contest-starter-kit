package com.labellens.app.util

import com.labellens.app.data.source.featureflag.FeatureFlagManager
import com.labellens.app.data.source.local.DatabaseProvider
import com.labellens.app.data.source.local.DatabaseProviderImpl
import com.labellens.app.data.source.local.databaseModule
import com.labellens.app.data.source.preferences.PreferencesDataStoreProvider
import com.labellens.app.data.source.preferences.PreferencesDataStoreProviderImpl
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.presentation.components.ads.AdsManager
import com.labellens.app.presentation.components.ads.IosAdsDisplayer
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.file.FileManager
import com.labellens.app.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import com.mmk.kmpnotifier.push.firebase.onApplicationDidReceiveRemoteNotification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
    factoryOf(::AppUtilImpl) bind AppUtil::class
}

internal fun swiftLibDependenciesModule(factory: SwiftLibDependencyFactory): Module = module {
    single { factory.provideFeatureFlagManagerImpl() } bind FeatureFlagManager::class
    single(named("platformAnalytics")) { factory.provideFirebaseAnalyticsImpl() } bind Analytics::class
    single { factory.provideAdsManagerImpl() } bind AdsManager::class
    single { factory.provideIosAdsDisplayer() } bind IosAdsDisplayer::class
    single { factory.provideMmpTracker() } bind MmpTracker::class
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(NotificationPlatformConfiguration.Ios(askNotificationPermissionOnStart = false), FirebasePush)
}

// Bridge so the iOS app forwards remote-notification callbacks without the Swift side
// (or the exported framework) depending on KMPNotifier types directly.
object IosPushNotificationHandler {
    fun onApplicationDidReceiveRemoteNotification(userInfo: Map<Any?, *>) {
        KMPNotifier.onApplicationDidReceiveRemoteNotification(userInfo)
    }
}

actual fun getPlatform(): Platform = Platform.Ios

internal actual val isAndroid = false

@OptIn(ExperimentalNativeApi::class)
internal actual val isDebug = kotlin.native.Platform.isDebugBinary

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
