package com.labellens.app.subscription.adapty

import com.labellens.app.subscription.api.SubscriptionProviderFactory

val SubscriptionProviderFactory.Companion.Adapty: SubscriptionProviderFactory
    get() = subscriptionProviderFactory

internal expect val subscriptionProviderFactory: SubscriptionProviderFactory
