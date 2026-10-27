package com.labellens.app.presentation.components.ads

import com.labellens.app.monetization.ads.AdPlacement
import com.labellens.app.monetization.ads.AdRulesEngine
import com.labellens.app.util.logging.AppLogger

class RuleControlledAdDisplayer(
    private val delegate: FullScreenAdDisplayer,
    private val placement: AdPlacement,
    private val adRulesEngine: AdRulesEngine,
    private val onAdShown: (() -> Unit)? = null,
) : FullScreenAdDisplayer {

    override fun show() {
        if (adRulesEngine.canShowAd(placement)) {
            adRulesEngine.recordAdShown(placement)
            delegate.show()
            onAdShown?.invoke()
        } else {
            AppLogger.d("Ad display skipped by AdRulesEngine for placement: $placement")
        }
    }
}
