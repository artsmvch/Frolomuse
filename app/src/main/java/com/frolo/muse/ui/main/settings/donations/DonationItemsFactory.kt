package com.frolo.muse.ui.main.settings.donations

import androidx.annotation.AnyThread
import com.frolo.billing.ProductDetails
import com.frolo.muse.DebugUtils
import com.frolo.muse.R
import com.frolo.muse.billing.Products


@AnyThread
internal object DonationItemsFactory {

    fun createDonationItems(productDetailsList: List<ProductDetails>): List<DonationItem> {
        val productDonationItems = productDetailsList
            //.sortedBy { details -> details.price }
            .mapNotNull(::toDonationItem)
        return listOf<DonationItem>(getRatingDonationItem()) + productDonationItems
    }

    private fun toDonationItem(productDetails: ProductDetails) : DonationItem? {
        return when (productDetails.productId) {
            Products.DONATE_THANKS -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_thanks,
                    iconResId = R.drawable.ic_donate_thanks,
                    tintResId = R.color.md_amber_600,
                    iconSide = DonationItem.IconSide.BOTTOM_RIGHT,
                    productDetails = productDetails
                )
            }
            Products.DONATE_COFFEE -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_coffee,
                    iconResId = R.drawable.ic_donate_coffee,
                    iconSide = DonationItem.IconSide.BOTTOM_LEFT,
                    tintResId = R.color.md_brown_300,
                    productDetails = productDetails
                )
            }
            Products.DONATE_MOVIE_TICKET -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_movie_ticket,
                    iconResId = R.drawable.ic_donate_movie,
                    iconSide = DonationItem.IconSide.BOTTOM_RIGHT,
                    tintResId = R.color.md_blue_A400,
                    productDetails = productDetails
                )
            }
            Products.DONATE_PIZZA -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_pizza,
                    iconResId = R.drawable.ic_donate_pizza,
                    iconSide = DonationItem.IconSide.BOTTOM_LEFT,
                    tintResId = R.color.md_orange_600,
                    productDetails = productDetails
                )
            }
            Products.DONATE_MEAL -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_meal,
                    iconResId = R.drawable.ic_donate_meal,
                    iconSide = DonationItem.IconSide.BOTTOM_RIGHT,
                    tintResId = R.color.md_green_500,
                    productDetails = productDetails
                )
            }
            Products.DONATE_GYM_MEMBERSHIP -> {
                DonationItem.Purchase(
                    nameResId = R.string.donate_gym,
                    iconResId = R.drawable.ic_donate_gym,
                    iconSide = DonationItem.IconSide.BOTTOM_RIGHT,
                    tintResId = R.color.md_deep_purple_300,
                    productDetails = productDetails
                )
            }
            else -> {
                val errorMessage = "Unexpected donation id: ${productDetails.productId}"
                DebugUtils.dumpOnMainThread(IllegalArgumentException(errorMessage))
                null
            }
        }
    }

    private fun getRatingDonationItem(): DonationItem.Rating {
        return DonationItem.Rating(
            nameResId = R.string.rate_on_play_store,
            iconResId = R.drawable.ic_donate_star,
            iconSide = DonationItem.IconSide.BOTTOM_LEFT,
            tintResId = R.color.md_pink_400
        )
    }
}