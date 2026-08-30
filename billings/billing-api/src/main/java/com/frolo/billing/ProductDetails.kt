package com.frolo.billing


data class ProductDetails(
    override val productId: ProductId,
    val title: String,
    val description: String,
    val price: String,
    val priceCurrencyCode: String
): ProductIdAware