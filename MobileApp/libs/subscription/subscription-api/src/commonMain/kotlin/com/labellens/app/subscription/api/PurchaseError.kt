package com.labellens.app.subscription.api

data class PurchaseError(
    val message: String,
    val code: String? = null,
)
