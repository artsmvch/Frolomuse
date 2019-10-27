package com.frolo.muse.views.spring

import android.util.Property
import androidx.dynamicanimation.animation.FloatPropertyCompat
import kotlin.reflect.KMutableProperty0


class KFloatProperty constructor(
        private val property: KMutableProperty0<Float>,
        name: String
) : Property<Any, Float>(Float::class.java, name) {

    override fun get(`object`: Any) = property.get()

    override fun set(`object`: Any, value: Float) {
        property.set(value)
    }
}


class KFloatPropertyCompat constructor(
        private val property: KMutableProperty0<Float>,
        name: String
) : FloatPropertyCompat<Any>(name) {

    override fun getValue(`object`: Any) = property.get()

    override fun setValue(`object`: Any, value: Float) {
        property.set(value)
    }
}