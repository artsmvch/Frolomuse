package com.frolo.muse.kotlin


inline fun <E> Iterable<E>.contains(predicate: (E) -> Boolean): Boolean {
    return indexOfFirst(predicate) >= 0
}

inline fun <reified R> Iterable<*>.containsInstanceOf(): Boolean {
    return contains { it is R }
}