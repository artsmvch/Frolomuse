package com.frolo.test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
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
    // Primitives
    val primitive = tryMockPrimitiveOrNull(clazz)
    if (primitive != null) {
        @Suppress("UNCHECKED_CAST")
        return primitive as T
    }

    // Strings
    val string = tryMockStringOrNull(clazz)
    if (string != null) {
        @Suppress("UNCHECKED_CAST")
        return string as T
    }

    // Enums
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

    // Abstracts
    if (clazz.isAbstract) {
        return mockAbstract(clazz)
    }

    // Well, we're still here. We are now trying to create
    // an instance using one of the available constructors.
    val constructors = clazz.constructors
        .sortedBy { it.parameters.size }

    for (constructor in constructors) {
        try {

            val hasParameterRecursion: Boolean = constructor.parameters
                .indexOfFirst { param -> param.type.classifier == clazz } != -1

            if (hasParameterRecursion) {
                // We don't want to use this constructor,
                // otherwise we will end up in recursion.
                continue
            }

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

private fun tryMockPrimitiveOrNull(clazz: KClass<*>): Any? = when(clazz) {
    Int::class ->       randomInt()
    Long::class ->      randomLong()
    Double::class ->    randomDouble()
    Float::class ->     randomFloat()
    Char::class ->      randomChar()
    Boolean::class ->   randomBoolean()
    else -> null
}

private fun tryMockStringOrNull(clazz: KClass<*>): Any? {
    if (clazz == String::class) {
        return randomString()
    }

    return null
}

private fun <T: Any> mockAbstract(clazz: KClass<T>): T {
    val javaClazz = clazz.java
    val proxy = Proxy.newProxyInstance(
        javaClazz.classLoader,
        arrayOf<Class<*>>(javaClazz),
        object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                val kotlinClass: KClass<*> = method.returnType.kotlin
                return mockKT(kotlinClass)
            }
        })

    if (!clazz.isInstance(proxy)) {
        throw IllegalArgumentException("Failed to instantiate interface/abstract class: $javaClazz")
    }

    @Suppress("UNCHECKED_CAST")
    return proxy as T
}