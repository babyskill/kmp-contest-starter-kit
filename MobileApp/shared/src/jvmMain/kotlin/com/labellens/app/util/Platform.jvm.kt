package com.labellens.app.util

import com.labellens.app.data.source.featureflag.FeatureFlagManager
import com.labellens.app.data.source.featureflag.NoImplFeatureFlagManager
import com.labellens.app.data.source.local.DatabaseProvider
import com.labellens.app.data.source.local.DatabaseProviderImpl
import com.labellens.app.data.source.local.databaseModule
import com.labellens.app.data.source.preferences.PreferencesDataStoreProvider
import com.labellens.app.data.source.preferences.PreferencesDataStoreProviderImpl
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.growth.analytics.mmp.NoImplMmpTracker
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.analytics.NoImplAnalytics
import com.labellens.app.util.file.FileManager
import com.labellens.app.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
    factoryOf(::AppUtilImpl) bind AppUtil::class
    single { NoImplFeatureFlagManager } bind FeatureFlagManager::class
    single(named("platformAnalytics")) { NoImplAnalytics } bind Analytics::class
    single<MmpTracker> { NoImplMmpTracker }
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(NotificationPlatformConfiguration.Desktop(), FirebasePush)
    FileKit.init("com.labellens.app")
}

actual fun getPlatform(): Platform = Platform.Desktop

internal actual val isAndroid: Boolean
    get() = false
internal actual val isDebug: Boolean
    get() = System.getProperty("app.debug") == "true" ||
        System.getenv("APP_DEBUG") == "true"

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
