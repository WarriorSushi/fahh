package com.fahh

import com.fahh.ui.screens.LockedSoundPreviewGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockedSoundPreviewGateTest {

    @Test
    fun `each locked sound gets exactly two previews`() {
        val gate = LockedSoundPreviewGate()

        assertTrue(gate.tryConsume("john_cena"))
        assertTrue(gate.tryConsume("john_cena"))
        assertFalse(gate.tryConsume("john_cena"))
        assertEquals(2, gate.usedPreviews("john_cena"))
    }

    @Test
    fun `preview allowance is independent for each sound`() {
        val gate = LockedSoundPreviewGate()

        repeat(2) { assertTrue(gate.tryConsume("john_cena")) }
        assertTrue(gate.tryConsume("sad_violin"))
        assertEquals(2, gate.usedPreviews("john_cena"))
        assertEquals(1, gate.usedPreviews("sad_violin"))
    }
}
