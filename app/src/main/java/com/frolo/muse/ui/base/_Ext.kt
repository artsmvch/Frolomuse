package com.frolo.muse.ui.base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.io.Serializable


//<editor-fold desc="BaseFragment arguments">
fun BaseFragment.withArg(key: String, arg: Serializable): Fragment {
    return apply {
        arguments = Bundle(1).apply {
            putSerializable(key, arg)
        }
    }
}

fun <T: Serializable> BaseFragment.serializableArg(key: String): Lazy<T> = lazy {
    @Suppress("UNCHECKED_CAST")
    requireArguments().getSerializable(key) as T
}

fun BaseFragment.withLongArg(key: String, arg: Long): Fragment {
    return apply {
        arguments = Bundle(1).apply {
            putLong(key, arg)
        }
    }
}
//</editor-fold>


//<editor-fold desc="BaseDialogFragment arguments">
fun BaseDialogFragment.withArg(key: String, arg: Serializable): DialogFragment {
    return apply {
        arguments = Bundle(1).apply {
            putSerializable(key, arg)
        }
    }
}

fun <T: Serializable> BaseDialogFragment.serializableArg(key: String): Lazy<T> = lazy {
    @Suppress("UNCHECKED_CAST")
    requireArguments().getSerializable(key) as T
}
//</editor-fold>


//<editor-fold desc="BaseDialogFragment nullable arguments">
fun BaseDialogFragment.withNullableArg(key: String, arg: Serializable?): DialogFragment {
    return apply {
        arguments = Bundle(1).apply {
            putSerializable(key, arg)
        }
    }
}

fun <T: Serializable> BaseDialogFragment.serializableNullableArg(key: String): Lazy<T?> = lazy {
    @Suppress("UNCHECKED_CAST")
    requireArguments().getSerializable(key) as? T
}
//</editor-fold>

inline fun <reified T> BaseFragment.castHost(): T? {
    return (parentFragment as? T) ?: (context as? T) ?: (host as? T)
}