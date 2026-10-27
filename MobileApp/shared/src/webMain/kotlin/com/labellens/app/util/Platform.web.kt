package com.labellens.app.util

import com.labellens.app.data.source.featureflag.FeatureFlagManager
import com.labellens.app.data.source.featureflag.NoImplFeatureFlagManager
import com.labellens.app.data.source.local.webDatabaseModule
import com.labellens.app.data.source.preferences.PreferencesDataStoreProvider
import com.labellens.app.data.source.preferences.PreferencesDataStoreProviderImpl
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.growth.analytics.mmp.NoImplMmpTracker
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.analytics.NoImplAnalytics
import com.labellens.app.util.file.FileManager
import com.labellens.app.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.local.LocalNotifications
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(webDatabaseModule)
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factoryOf(::AppUtilImpl) bind AppUtil::class
    // Singleton, not factory: the web FileManager holds an in-memory byte store, so every
    // consumer must share the same instance (a fresh factory instance would be empty).
    singleOf(::FileManagerImpl) bind FileManager::class
    single { NoImplFeatureFlagManager } bind FeatureFlagManager::class
    single(named("platformAnalytics")) { NoImplAnalytics } bind Analytics::class
    single<MmpTracker> { NoImplMmpTracker }
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(
        NotificationPlatformConfiguration.Web(askNotificationPermissionOnStart = false),
        LocalNotifications,
    )
}

actual fun getPlatform(): Platform = Platform.Web

internal actual val isAndroid: Boolean get() = false
internal actual val isDebug: Boolean get() = false

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.Default
