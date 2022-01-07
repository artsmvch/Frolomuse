package com.frolo.muse.ui.main.library.base

import android.content.Context
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.model.Recently
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.model.menu.SortOrderMenu
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.frolo.music.model.SortOrder


private fun MenuItem.setEnabledCompat(enabled: Boolean, context: Context) {
    isEnabled = enabled
    title = SpannableString(title).apply {
        val color = ContextCompat.getColor(context, R.color.popup_header)
        setSpan(ForegroundColorSpan(color), 0, length, 0)
    }
}

fun View.showSortOrderPopup(
    sortOrderMenu: SortOrderMenu,
    sortOrderConsumer: (sortOrder: SortOrder) -> Unit,
    reversedConsumer: (reversed: Boolean) -> Unit
): PopupMenu {

    val popupMenu = PopupMenu(context, this, Gravity.END)

    popupMenu.menuInflater.inflate(R.menu.popup_sort_order, popupMenu.menu)
    popupMenu.menu.also { menu ->
        menu.findItem(R.id.title)?.apply {
            setEnabledCompat(false, context)
            setOnMenuItemClickListener { true }
        }

        sortOrderMenu.sortOrders.forEachIndexed { index, sortOrder ->
            val menuItem = menu.add(R.id.orders, Menu.NONE, index, sortOrder.localizedName)

            menuItem.isCheckable = true
            if (SortOrder.areKeysTheSame(sortOrderMenu.selectedSortOrder, sortOrder)) {
                menuItem.isChecked = true
            }

            menuItem.setOnMenuItemClickListener {
                if (it.isChecked.not()) {
                    sortOrderConsumer(sortOrder)
                }
                false
            }
        }
        menu.setGroupCheckable(R.id.orders, true, true)

        menu.findItem(R.id.ascending)?.apply {
            isChecked = !sortOrderMenu.isSortOrderReversed
            setOnMenuItemClickListener {
                reversedConsumer(it.isChecked)
                false
            }
        }
    }

    popupMenu.show()

    return popupMenu
}


fun View.showRecentPeriodPopup(
    recentPeriodMenu: RecentPeriodMenu,
    recentPeriodConsumer: (period: Int) -> Unit
): PopupMenu {

    val popupMenu = PopupMenu(context, this, Gravity.END)

    popupMenu.menuInflater.inflate(R.menu.popup_recent_period, popupMenu.menu)
    popupMenu.menu.also { menu ->
        menu.findItem(R.id.title)?.apply {
            setEnabledCompat(false, context)
            setOnMenuItemClickListener { true }
        }

        val menuItemId = when (recentPeriodMenu.selectedPeriod) {
            Recently.FOR_LAST_HOUR -> R.id.action_for_last_hour
            Recently.FOR_LAST_DAY -> R.id.action_for_last_day
            Recently.FOR_LAST_WEEK -> R.id.action_for_last_week
            Recently.FOR_LAST_MONTH -> R.id.action_for_last_month
            Recently.FOR_LAST_YEAR -> R.id.action_for_last_year
            else -> 0
        }
        menu.findItem(menuItemId)?.isChecked = true

        for (i in 0 until menu.size()) {
            val candidate = menu.getItem(i)
            if (candidate.groupId == R.id.group_periods) {
                candidate.setOnMenuItemClickListener { menuItem ->

                    val period = when (menuItem.itemId) {
                        R.id.action_for_last_hour -> Recently.FOR_LAST_HOUR
                        R.id.action_for_last_day -> Recently.FOR_LAST_DAY
                        R.id.action_for_last_week -> Recently.FOR_LAST_WEEK
                        R.id.action_for_last_month -> Recently.FOR_LAST_MONTH
                        R.id.action_for_last_year -> Recently.FOR_LAST_YEAR
                        else -> {
                            if (BuildConfig.DEBUG) {
                                throw IllegalArgumentException("Unexpected menu item id")
                            }
                            Recently.FOR_LAST_YEAR
                        }
                    }

                    recentPeriodConsumer(period)

                    true
                }
            }
        }

        menu.setGroupCheckable(R.id.group_periods, true, true)
    }

    popupMenu.show()

    return popupMenu
}