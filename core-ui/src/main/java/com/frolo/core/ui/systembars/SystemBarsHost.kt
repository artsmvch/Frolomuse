package com.frolo.core.ui.systembars


interface SystemBarsHost {
    /**
     * If [owner] is currently in control of the system bars,
     * then a non-null controller will be returned.
     *
     * The controller is enabled as long as [owner] owns the control.
     */
    fun getSystemBarsController(owner: SystemBarsControlOwner): SystemBarsController?

    /**
     * Obtains control of the system bars.
     */
    fun obtainSystemBarsControl(owner: SystemBarsControlOwner)

    /**
     * Abandons control of the system bars.
     */
    fun abandonSystemBarsControl(owner: SystemBarsControlOwner)
}