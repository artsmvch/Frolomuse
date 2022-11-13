package com.frolo.billing.playstore

import com.android.billingclient.api.Purchase
import org.json.JSONObject


internal data class PurchaseDetails(
    val skus: List<String>,
    @Purchase.PurchaseState val state: Int,
    val isAcknowledged: Boolean
) {

    companion object {
        private const val SKU_SEPARATOR = ","

        fun from(purchase: Purchase): PurchaseDetails {
            return PurchaseDetails(
                skus = purchase.skus,
                state = purchase.purchaseState,
                isAcknowledged = purchase.isAcknowledged
            )
        }

        fun serializeToJson(details: PurchaseDetails): String {
            val json = JSONObject()
            json.put("skus", serializeSkuList(details.skus))
            json.put("state", details.state)
            json.put("is_acknowledged", details.isAcknowledged)
            return json.toString()
        }

        fun deserializeFromJson(jsonString: String): PurchaseDetails {
            val json = JSONObject(jsonString)
            val skus = when {
                json.has("skus") -> {
                    deserializeSkuList(json.optString("skus"))
                }
                json.has("sku") -> {
                    listOf(json.optString("sku"))
                }
                else -> emptyList<String>()
            }
            return PurchaseDetails(
                skus = skus,
                state = json.getInt("state"),
                isAcknowledged = json.getBoolean("is_acknowledged")
            )
        }

        private fun serializeSkuList(skus: List<String>): String {
            return skus.joinToString(separator = SKU_SEPARATOR)
        }

        private fun deserializeSkuList(skusString: String): List<String> {
            return skusString.split(SKU_SEPARATOR)
        }
    }

}