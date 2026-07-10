package com.fahh

import com.fahh.data.catalog.SoundCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundCatalogTest {

    @Test
    fun `catalog sound IDs are unique and nonblank`() {
        val soundIds = SoundCatalog.sounds.map { it.id }

        assertEquals(soundIds.size, soundIds.toSet().size)
        assertTrue(soundIds.all { it.isNotBlank() })
    }

    @Test
    fun `starter sound remains available without an unlock`() {
        assertTrue(SoundCatalog.defaultSelectedSound.id == "fahh")
        assertTrue(!SoundCatalog.defaultSelectedSound.isLocked)
    }
}
