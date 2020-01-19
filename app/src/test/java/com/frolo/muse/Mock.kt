package com.frolo.muse

import com.frolo.muse.model.media.Song
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

private val sharedRandom = Random

private val mockChars by lazy {
    ArrayList<Char>().also { list ->
        var c = 'A'
        while (c <= 'Z') {
            list.add(c)
            ++c
        }
    }
}

private fun randomChar(): Char {
    return mockChars[sharedRandom.nextInt(mockChars.size)]
}

// Mocks a list of specified type with the given size
fun <T: Any> mockListOf(clazz: KClass<T>, size: Int = 1): List<T> {
    return ArrayList<T>().apply {
        repeat(size) {
            val item: T = mockKT(clazz)
            add(item)
        }
    }
}

// Mocks a list of specified type with the given size
inline fun <reified T: Any> mockList(size: Int = 1): List<T> = mockListOf(T::class, size)

fun <T: Any> mockKT(clazz: KClass<T>): T {
    val primitive = mockPrimitiveOrNull(clazz)
    if (primitive != null) {
        @Suppress("UNCHECKED_CAST")
        return primitive as T
    }

    if (clazz.isSubclassOf(Enum::class)) {
        return clazz.java.enumConstants.let { constants ->
            if (constants == null) {
                throw IllegalStateException("Enum constants == null")
            }

            if (constants.isEmpty()) {
                throw IllegalStateException("Enum constants is empty")
            }

            @Suppress("UNCHECKED_CAST")
            constants[sharedRandom.nextInt(constants.size)] as T
        }
    }

    if (clazz.isAbstract) {
        return tryMockAbstract(clazz)
    }

    val constructors = clazz.constructors
            .sortedBy { it.parameters.size }

    for (constructor in constructors) {
        try {
            val arguments = constructor.parameters
                    .map { it.type.classifier as KClass<*> }
                    .map { mockKT(it) }
                    .toTypedArray()

            constructor.isAccessible = true
            return constructor.call(*arguments)
        } catch (e: Throwable) {
            println(e)
        }
    }

    throw IllegalStateException("Failed to instantiate ${clazz.simpleName} class")
}

inline fun <reified T: Any> mockKT(): T = mockKT(T::class)

private fun mockPrimitiveOrNull(clazz: KClass<*>) = when(clazz) {
    Int::class -> sharedRandom.nextInt()
    Long::class -> sharedRandom.nextLong()
    Double::class -> sharedRandom.nextDouble()
    Float::class -> sharedRandom.nextFloat()
    Char::class -> randomChar()

    String::class -> (1..sharedRandom.nextInt(10))
            .map { randomChar() }
            .joinToString(separator = "") { "$it" }

    //else -> throw IllegalArgumentException("The given clazz is not a primitive")
    else -> null
}

// Tries mocking an instance based on known interfaces/abstract classes
private fun <T: Any> tryMockAbstract(clazz: KClass<T>): T {
    if (clazz == Song::class) {
        @Suppress("UNCHECKED_CAST")
        return SongImpl(
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT(),
                mockKT()
        ) as T
    }

    error("Failed to instantiate interface/abstract class")
}