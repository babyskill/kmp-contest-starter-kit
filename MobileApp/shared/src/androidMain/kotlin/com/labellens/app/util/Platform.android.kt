package com.labellens.app.util

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.labellens.app.common.BuildConfig
import com.labellens.app.data.source.featureflag.FeatureFlagManager
import com.labellens.app.data.source.featureflag.FeatureFlagManagerImpl
import com.labellens.app.data.source.local.DatabaseProvider
import com.labellens.app.data.source.local.DatabaseProviderImpl
import com.labellens.app.data.source.local.databaseModule
import com.labellens.app.data.source.preferences.PreferencesDataStoreProvider
import com.labellens.app.data.source.preferences.PreferencesDataStoreProviderImpl
import com.labellens.app.growth.analytics.mmp.CompositeAndroidMmpTracker
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.shared.R
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.analytics.FirebaseAnalyticsImpl
import com.labellens.app.util.file.FileManager
import com.labellens.app.util.file.FileManagerImpl
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
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
    single<FeatureFlagManagerImpl> {
        val remoteConfig = Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    // set minimumFetchIntervalInSeconds to 0 to get fresh updates in debug mode for testing
                    if (BuildConfig.DEBUG) minimumFetchIntervalInSeconds = 3600
                },
            )
            setDefaultsAsync(FeatureFlagManager.DEFAULT_VALUES)
        }
        FeatureFlagManagerImpl(remoteConfig = remoteConfig)
    } bind FeatureFlagManager::class
    single(named("platformAnalytics")) { FirebaseAnalyticsImpl(firebaseAnalytics = Firebase.analytics) } bind Analytics::class
    single<MmpTracker> { CompositeAndroidMmpTracker(context = get()) }
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(
        NotificationPlatformConfiguration.Android(
            notificationIconResId = R.drawable.ic_notification,
        ),
        FirebasePush,
    )
}

actual fun getPlatform(): Platform = Platform.Android

internal actual val isAndroid = true
internal actual val isDebug = BuildConfig.DEBUG
actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
