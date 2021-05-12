package com.frolo.muse.ui.main.settings.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.*
import com.frolo.muse.model.Theme
import com.frolo.muse.model.ThemeUtils
import kotlinx.android.synthetic.main.item_simple_theme_page.view.*
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
        init {
            val onApplyThemeClickListener = View.OnClickListener {
                pages.getOrNull(bindingAdapterPosition)?.also { page ->
                    callback.onApplyThemeClick(page)
                }
            }
            itemView.setOnClickListener(onApplyThemeClickListener)
            itemView.fab_button.setOnClickListener(onApplyThemeClickListener)
            itemView.imv_preview_pro_badge.setOnClickListener {
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
            val windowBackground = StyleUtil.resolveDrawable(themedContext, android.R.attr.windowBackground)
            val primaryColor = StyleUtil.resolveColor(themedContext, R.attr.colorPrimary)
            val secondaryColor = StyleUtil.resolveColor(themedContext, R.attr.colorSecondary)
            val textColor = StyleUtil.resolveColorStateList(themedContext, android.R.attr.textColorPrimary)
            val themeName = ThemeUtils.getNameResourceId(page.theme)?.let { themedContext.getString(it) } ?: ""
            val isCurrentThemeDark = currentTheme.isDark
            with(itemView) {
                requestManager.load(windowBackground)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imv_window_background)
                imv_primary_color.setImageDrawable(ColorDrawable(primaryColor))
                fab_button.supportBackgroundTintList = ColorStateList.valueOf(secondaryColor)
                tv_theme_name.setTextColor(textColor)
                tv_theme_name.text = themeName

                imv_preview_pro_badge.isVisible = page.hasProBadge

                // Card view
                if (isCurrentThemeDark) {
                    card_view.strokeWidth = Screen.dp(context, 1f).coerceAtLeast(1)
                    card_view.strokeColor = if (page.theme.isDark) {
                        context.getColor(R.color.md_grey_500)
                    } else {
                        context.getColor(R.color.md_grey_50)
                    }
                    card_view.cardElevation = 0f
                    card_view.maxCardElevation = 0f
                } else {
                    card_view.strokeWidth = 0
                    card_view.strokeColor = Color.TRANSPARENT
                    card_view.cardElevation = Screen.dpFloat(3f)
                    card_view.maxCardElevation = Screen.dpFloat(4f)
                }

                this.isClickable = !page.isApplied
                doTraversal { view ->
                    view.alpha = if (page.isApplied) 0.6f else 1f
                }
            }
        }
    }

}