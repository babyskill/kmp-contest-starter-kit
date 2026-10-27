package com.labellens.app.monetization.entitlement

import com.labellens.app.data.repository.CreditRepository
import com.labellens.app.data.repository.SubscriptionRepository
import com.labellens.app.identity.SessionManager
import kotlinx.coroutines.flow.first

/**
 * Single entry point for checking user entitlements across subscriptions and credit balances.
 */
class EntitlementManager(
    private val subscriptionRepository: SubscriptionRepository? = null,
    private val creditRepository: CreditRepository? = null,
    private val sessionManager: SessionManager,
    private val premiumAccessChecker: (suspend () -> Boolean)? = null,
    private val creditBalanceProvider: (() -> Int)? = null,
) {
    suspend fun has(entitlement: Entitlement): Boolean = when (entitlement) {
        is Entitlement.Premium -> {
            val hasRepoAccess = premiumAccessChecker?.invoke()
                ?: subscriptionRepository?.hasPremiumAccess()
                ?: false
            hasRepoAccess || sessionManager.userIdentity.value.isPremium
        }

        is Entitlement.Lifetime -> {
            premiumAccessChecker?.invoke()
                ?: subscriptionRepository?.hasPremiumAccess()
                ?: false
        }

        is Entitlement.AiCredits -> {
            val balance = creditBalanceProvider?.invoke()
                ?: creditRepository?.balance?.first()
                ?: 0
            balance >= entitlement.minAmount
        }

        is Entitlement.Custom -> {
            false
        }
    }
}
