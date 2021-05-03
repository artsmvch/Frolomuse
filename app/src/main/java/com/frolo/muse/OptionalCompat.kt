package com.frolo.muse


data class OptionalCompat<T>(val value: T?) {
    
    inline fun unwrap(action: (T) -> Unit) {
        value?.also(action)
    }
    
    fun or(defaultValue: T): T = value ?: defaultValue

    companion object {
        fun <T> of(value: T?): OptionalCompat<T> = OptionalCompat(value)

        fun <T> empty(): OptionalCompat<T> = OptionalCompat(null)
    }
    
}