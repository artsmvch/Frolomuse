package com.frolo.muse.billing


data class ProductDetails(
    val productId: ProductId,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val price: String,
    val priceCurrencyCode: String
)