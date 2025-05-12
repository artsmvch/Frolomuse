package com.frolo.collections


// A workaround for a Kotlin issue
// https://youtrack.jetbrains.com/issue/KT-71375/Prevent-Kotlins-removeFirst-and-removeLast-from-causing-crashes-on-Android-14-and-below-after-upgrading-to-Android-API-Level-35
fun <T> List<T>.reversedCompat(): List<T> {
    val result = ArrayList<T>(size)
    for (i in size - 1 downTo 0) {
        result.add(get(i))
    }
    return result
}