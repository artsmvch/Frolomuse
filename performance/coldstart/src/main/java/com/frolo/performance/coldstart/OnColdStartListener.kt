package com.frolo.performance.coldstart

fun interface OnColdStartListener {
    fun onColdStart(info: ColdStartInfo)
}