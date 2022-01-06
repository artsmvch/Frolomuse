package com.frolo.rx

import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class KeyedDisposableContainerTest {

    private fun createDisposableContainer(): KeyedDisposableContainer<String> {
        return StringKeyedDisposableContainer()
    }

    private fun createDisposable(): Disposable {
        return Disposables.empty()
    }

    @Test
    fun test_AddByKey() {
        val container = createDisposableContainer()

        val disposable1 = createDisposable()
        container.add("key1", disposable1)

        assertTrue(container.containsKey("key1"))
        assertTrue(container.containsValue(disposable1))

        val disposable2 = createDisposable()
        container.add("key2", disposable2)

        assertTrue(container.containsKey("key2"))
        assertTrue(container.containsValue(disposable2))

        // Dispose the whole container
        container.dispose()
        assertTrue(container.isDisposed)
        assertFalse(container.containsKey("key1"))
        assertFalse(container.containsKey("key2"))
        assertFalse(container.containsValue(disposable1))
        assertFalse(container.containsValue(disposable2))
    }

    @Test
    fun test_RemoveByKey() {
        val container = createDisposableContainer()

        // Remove
        val disposable1 = createDisposable()
        container.add("key1", disposable1)
        val removedDisposable1 = container.remove("key1")!!

        assertFalse(container.containsKey("key1"))
        assertEquals(disposable1, removedDisposable1)
        assertTrue(removedDisposable1.isDisposed)

        // Dispose the whole container
        container.dispose()
        assertTrue(container.isDisposed)
        assertFalse(container.containsKey("key1"))
        assertFalse(container.containsValue(disposable1))
    }

    @Test
    fun test_DeleteByKey() {
        val container = createDisposableContainer()

        // Remove
        val disposable1 = createDisposable()
        container.add("key1", disposable1)
        val removedDisposable1 = container.delete("key1")!!

        assertFalse(container.containsKey("key1"))
        assertEquals(disposable1, removedDisposable1)
        assertFalse(removedDisposable1.isDisposed)

        // Dispose the whole container
        container.dispose()
        assertTrue(container.isDisposed)
        assertFalse(container.containsKey("key1"))
        assertFalse(container.containsValue(disposable1))
    }

    @Test
    fun test_ReplaceByKey() {
        val container = createDisposableContainer()

        // Remove
        val disposable1 = createDisposable()
        container.add("key1", disposable1)

        val disposable2 = createDisposable()
        val replacedDisposable1 = container.add("key1", disposable2)

        assertTrue(container.containsKey("key1"))
        assertEquals(disposable1, replacedDisposable1)

        // Dispose the whole container
        container.dispose()
        assertTrue(container.isDisposed)
        assertFalse(container.containsKey("key1"))
        assertFalse(container.containsValue(disposable1))
    }

    @Test
    fun test_Add() {
        val container = createDisposableContainer()

        // Remove
        val disposable1 = createDisposable()
        container.add(disposable1)
        assertTrue(container.containsKey(null))
        assertTrue(container.containsValue(disposable1))

        val result1 = container.remove(disposable1)
        assertTrue(result1)
        assertTrue(disposable1.isDisposed)

        // Delete
        val disposable2 = createDisposable()
        container.add(disposable2)
        assertTrue(container.containsKey(null))
        assertFalse(container.containsValue(disposable1))
        assertTrue(container.containsValue(disposable2))

        val result2 = container.delete(disposable2)
        assertTrue(result2)
        assertFalse(disposable2.isDisposed)

        // Replace
        val disposable3 = createDisposable()
        val disposable4 = createDisposable()
        container.add(disposable3)
        container.add(disposable4)
        assertTrue(disposable3.isDisposed)
        assertFalse(disposable4.isDisposed)

        // Dispose the whole container
        container.dispose()
        assertTrue(container.isDisposed)
        assertFalse(container.containsKey(null))

        assertFalse(container.containsValue(disposable1))
        assertFalse(container.containsValue(disposable2))
        assertFalse(container.containsValue(disposable3))
        assertFalse(container.containsValue(disposable4))

        assertTrue(disposable4.isDisposed)
    }

}