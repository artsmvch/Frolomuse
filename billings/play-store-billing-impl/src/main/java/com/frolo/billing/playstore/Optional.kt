package com.frolo.billing.playstore


internal class Optional<V> private constructor(val value: V?) {

    companion object {
        fun <V> of(value: V?): Optional<V> = Optional(value)
    }

}