package com.frolo.muse

import com.frolo.muse.model.media.Song
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible


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
            constants[randomInt(constants.size)] as T
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

private fun mockPrimitiveOrNull(clazz: KClass<*>): Any? = when(clazz) {
    Int::class -> randomInt()
    Long::class -> randomLong()
    Double::class -> randomDouble()
    Float::class -> randomFloat()
    Char::class -> randomChar()

    String::class -> randomString()

    //else -> throw IllegalArgumentException("The given clazz is not a primitive")
    else -> null
}

// Tries mocking an instance based on known interfaces/abstract classes
private fun <T: Any> tryMockAbstract(clazz: KClass<T>): T {
    if (clazz == Song::class) {
        @Suppress("UNCHECKED_CAST")
        return mockSong() as T
    }

    error("Failed to instantiate interface/abstract class")
}