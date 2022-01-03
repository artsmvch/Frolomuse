package com.frolo.muse.ui.main.settings.donations

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frolo.billing.ProductDetails
import com.frolo.muse.R
import com.frolo.muse.Screen
import com.frolo.muse.inflateChild
import kotlinx.android.synthetic.main.item_donation.view.*
import kotlin.properties.Delegates


internal class DonationItemAdapter constructor(
    private val onItemClickListener: (DonationItem) -> Unit
) : RecyclerView.Adapter<DonationItemAdapter.ViewHolder>() {

    var items: List<DonationItem> by Delegates.observable(emptyList()) { _, oldList, newList ->
        val diffCallback = DonationItemDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_donation)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val cornerRadius = Screen.dpFloat(itemView.context, 8f)

        init {
            setIconOutlineProvider(itemView.imv_bottom_left_donation_icon)
            setIconOutlineProvider(itemView.imv_top_right_donation_icon)
            itemView.setOnClickListener {
                dispatchClick()
            }
            itemView.setOnLongClickListener {
                dispatchClick()
                true
            }
        }

        private fun setIconOutlineProvider(iconView: View) {
            iconView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
            iconView.clipToOutline = true
        }

        private fun dispatchClick() {
            items.getOrNull(bindingAdapterPosition)?.also(onItemClickListener)
        }

        fun bind(item: DonationItem) = with(itemView) {

            // Fancy height
            frame.updateLayoutParams<ConstraintLayout.LayoutParams> {
                val minHeightInDp = when (bindingAdapterPosition % 10) {
                    0 -> 96
                    1 -> 128
                    2 -> 108
                    3 -> 140
                    4 -> 108
                    5 -> 128
                    6 -> 120
                    7 -> 108
                    else -> 120
                }
                matchConstraintMinHeight = Screen.dp(context, minHeightInDp)
            }

            val tint = ContextCompat.getColor(context, item.tintResId)

            val iconView: ImageView? = when (item.iconSide) {
                DonationItem.IconSide.BOTTOM_LEFT -> {
                    imv_top_right_donation_icon.isVisible = false
                    imv_bottom_left_donation_icon.isVisible = true
                    imv_bottom_left_donation_icon
                }
                DonationItem.IconSide.BOTTOM_RIGHT -> {
                    imv_top_right_donation_icon.isVisible = true
                    imv_bottom_left_donation_icon.isVisible = false
                    imv_top_right_donation_icon
                }
                else -> {
                    imv_top_right_donation_icon.isVisible = false
                    imv_bottom_left_donation_icon.isVisible = false
                    null
                }
            }
            iconView?.apply {
                setImageResource(item.iconResId)
                val iconTint = ColorUtils.setAlphaComponent(tint, (255 * 0.32f).toInt())
                imageTintList = ColorStateList.valueOf(iconTint)
            }

            tv_donation_name.text = getTitle(item)

            if (item is DonationItem.Purchase) {
                tv_donation_price.isVisible = true
                tv_donation_price.text = getPriceText(item.productDetails)
            } else {
                tv_donation_price.isVisible = false
                tv_donation_price.text = null
            }

            background = getBackground(tint)
        }

        private fun getTitle(item: DonationItem): String {
            return when (item) {
                is DonationItem.Purchase -> {
                    //return item.productDetails.title.replace(productTitleAppNameRegex, "")
                    itemView.context.getString(item.nameResId)
                }
                is DonationItem.Rating -> {
                    itemView.context.getString(item.nameResId)
                }
            }
        }

        private fun getPriceText(details: ProductDetails): String {
            return details.price
        }

        private fun getBackground(@ColorInt tint: Int): Drawable {
            val strokeWidth = Screen.dp(itemView.context, 1.2f).coerceAtLeast(1)
            val strokeColor = ColorUtils.setAlphaComponent(tint, (255 * 0.6f).toInt())
            val backgroundColor = ColorUtils.setAlphaComponent(tint, (255 * 0.04f).toInt())

            val contentDrawable = GradientDrawable()
            contentDrawable.setColor(backgroundColor)
            contentDrawable.cornerRadius = cornerRadius
            contentDrawable.setStroke(strokeWidth, strokeColor)

            val rippleColorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf()),
                intArrayOf(backgroundPressedColor, backgroundColor)
            )
            return RippleDrawable(rippleColorStateList, contentDrawable, null)
        }

    }

    companion object {
        private val productTitleAppNameRegex by lazy { """(?> \(.+?\))$""".toRegex() }

        @ColorInt
        private val backgroundPressedColor: Int = Color.parseColor("#AAFFFFFF")
    }
}