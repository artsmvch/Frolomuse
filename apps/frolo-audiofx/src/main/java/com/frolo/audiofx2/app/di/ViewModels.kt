package com.frolo.audiofx2.app.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified VM: ViewModel> injectViewModel(): ReadOnlyProperty<Fragment, VM> =
    object : ReadOnlyProperty<Fragment, VM> {
        override fun getValue(thisRef: Fragment, property: KProperty<*>): VM {
            return ViewModelProviders.of(thisRef).get(VM::class.java)
        }
    }
