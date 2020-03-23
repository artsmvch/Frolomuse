package com.frolo.muse


import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.internal.view.SupportMenuItem
import androidx.core.view.forEach


fun MenuItem.disableIconTint() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        iconTintList = null
    } else if (this is SupportMenuItem) {
        this.iconTintList = null
    }
}

fun MenuItem.setIconTint(@ColorInt color: Int) {
    icon?.also { safeIcon ->
        val updatedIcon = safeIcon.mutate()
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        updatedIcon.colorFilter = colorFilter
        icon = updatedIcon
    }
}

fun Menu.setIconTint(@ColorInt color: Int) {
    forEach { menuItem ->
        menuItem.setIconTint(color)
        if (menuItem.hasSubMenu()) {
            menuItem.subMenu.setIconTint(color)
        }
    }
}