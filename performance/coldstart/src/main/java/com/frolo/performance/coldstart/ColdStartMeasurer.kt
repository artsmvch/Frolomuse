package com.frolo.performance.coldstart

object ColdStartMeasurer {
    private var coldStartInfo: ColdStartInfo? = null
    private val listeners = ArrayList<OnColdStartListener>()

    internal fun report(info: ColdStartInfo) {
        this.coldStartInfo = info
        listeners.forEach { it.onColdStart(info) }
        listeners.clear()
    }

    fun addListener(listener: OnColdStartListener) {
        val info = this.coldStartInfo
        if (info != null) {
            listener.onColdStart(info)
        } else {
            // Wait for a report
            listeners.add(listener)
        }
    }
}