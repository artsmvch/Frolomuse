package com.frolo.mediascan

import android.util.SparseArray
import androidx.core.util.putAll

internal object CollectionUtils {
    fun <T> copy(array: SparseArray<T>): SparseArray<T> {
        val copy = SparseArray<T>(array.size())
        copy.putAll(array)
        return copy
    }
}