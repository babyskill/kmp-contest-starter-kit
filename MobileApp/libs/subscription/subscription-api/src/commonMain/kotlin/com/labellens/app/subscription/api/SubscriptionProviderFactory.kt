package com.labellens.app.subscription.api

interface SubscriptionProviderFactory {
    companion object

    fun createProvider(): SubscriptionProvider

    fun createProviderUi(): SubscriptionProviderUi
}
