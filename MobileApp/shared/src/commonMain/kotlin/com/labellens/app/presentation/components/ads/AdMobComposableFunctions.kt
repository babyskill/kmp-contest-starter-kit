package com.labellens.app.presentation.components.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.labellens.app.monetization.ads.AdPlacement
import com.labellens.app.monetization.ads.AdRulesEngine
import com.labellens.app.util.logging.AppLogger
import org.koin.compose.koinInject

@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier,
    placement: AdPlacement = AdPlacement.BANNER_MAIN,
) {
    val adRulesEngine = koinInject<AdRulesEngine>()

    val isShowingAdsAllowed = remember(placement, adRulesEngine) {
        adRulesEngine.canShowAd(placement)
    }

    if (!isShowingAdsAllowed) {
        AppLogger.d("Showing Banner Ads is not allowed by AdRulesEngine for placement: $placement")
        return
    }

    NativeAdmobBanner(modifier)
}

@Composable
fun rememberInterstitialAdDisplayer(
    placement: AdPlacement = AdPlacement.INTERSTITIAL_EXPORT,
): FullScreenAdDisplayer {
    val adRulesEngine = koinInject<AdRulesEngine>()
    val nativeDisplayer = rememberNativeInterstitialAdDisplayer()
    return remember(nativeDisplayer, placement, adRulesEngine) {
        RuleControlledAdDisplayer(
            delegate = nativeDisplayer,
            placement = placement,
            adRulesEngine = adRulesEngine,
        )
    }
}

@Composable
fun rememberRewardedAdDisplayer(
    placement: AdPlacement = AdPlacement.REWARDED_UNLOCK,
    onRewarded: (AdsRewardItem) -> Unit,
): FullScreenAdDisplayer {
    val adRulesEngine = koinInject<AdRulesEngine>()
    val nativeDisplayer = rememberNativeRewardedAdDisplayer(onRewarded)
    return remember(nativeDisplayer, placement, adRulesEngine) {
        RuleControlledAdDisplayer(
            delegate = nativeDisplayer,
            placement = placement,
            adRulesEngine = adRulesEngine,
        )
    }
}

@Composable
expect fun NativeAdmobBanner(modifier: Modifier = Modifier)

@Composable
expect fun rememberNativeInterstitialAdDisplayer(): FullScreenAdDisplayer

@Composable
expect fun rememberNativeRewardedAdDisplayer(onRewarded: (AdsRewardItem) -> Unit): FullScreenAdDisplayer
