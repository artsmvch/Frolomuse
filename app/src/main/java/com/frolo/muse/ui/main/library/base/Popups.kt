package com.frolo.muse.ui.main.library.base

import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.model.Recently
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.model.menu.SortOrderMenu


fun View.chooseSortOrder(
        sortOrderMenu: SortOrderMenu,
        sortOrderConsumer: (sortOrder: String) -> Unit,
        reversedConsumer: (reversed: Boolean) -> Unit): PopupMenu {

    val popupMenu = PopupMenu(
            context, this, Gravity.NO_GRAVITY)

    popupMenu.menuInflater.inflate(R.menu.popup_sort_order, popupMenu.menu)
    popupMenu.menu.also { menu ->
        menu.findItem(R.id.title)?.setOnMenuItemClickListener { true }

        sortOrderMenu.sortOrders.entries.forEachIndexed { index, entry ->
            val menuItem = menu.add(R.id.orders, Menu.NONE, index, entry.value)

            menuItem.isCheckable = true
            if (sortOrderMenu.selectedSortOrder == entry.key) {
                menuItem.isChecked = true
            }

            menuItem.setOnMenuItemClickListener {
                if (it.isChecked.not()) {
                    sortOrderConsumer(entry.key)
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


fun View.chooseRecentPeriod(
        recentPeriodMenu: RecentPeriodMenu,
        recentPeriodConsumer: (period: Int) -> Unit): PopupMenu {

    val popupMenu = PopupMenu(
            context, this, Gravity.NO_GRAVITY)

    popupMenu.menuInflater.inflate(R.menu.popup_recent_period, popupMenu.menu)
    popupMenu.menu.also { menu ->
        menu.findItem(R.id.title)?.setOnMenuItemClickListener { true }

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
                                throw IllegalArgumentException("Unknown menu item id")
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