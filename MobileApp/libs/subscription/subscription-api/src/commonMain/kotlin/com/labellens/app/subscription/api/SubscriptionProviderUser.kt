package com.labellens.app.subscription.api

class SubscriptionProviderUser(
    val grantedAccesses: Map<String, GrantedAccess>,
    val activeSubscriptionIds: Set<String>,
)
