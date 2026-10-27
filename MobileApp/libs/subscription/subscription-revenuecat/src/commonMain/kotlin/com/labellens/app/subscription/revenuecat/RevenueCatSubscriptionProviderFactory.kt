package com.labellens.app.subscription.revenuecat

import com.labellens.app.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.RevenueCat: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
