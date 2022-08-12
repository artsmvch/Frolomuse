package com.frolo.muse.ui.main.settings.theme


/**
 * Abstract theme page adapter with the ability to get/set theme pages.
 */
interface AbsThemePageAdapter {
    var pages: List<ThemePage>
    val pageCount: Int get() = pages.count()
}