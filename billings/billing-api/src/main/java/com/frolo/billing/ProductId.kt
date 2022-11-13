package com.frolo.billing


data class ProductId(val sku: String, val type: SkuType) {
    val skus: List<String> get() = listOf(sku)

    fun hasTheSameSkus(skus: List<String>): Boolean {
        val thisSkus = this.skus
        return thisSkus.containsAll(skus) && skus.containsAll(thisSkus)
    }
}