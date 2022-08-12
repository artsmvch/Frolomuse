package com.frolo.muse.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ThemeTest {

    /**
     * Testing that all themes have a unique ID.
     */
    @Test
    fun test_distinctById() {
        Theme.values().also { themes ->
            themes.forEach { theme ->
                assertEquals(themes.count { it.id == theme.id }, 1)
            }
        }
    }

    @Test
    fun test_findById() {
        assertEquals(Theme.findById(Theme.LIGHT_BLUE.id), Theme.LIGHT_BLUE)
        assertEquals(Theme.findById(Theme.DARK_BLUE.id), Theme.DARK_BLUE)
        assertEquals(Theme.findById(Theme.DARK_BLUE_ESPECIAL.id), Theme.DARK_BLUE_ESPECIAL)
        assertEquals(Theme.findById(Theme.DARK_PURPLE.id), Theme.DARK_PURPLE)
        assertEquals(Theme.findById(Theme.DARK_ORANGE.id), Theme.DARK_ORANGE)
        assertEquals(Theme.findById(0), null)
        assertEquals(Theme.findById(20), null)
        assertEquals(Theme.findById(1000), null)
    }

    @Test
    fun test_findByIdOrDefault() {
        assertEquals(Theme.findByIdOrDefault(Theme.LIGHT_BLUE.id, Theme.DARK_ORANGE), Theme.LIGHT_BLUE)
        assertEquals(Theme.findByIdOrDefault(Theme.DARK_BLUE.id, Theme.DARK_ORANGE), Theme.DARK_BLUE)
        assertEquals(Theme.findByIdOrDefault(Theme.DARK_BLUE_ESPECIAL.id, Theme.DARK_ORANGE), Theme.DARK_BLUE_ESPECIAL)
        assertEquals(Theme.findByIdOrDefault(Theme.DARK_PURPLE.id, Theme.DARK_ORANGE), Theme.DARK_PURPLE)
        assertEquals(Theme.findByIdOrDefault(Theme.DARK_ORANGE.id, Theme.LIGHT_BLUE), Theme.DARK_ORANGE)
        assertEquals(Theme.findByIdOrDefault(0, Theme.DARK_ORANGE), Theme.DARK_ORANGE)
        assertEquals(Theme.findByIdOrDefault(20, Theme.LIGHT_BLUE), Theme.LIGHT_BLUE)
        assertEquals(Theme.findByIdOrDefault(1000, null), null)
    }

}