package com.labellens.app.monetization.entitlement

sealed interface Entitlement {
    data object Premium : Entitlement
    data object Lifetime : Entitlement
    data class AiCredits(val minAmount: Int = 1) : Entitlement
    data class Custom(val id: String) : Entitlement
}
