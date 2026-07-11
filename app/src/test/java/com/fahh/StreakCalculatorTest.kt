package com.fahh

import com.fahh.data.repository.calculateDailyStreak
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    private val today = LocalDate.of(2026, 7, 12)

    @Test
    fun `same day does not increment streak`() {
        assertEquals(4, calculateDailyStreak(today, today, 4))
    }

    @Test
    fun `consecutive day increments streak`() {
        assertEquals(5, calculateDailyStreak(today.minusDays(1), today, 4))
    }

    @Test
    fun `missed or invalid previous day resets streak`() {
        assertEquals(1, calculateDailyStreak(today.minusDays(2), today, 4))
        assertEquals(1, calculateDailyStreak(null, today, 4))
    }
}
