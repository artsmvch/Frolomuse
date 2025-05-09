package com.frolo.muse.ui.main.settings.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.core.ui.doTraversal
import com.frolo.core.ui.inflateChild
import com.frolo.muse.*
import com.frolo.muse.model.Theme
import com.frolo.muse.model.ThemeUtils
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.properties.Delegates


class SimpleThemePageAdapter(
    private val currentTheme: Theme,
    private val requestManager: RequestManager,
    private val callback: ThemePageCallback
) : RecyclerView.Adapter<SimpleThemePageAdapter.ViewHolder>(), AbsThemePageAdapter {

    override var pages: List<ThemePage> by Delegates.observable(emptyList()) { _, oldList, newList ->
        val diffCallback = ThemePageItemDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = pages.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_simple_theme_page)
        itemView.layoutParams?.width = (Screen.getScreenWidth(parent.context) / 3.5f).toInt()
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val cardView = itemView.findViewById<MaterialCardView>(R.id.card_view)
        private val fabButton = itemView.findViewById<FloatingActionButton>(R.id.fab_button)
        private val imvPrimaryColor = itemView.findViewById<ImageView>(R.id.imv_primary_color)
        private val imvPreviewProBadge = itemView.findViewById<ImageView>(R.id.imv_preview_pro_badge)
        private val imvWindowBackground = itemView.findViewById<ImageView>(R.id.imv_window_background)
        private val tvThemeName = itemView.findViewById<TextView>(R.id.tv_theme_name)

        init {
            val onApplyThemeClickListener = View.OnClickListener {
                pages.getOrNull(bindingAdapterPosition)?.also { page ->
                    callback.onApplyThemeClick(page)
                }
            }
            itemView.setOnClickListener(onApplyThemeClickListener)
            fabButton.setOnClickListener(onApplyThemeClickListener)
            imvPreviewProBadge.setOnClickListener {
                pages.getOrNull(bindingAdapterPosition)?.also { page ->
                    callback.onProBadgeClick(page)
                }
            }
        }

        private fun dispatchApplyThemeClick() {
            pages.getOrNull(bindingAdapterPosition)?.also { page ->
                callback.onApplyThemeClick(page)
            }
        }

        fun bind(page: ThemePage) {
            val themedContext = ThemeUtils.createThemedContext(itemView.context, page.theme)
            val windowBackground = StyleUtils.resolveDrawable(themedContext, android.R.attr.windowBackground)
            val primaryColor = StyleUtils.resolveColor(themedContext, com.google.android.material.R.attr.colorPrimary)
            val secondaryColor = StyleUtils.resolveColor(themedContext, com.google.android.material.R.attr.colorSecondary)
            val textColor = StyleUtils.resolveColorStateList(themedContext, android.R.attr.textColorPrimary)
            val themeName = ThemeUtils.getNameResourceId(page.theme)?.let { themedContext.getString(it) } ?: ""
            val isCurrentThemeDark = currentTheme.isDark
            with(itemView) {
                requestManager.load(windowBackground)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imvWindowBackground)
                imvPrimaryColor.setImageDrawable(ColorDrawable(primaryColor))
                fabButton.supportBackgroundTintList = ColorStateList.valueOf(secondaryColor)
                tvThemeName.setTextColor(textColor)
                tvThemeName.text = themeName

                imvPreviewProBadge.isVisible = page.hasProBadge

                // Card view
                if (isCurrentThemeDark) {
                    cardView.strokeWidth = Screen.dp(context, 1f).coerceAtLeast(1)
                    cardView.strokeColor = if (page.theme.isDark) {
                        context.getColor(com.google.android.material.support.R.color.md_grey_500)
                    } else {
                        context.getColor(com.google.android.material.support.R.color.md_grey_50)
                    }
                    cardView.cardElevation = 0f
                    cardView.maxCardElevation = 0f
                } else {
                    cardView.strokeWidth = 0
                    cardView.strokeColor = Color.TRANSPARENT
                    cardView.cardElevation = Screen.dpFloat(3f)
                    cardView.maxCardElevation = Screen.dpFloat(4f)
                }

                this.isClickable = !page.isApplied
                doTraversal { view ->
                    view.alpha = if (page.isApplied) 0.6f else 1f
                }
            }
        }
    }

}