package com.fahh

import com.fahh.ui.screens.ComboWindowCounter
import org.junit.Assert.assertEquals
import org.junit.Test

class ComboWindowCounterTest {
    @Test
    fun `counts every tap before the three second boundary`() {
        val counter = ComboWindowCounter()

        assertEquals(1, counter.registerTap(10_000L))
        assertEquals(2, counter.registerTap(10_100L))
        assertEquals(3, counter.registerTap(12_999L))
    }

    @Test
    fun `tap at the boundary starts a fresh window`() {
        val counter = ComboWindowCounter()

        assertEquals(1, counter.registerTap(10_000L))
        assertEquals(2, counter.registerTap(12_999L))
        assertEquals(1, counter.registerTap(13_000L))
        assertEquals(2, counter.registerTap(13_010L))
    }

    @Test
    fun `continuous taps still reset after each fixed window`() {
        val counter = ComboWindowCounter()
        repeat(30) { index -> assertEquals(index + 1, counter.registerTap(index * 90L)) }

        assertEquals(1, counter.registerTap(3_000L))
        assertEquals(2, counter.registerTap(3_050L))
    }
}
