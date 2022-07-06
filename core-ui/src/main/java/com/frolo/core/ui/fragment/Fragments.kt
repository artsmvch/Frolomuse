package com.frolo.core.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer


inline fun Fragment.doOnViewCreated(crossinline action: (View) -> Unit) {
    val viewLifecycleOwnerObserver = object : Observer<LifecycleOwner> {
        override fun onChanged(value: LifecycleOwner?) {
            if (value != null) {
                viewLifecycleOwnerLiveData.removeObserver(this)
                action.invoke(requireView())
            }
        }
    }
    viewLifecycleOwnerLiveData.observe(this, viewLifecycleOwnerObserver)
}

inline fun Fragment.doOnCreate(crossinline action: () -> Unit) {
    doOnLifecycleEvent(targetEvent = Lifecycle.Event.ON_CREATE, action = action)
}

inline fun Fragment.doOnStart(crossinline action: () -> Unit) {
    doOnLifecycleEvent(targetEvent = Lifecycle.Event.ON_START, action = action)
}

inline fun Fragment.doOnResume(crossinline action: () -> Unit) {
    doOnLifecycleEvent(targetEvent = Lifecycle.Event.ON_RESUME, action = action)
}

@PublishedApi
internal inline fun Fragment.doOnLifecycleEvent(
    targetEvent: Lifecycle.Event,
    crossinline action: () -> Unit
) {
    if (lifecycle.currentState.isAtLeast(targetEvent.targetState)) {
        action.invoke()
        return
    }
    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
        return
    }
    lifecycle.addObserver(
        object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == targetEvent) {
                    removeItself()
                    action.invoke()
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    removeItself()
                }
            }

            fun removeItself() {
                lifecycle.removeObserver(this)
            }
        }
    )
}