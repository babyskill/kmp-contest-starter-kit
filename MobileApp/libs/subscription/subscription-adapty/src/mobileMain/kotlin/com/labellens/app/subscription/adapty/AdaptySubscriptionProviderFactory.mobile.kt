package com.labellens.app.subscription.adapty

import com.labellens.app.subscription.api.SubscriptionProvider
import com.labellens.app.subscription.api.SubscriptionProviderFactory
import com.labellens.app.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = AdaptySubscriptionProvider()

            override fun createProviderUi(): SubscriptionProviderUi = AdaptySubscriptionProviderUi()
        }
