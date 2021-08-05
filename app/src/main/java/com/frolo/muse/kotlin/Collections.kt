package com.frolo.muse.kotlin


inline fun <E> Iterable<E>.contains(predicate: (E) -> Boolean): Boolean {
    return indexOfFirst(predicate) >= 0
}

inline fun <reified R> Iterable<*>.containsInstanceOf(): Boolean {
    return contains { it is R }
}

/**
 * Moves an item from [fromPosition] to [toPosition].
 * Not to be confused with [java.util.Collections.swap].
 */
fun <E> MutableList<E>.moveItem(fromPosition: Int, toPosition: Int) {
    add(toPosition, removeAt(fromPosition))
}