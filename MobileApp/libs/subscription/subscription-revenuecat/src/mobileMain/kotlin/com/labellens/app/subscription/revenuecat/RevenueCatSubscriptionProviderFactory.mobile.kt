package com.labellens.app.subscription.revenuecat

import com.labellens.app.subscription.api.SubscriptionProvider
import com.labellens.app.subscription.api.SubscriptionProviderFactory
import com.labellens.app.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = RevenueCatSubscriptionProvider()

            override fun createProviderUi(): SubscriptionProviderUi = RevenueCatSubscriptionProviderUi()
        }
