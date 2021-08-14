package com.frolo.muse

import kotlin.random.Random


private val sharedRandom = Random

private val chars by lazy {
    ArrayList<Char>().also { list ->
        var c = 'A'
        while (c <= 'Z') {
            list.add(c)
            ++c
        }
    }
}

fun randomInt(until: Int? = null): Int =
    if (until != null) sharedRandom.nextInt(until)
    else sharedRandom.nextInt()

fun randomLong(until: Long? = null): Long =
    if (until != null) sharedRandom.nextLong(until)
    else sharedRandom.nextLong()

fun randomDouble(until: Double? = null): Double =
    if (until != null) sharedRandom.nextDouble(until)
    else sharedRandom.nextDouble()

fun randomFloat(): Float = sharedRandom.nextFloat()

fun randomChar(): Char = chars[sharedRandom.nextInt(chars.size)]

fun randomBoolean(): Boolean = sharedRandom.nextBoolean()

fun randomString(length: Int = sharedRandom.nextInt(10)): String {
    return (1..length).map { randomChar() }.joinToString(separator = "") { "$it" }
}

