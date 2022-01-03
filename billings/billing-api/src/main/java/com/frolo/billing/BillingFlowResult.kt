package com.frolo.billing


sealed class BillingFlowResult: ProductIdAware {
    abstract override val productId: ProductId
}

data class BillingFlowSuccess(override val productId: ProductId): BillingFlowResult()

data class BillingFlowFailure(
    override val productId: ProductId,
    val cause: Cause,
    val exception: Exception?
): BillingFlowResult() {

    enum class Cause {
        UNKNOWN,
        UNAVAILABLE,
        USER_CANCELED
    }

}