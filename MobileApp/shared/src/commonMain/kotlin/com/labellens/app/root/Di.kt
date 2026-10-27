package com.labellens.app.root

import com.labellens.app.ads.config.activeAdsModule
import com.labellens.app.common.BuildConfig
import com.labellens.app.core.consent.ConsentManager
import com.labellens.app.core.navigation.DeepLinkManager
import com.labellens.app.core.navigation.DeepLinkParser
import com.labellens.app.data.BackgroundExecutor
import com.labellens.app.data.repository.AttributionRepository
import com.labellens.app.data.repository.CreditRepository
import com.labellens.app.data.repository.GenerationRepository
import com.labellens.app.data.repository.SubscriptionRepository
import com.labellens.app.data.repository.UserRepository
import com.labellens.app.data.source.ai.ReplicateGenerationProvider
import com.labellens.app.data.source.preferences.PreferencesDataStoreProvider
import com.labellens.app.data.source.preferences.UserPreferences
import com.labellens.app.data.source.preferences.UserPreferencesImpl
import com.labellens.app.data.source.remote.HttpClientFactory
import com.labellens.app.data.source.remote.apiservices.ApiService
import com.labellens.app.data.source.remote.apiservices.TemporaryFileUploadApiService
import com.labellens.app.data.source.remote.apiservices.ai.AiTransport
import com.labellens.app.data.source.remote.apiservices.ai.OpenAiApiService
import com.labellens.app.data.source.remote.apiservices.ai.ReplicateApiService
import com.labellens.app.data.source.remote.apiservices.attribution.AttributionApiService
import com.labellens.app.domain.model.credit.creditSystemConfig
import com.labellens.app.domain.usecase.AiGenerationProvider
import com.labellens.app.growth.analytics.AnalyticsRouter
import com.labellens.app.growth.analytics.mmp.MmpAnalyticsDestination
import com.labellens.app.growth.analytics.mmp.MmpConfig
import com.labellens.app.growth.analytics.mmp.MmpEnvironment
import com.labellens.app.growth.analytics.mmp.MmpTracker
import com.labellens.app.growth.experiment.ExperimentEngine
import com.labellens.app.identity.SessionManager
import com.labellens.app.monetization.ads.AdRulesEngine
import com.labellens.app.monetization.entitlement.EntitlementManager
import com.labellens.app.presentation.components.ads.AdPaidEventListener
import com.labellens.app.presentation.screens.account.AccountViewModel
import com.labellens.app.presentation.screens.creditbalance.CreditBalanceViewModel
import com.labellens.app.presentation.screens.debug.DebugMenuViewModel
import com.labellens.app.presentation.screens.gallery.GalleryViewModel
import com.labellens.app.presentation.screens.generationresult.GenerationResultViewModel
import com.labellens.app.presentation.screens.home.HomeViewModel
import com.labellens.app.presentation.screens.onboarding.OnBoardingViewModel
import com.labellens.app.presentation.screens.paywall.PaywallViewModel
import com.labellens.app.presentation.screens.profile.ProfileViewModel
import com.labellens.app.presentation.screens.subscriptions.SubscriptionsViewModel
import com.labellens.app.subscription.api.MockSubscriptionProvider
import com.labellens.app.subscription.api.NoOpSubscriptionProviderUi
import com.labellens.app.subscription.api.SubscriptionProvider
import com.labellens.app.subscription.api.SubscriptionProviderFactory
import com.labellens.app.subscription.api.SubscriptionProviderUi
import com.labellens.app.util.ApplicationScope
import com.labellens.app.util.Constants
import com.labellens.app.util.analytics.Analytics
import com.labellens.app.util.analytics.NoImplAnalytics
import com.labellens.app.util.analytics.logAdImpression
import com.labellens.app.util.defaultAsyncDispatcher
import com.labellens.app.util.extensions.nowEpochMillis
import com.labellens.app.util.isAndroid
import com.labellens.app.util.isDebug
import com.labellens.app.util.logging.Logger
import com.labellens.app.util.logging.NapierLogger
import com.labellens.app.util.logging.TelegramLogger
import com.labellens.app.util.platformModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/**
 * Koin module graph for the shared app. [AppInitializer] loads [appModules] at startup.
 * Layered by concern: [domainModule] (pure, empty), [dataModule] (infra + repositories),
 * [presentationModule] (ViewModels), plus the per-target expect/actual `platformModule`.
 */

// Empty by design — the domain layer is pure (models/exceptions), nothing to inject.
private val domainModule = module {
}

// Infrastructure + repositories: scopes/dispatchers, preferences, network, auth & subscription
// providers (selected via Constants), repositories, loggers, AI provider, and the credit system.
private val dataModule = module {
    singleOf(::ApplicationScope)
    factory { defaultAsyncDispatcher } bind CoroutineContext::class
    factory { BackgroundExecutor.IO } bind BackgroundExecutor::class

    // Preferences Source. The DataStore instance stays out of the Koin graph on
    // purpose — generic types erase to `DataStore`, so a second DataStore<T>
    // registered later would silently collide with this one.
    single { UserPreferencesImpl(get<PreferencesDataStoreProvider>().providePreferencesDataStore()) } bind UserPreferences::class

    single { SessionManager(get(), { get<UserRepository>() }, get()) }
    singleOf(::ExperimentEngine)
    single { ConsentManager(userPreferences = get(), coroutineScope = getOrNull<ApplicationScope>()) }
    single { DeepLinkParser() }
    single { DeepLinkManager(parser = get(), attributionRepository = getOrNull()) }
    single { AdRulesEngine(sessionManager = get(), featureFlagManager = get(), userPreferences = get(), scope = getOrNull<ApplicationScope>()) }
    includes(activeAdsModule)
    single<AdPaidEventListener> {
        val analytics = getOrNull<Analytics>()
        AdPaidEventListener { revenue, currency, adPlatform, adFormat ->
            analytics?.logAdImpression(
                value = revenue,
                currency = currency,
                adPlatform = adPlatform,
                adFormat = adFormat,
            )
        }
    }
    single { EntitlementManager(subscriptionRepository = get(), creditRepository = get(), sessionManager = get()) }

    // Unified MMP Tracking & Analytics Router (Adjust or AppsFlyer)
    single {
        MmpConfig(
            provider = AppConfiguration.MMP_PROVIDER,
            environment = if (isDebug) MmpEnvironment.SANDBOX else MmpEnvironment.PRODUCTION,
            isAdRevenueTrackingEnabled = true,
            adjustAppTokenAndroid = AppConfiguration.ADJUST_APP_TOKEN_ANDROID,
            adjustAppTokenIos = AppConfiguration.ADJUST_APP_TOKEN_IOS,
            adjustEventTokens = AppConfiguration.ADJUST_EVENT_TOKENS,
            appsFlyerDevKey = AppConfiguration.APPSFLYER_DEV_KEY,
            appsFlyerAppIdIos = AppConfiguration.APPSFLYER_APP_ID_IOS,
            appsFlyerEventTokens = AppConfiguration.APPSFLYER_EVENT_TOKENS,
        )
    }
    singleOf(::MmpAnalyticsDestination)
    single {
        AnalyticsRouter(
            primaryAnalytics = getOrNull<Analytics>(named("platformAnalytics")) ?: NoImplAnalytics,
            destinations = listOf(get<MmpAnalyticsDestination>()),
            isLoggingEnabled = isDebug,
        )
    }
    single<Analytics> { get<AnalyticsRouter>() }

    // Remote source
    single { HttpClientFactory.default(sessionManager = get()) }
    single(named("aiDirectClient")) { HttpClientFactory.noAuth() }
    single { AiTransport(proxyClient = get(), directClient = get(named("aiDirectClient"))) }
    single { TemporaryFileUploadApiService(HttpClientFactory.fileUpload()) }
    single { AttributionApiService(HttpClientFactory.noAuth()) }

    factoryOf(::ApiService)
    factoryOf(::OpenAiApiService)
    factoryOf(::ReplicateApiService)

    // Subscription Provider. When no real SDK key is set (isSubscriptionMockActive), swap in the
    // MockSubscriptionProvider so the paywall/purchase/unlock flow is explorable with zero keys.
    // Auto-reverts to the real provider (Adapty/RevenueCat) the moment a key is configured.
    factory { AppConfiguration.subscriptionProviderFactory } bind SubscriptionProviderFactory::class
    single {
        if (isSubscriptionMockActive()) {
            val userPreferences = get<UserPreferences>()
            MockSubscriptionProvider(
                readPremiumPurchased = {
                    userPreferences.getBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, false)
                },
                writePremiumPurchased = {
                    userPreferences.putBoolean(MockSubscriptionProvider.KEY_MOCK_PREMIUM_PURCHASED, it)
                },
                premiumAccessId = Constants.PAYWALL_PREMIUM_ACCESS,
                creditPackPrefix = Constants.CREDIT_PACK_PRODUCT_ID_PREFIX,
                creditPackPlacementId = Constants.PAYWALL_PLACEMENT_CREDITS_PACK,
                currentTimeMillis = ::nowEpochMillis,
            )
        } else {
            get<SubscriptionProviderFactory>().createProvider()
        }
    } bind SubscriptionProvider::class
    factory {
        if (isSubscriptionMockActive()) {
            NoOpSubscriptionProviderUi
        } else {
            get<SubscriptionProviderFactory>().createProviderUi()
        }
    } bind SubscriptionProviderUi::class

    // Repositories
    single { UserRepository(get(), get(), get(), get(), get()) }
    single { SubscriptionRepository(get(), get(), get(), get()) }
    single { GenerationRepository(get(), get(), get(), get(), get(), get(), get()) }
    single { AttributionRepository(get(), get()) }
    single { com.labellens.app.domain.analyzer.LabelAnalyzer() }
    single<com.labellens.app.data.repository.ScanRepository> {
        com.labellens.app.data.repository.ScanRepositoryImpl(get<com.labellens.app.data.source.local.AppDatabase>().scanHistoryDao(), get(), get())
    }
    single<com.labellens.app.data.repository.UserProfileRepository> {
        com.labellens.app.data.repository.UserProfileRepositoryImpl(get())
    }

    // Loggers
    factory { TelegramLogger(get(), get(), get()) } bind Logger::class
    factory { NapierLogger() } bind Logger::class

//    factory<AiGenerationProvider> { OpenAiImageGenerationProvider(get(), get()) }
    factory<AiGenerationProvider> { ReplicateGenerationProvider(get(), get()) }

    initializeCreditSystem()
}

// ViewModels, scoped per NavEntry. PaywallViewModel takes a placementId param.
private val presentationModule = module {
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GalleryViewModel)
    viewModel {
        ProfileViewModel(
            userRepository = get(),
            userPreferences = get(),
            appUtil = get(),
            subscriptionRepository = get(),
            analytics = get(),
        )
    }
    viewModel { params ->
        PaywallViewModel(
            placementId = params.values.getOrNull(0) as? String,
            subscriptionRepository = get(),
            creditRepository = get(),
            userRepository = get(),
            featureFlagManager = get(),
            userPreferences = get(),
            analytics = get(),
            userGoal = params.values.getOrNull(1) as? String,
            userBarrier = params.values.getOrNull(2) as? String,
            userDailyMinutes = params.values.getOrNull(3) as? Int,
        )
    }
    viewModelOf(::AccountViewModel)
    viewModelOf(::SubscriptionsViewModel)
    viewModelOf(::GenerationResultViewModel)
    viewModelOf(::CreditBalanceViewModel)
    viewModelOf(::DebugMenuViewModel)

    viewModel { com.labellens.app.presentation.screens.scan.ScanViewModel(get()) }
    viewModel { com.labellens.app.presentation.screens.result.ResultViewModel(get(), get(), get()) }
    viewModel { com.labellens.app.presentation.screens.profile.LabelLensProfileViewModel(get()) }
    viewModel { com.labellens.app.presentation.screens.history.HistoryViewModel(get()) }
    viewModel { com.labellens.app.presentation.screens.compare.CompareViewModel(get(), get()) }

    // Add new view models below — generate_screen.sh inserts here.
}

private fun Module.initializeCreditSystem() {
    single {
        val subscriptionRepository = get<SubscriptionRepository>()
        val appCreditSystemConfig = creditSystemConfig {
            oneTimeBonus("welcome_bonus_credit", 1)
//            val userPreferences = get<UserPreferences>()
//            oneTimeBonus(
//                id = "referral_bonus",
//                amount = 1,
//                condition = {
//                    userPreferences.getBoolean(UserPreferences.KEY_REFERRAL_COMPLETED)
//                }
//            )
//            recurringWeekly(
//                id = "free_plan_weekly",
//                amount = 2,
//                condition = {
//                    !subscriptionRepository.hasPremiumAccess()
//                }
//            )

            recurringWeekly(
                id = "premium_plan_weekly", // Gives all premium users 10 credits per week
                amount = 10,
                condition = {
                    subscriptionRepository.hasPremiumAccess()
                },
            )
        }

        CreditRepository(appCreditSystemConfig, get(), get(), get(), get())
    }
}

/**
 * True when the active-platform subscription SDK key is still a placeholder, i.e. no real
 * Adapty/RevenueCat account is wired yet. While true the app runs [MockSubscriptionProvider] so the
 * whole paywall → purchase → unlock flow is explorable with zero keys. Auto-off once a real key is set.
 */
private fun isSubscriptionMockActive(): Boolean {
    val key =
        if (isAndroid) {
            BuildConfig.SUBSCRIPTION_PROVIDER_ANDROID_API_KEY
        } else {
            BuildConfig.SUBSCRIPTION_PROVIDER_IOS_API_KEY
        }
    return key.isBlank() || key == MockSubscriptionProvider.PLACEHOLDER_KEY
}

// All Koin modules loaded at startup. platformModule is the expect/actual per-target module.
internal val appModules: List<Module> get() = platformModule + domainModule + dataModule + presentationModule
