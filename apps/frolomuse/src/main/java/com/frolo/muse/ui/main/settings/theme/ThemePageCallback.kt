package com.frolo.muse.ui.main.settings.theme


interface ThemePageCallback {
    /**
     * Called when the user clicks of the pro badge.
     */
    fun onProBadgeClick(page: ThemePage)

    /**
     * Called when the user clicks on the theme to apply it.
     */
    fun onApplyThemeClick(page: ThemePage)
}