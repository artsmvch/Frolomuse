package com.frolo.core.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.view.forEach


fun MenuItem.setIconTint(@ColorInt color: Int) {
    icon?.also { safeIcon ->
        val updatedIcon = safeIcon.mutate()
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        updatedIcon.colorFilter = colorFilter
        icon = updatedIcon
    }
}

fun Menu.doTraversal(action: (MenuItem) -> Unit) {
    forEach { menuItem ->
        action.invoke(menuItem)
        if (menuItem.hasSubMenu()) {
            menuItem.subMenu?.doTraversal(action)
        }
    }
}

fun Menu.setIconTint(@ColorInt color: Int) {
    doTraversal { menuItem -> menuItem.setIconTint(color) }
}