package com.frolo.billing.playstore

import com.android.billingclient.api.Purchase
import org.json.JSONObject


internal data class PurchaseDetails(
    val sku: String,
    @Purchase.PurchaseState val state: Int,
    val isAcknowledged: Boolean
) {

    companion object {

        fun from(purchase: Purchase): PurchaseDetails {
            return PurchaseDetails(
                sku = purchase.sku,
                state = purchase.purchaseState,
                isAcknowledged = purchase.isAcknowledged
            )
        }

        fun serializeToJson(details: PurchaseDetails): String {
            val json = JSONObject()
            json.put("sku", details.sku)
            json.put("state", details.state)
            json.put("is_acknowledged", details.isAcknowledged)
            return json.toString()
        }

        fun deserializeFromJson(jsonString: String): PurchaseDetails {
            val json = JSONObject(jsonString)
            return PurchaseDetails(
                sku = json.getString("sku"),
                state = json.getInt("state"),
                isAcknowledged = json.getBoolean("is_acknowledged")
            )
        }
    }

}