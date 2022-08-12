package com.frolo.muse.arch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.combineMultiple
import com.frolo.muse.observeForever
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LiveDataExtTest {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Test
    fun test_combineMultiple() {
        val source1 = MutableLiveData<Boolean>()
        val source2 = MutableLiveData<Boolean>()
        val source3 = MutableLiveData<Boolean>()
        val source4 = MutableLiveData<Boolean>()
        val source5 = MutableLiveData<Boolean>()

        val combined: LiveData<List<Boolean?>> =
                combineMultiple(source1, source2, source3, source4, source5) { it }

        // required to trigger value updates
        combined.observeForever()

        assertEquals(combined.value, null)

        source1.value = null
        assertEquals(combined.value, listOf<Boolean?>(null, null, null, null, null))

        source1.value = true
        assertEquals(combined.value, listOf<Boolean?>(true, null, null, null, null))

        source2.value = false
        assertEquals(combined.value, listOf<Boolean?>(true, false, null, null, null))

        source3.value = null
        assertEquals(combined.value, listOf<Boolean?>(true, false, null, null, null))

        source4.value = false
        assertEquals(combined.value, listOf<Boolean?>(true, false, null, false, null))

        source5.value = true
        assertEquals(combined.value, listOf<Boolean?>(true, false, null, false, true))

        source1.value = null
        source2.value = null
        source3.value = true
        source4.value = null
        source5.value = null
        assertEquals(combined.value, listOf<Boolean?>(null, null, true, null, null))
    }

}