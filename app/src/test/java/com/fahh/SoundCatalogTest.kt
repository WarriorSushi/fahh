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

    @Test
    fun `first four new sounds are permanently free`() {
        assertEquals(
            setOf("buzzer", "chicken_scream", "core_effect", "crickets"),
            SoundCatalog.permanentlyFreeNewSoundIds
        )
        val freeNewSounds = SoundCatalog.sounds.filter { it.id in SoundCatalog.permanentlyFreeNewSoundIds }
        assertTrue(freeNewSounds.all { !it.isLocked })
    }

    @Test
    fun `retired long sound is absent and renamed sounds keep stable IDs`() {
        assertTrue(SoundCatalog.sounds.none { it.id == "sad_violin" })

        val labelsById = SoundCatalog.sounds.associate { it.id to it.name }
        assertEquals("Cina", labelsById["john_cena"])
        assertEquals("Taker", labelsById["undertaker_bell"])
        assertEquals("Hurt", labelsById["minecraft_hurt"])
        assertEquals("Oof", labelsById["roblox_oof"])
        assertEquals("Wasted", labelsById["gta_wasted"])
    }

    @Test
    fun `intentionally removed staged sounds stay out of the bundled catalog`() {
        val forbiddenFragments = setOf(
            "another_one_dj_khaled",
            "bad_to_the_bone",
            "rizzer",
            "you_need_to_stfu"
        )
        val normalizedCatalogText = SoundCatalog.sounds.flatMap { sound ->
            listOf(sound.id, sound.name, sound.packName)
        }.joinToString("_").lowercase().replace(Regex("[^a-z0-9]+"), "_")

        forbiddenFragments.forEach { fragment ->
            assertTrue("Removed sound $fragment must not be bundled", fragment !in normalizedCatalogText)
        }
    }
}
