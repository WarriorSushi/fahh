package com.fahh.ui.screens

/** Counts taps in fixed windows anchored to the first tap of each three-second run. */
internal class ComboWindowCounter(private val windowDurationMs: Long = 3_000L) {
    private var windowStartedAtMs: Long? = null
    private var tapCount = 0

    fun registerTap(nowMs: Long): Int {
        val startedAt = windowStartedAtMs
        if (startedAt == null || nowMs < startedAt || nowMs - startedAt >= windowDurationMs) {
            windowStartedAtMs = nowMs
            tapCount = 0
        }
        tapCount += 1
        return tapCount
    }
}
