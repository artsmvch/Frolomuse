package com.frolo.core.ui.systembars


interface SystemBarsControlOwner {
    /**
     * Called when this owner gains the control of the system bars.
     * [controller] can now be used to configure system bars and remains
     * enabled until [onSystemBarsControlAbandoned] is called.
     */
    fun onSystemBarsControlObtained(controller: SystemBarsController) = Unit

    /**
     * Called when this owner loses the control of the system bars. From this point on,
     * any previously acquired system bars controller is disabled until the control
     * is acquired again (see [onSystemBarsControlObtained]).
     */
    fun onSystemBarsControlAbandoned() = Unit
}