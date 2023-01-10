package com.frolo.billing

data class PurchaseHistoryRecord(
    val skus: List<String>,
    val purchaseTime: Long,
    val purchaseToken: String,
    val signature: String,
    val quantity: Int
)