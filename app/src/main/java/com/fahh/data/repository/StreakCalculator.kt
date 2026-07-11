package com.fahh.data.repository

import java.time.LocalDate

internal fun calculateDailyStreak(lastActiveDate: LocalDate?, today: LocalDate, currentStreak: Int): Int =
    when (lastActiveDate) {
        today -> currentStreak
        today.minusDays(1) -> currentStreak + 1
        else -> 1
    }
