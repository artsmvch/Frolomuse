package com.frolo.audiofx2

interface AudioEffect2 {
    val descriptor: AudioEffectDescriptor
    var isEnabled: Boolean
    fun addOnEnableStatusChangeListener(listener: OnEnableStatusChangeListener)
    fun removeOnEnableStatusChangeListener(listener: OnEnableStatusChangeListener)

    fun interface OnEnableStatusChangeListener {
        fun onEnableStatusChange(effect: AudioEffect2, isEnabled: Boolean)
    }
}