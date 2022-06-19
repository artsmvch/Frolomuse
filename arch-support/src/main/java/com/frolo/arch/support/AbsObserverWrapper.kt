package com.frolo.arch.support

import androidx.lifecycle.Observer


internal abstract class AbsObserverWrapper<T>(
    protected val wrapped: Observer<T>
) : Observer<T> {

    override fun hashCode(): Int {
        return unwrap().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is AbsObserverWrapper<*>) {
            return unwrap() == other.unwrap()
        }
        return false
    }

    /**
     * Peels off all wrappers in order to find the original observer.
     */
    private fun unwrap(): Observer<*> {
        var wrapped: Observer<*> = this.wrapped
        while (wrapped is AbsObserverWrapper<*>) {
            wrapped = wrapped.wrapped
        }
        return wrapped
    }
}