package com.frolo.billing


data class ProductDetails(
    override val productId: ProductId,
    val title: String,
    val description: String,
    val iconUrl: String?,
    val price: String,
    val priceCurrencyCode: String
): ProductIdAware