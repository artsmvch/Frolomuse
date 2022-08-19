package com.frolo.core.ui.startup

interface AppStartUpInfoProvider {
    val coldStartCount: Long
    val warmStartCount: Long
    val hotStartCount: Long
}