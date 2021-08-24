package com.frolo.muse.engine

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class MathUtilTest {

    @Test
    fun test_clamp() {
        assertEquals(MathUtil.clamp(3f, 1f, 5f), 3f)

        assertEquals(MathUtil.clamp(5f, 1f, 5f), 5f)

        assertEquals(MathUtil.clamp(10f, 1f, 5f), 5f)

        assertEquals(MathUtil.clamp(1f, 1f, 5f), 1f)

        assertEquals(MathUtil.clamp(-10f, 1f, 5f), 1f)
    }

    @Test
    fun test_clampWithRange() {
        assertEquals(MathUtil.clamp(3f, MathUtil.Range(1f, 5f)), 3f)

        assertEquals(MathUtil.clamp(5f, MathUtil.Range(1f, 5f)), 5f)

        assertEquals(MathUtil.clamp(10f, MathUtil.Range(1f, 5f)), 5f)

        assertEquals(MathUtil.clamp(1f, MathUtil.Range(1f, 5f)), 1f)

        assertEquals(MathUtil.clamp(-10f, MathUtil.Range(1f, 5f)), 1f)
    }

}