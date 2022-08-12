package com.frolo.muse.ui.main.settings.donations

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.frolo.billing.ProductDetails


internal sealed class DonationItem {

    @get:StringRes
    abstract val nameResId: Int
    @get:DrawableRes
    abstract val iconResId: Int
    abstract val iconSide: IconSide
    @get:ColorRes
    abstract val tintResId: Int

    data class Rating(
        @StringRes
        override val nameResId: Int,
        @DrawableRes
        override val iconResId: Int,
        override val iconSide: IconSide,
        @ColorRes
        override val tintResId: Int
    ) : DonationItem()

    data class Purchase(
        @StringRes
        override val nameResId: Int,
        @DrawableRes
        override val iconResId: Int,
        override val iconSide: IconSide,
        @ColorRes
        override val tintResId: Int,
        val productDetails: ProductDetails
    ) : DonationItem()

    enum class IconSide {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

}