package com.frolo.muse

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.lang.reflect.Modifier
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter


// Observes the live data forever with a stub observer
fun <T> LiveData<T>.observeForever() {
    observeForever { /*stub*/ }
}

// Observes all public live data instances within this view model
// There is a bug with MediatorLiveData.
// See https://stackoverflow.com/questions/53910409/mediatorlivedata-doesnt-work-in-junit-tests
// So this workaround is necessary if you want to test mediator live data.
fun ViewModel.observeEntirely() {
    val clazz = this::class
    for (property in clazz.memberProperties) {
        // Check if it's a live data
        val propertyClazz = property.javaField?.type
        if (propertyClazz != null &&
                propertyClazz.isAssignableFrom(LiveData::class.java)) {
            if (property.visibility == KVisibility.PUBLIC) {
                val liveData = property.getter.call(this) as LiveData<*>
                liveData.observeForever()
            }
        } else {
            val getter = property.javaGetter
            if (getter != null) {
                val modifiers = getter.getModifiers()
                if (Modifier.isPublic(modifiers)) {
                    getter.isAccessible = true
                    val liveData = getter.invoke(this) as LiveData<*>
                    liveData.observeForever()
                }
            }
        }
    }
}

