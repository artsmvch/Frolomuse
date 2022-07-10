package com.frolo.core.ui.touch


interface TouchFlowAware {
    var touchFlowCallback: TouchFlowCallback?

    interface TouchFlowCallback {
        /**
         * Called when the sequence of motion events started
         */
        fun onTouchFlowStarted()

        /**
         * Called when the sequence of motion events ended
         */
        fun onTouchFlowEnded()
    }
}