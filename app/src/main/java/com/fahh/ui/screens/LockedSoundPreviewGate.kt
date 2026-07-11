package com.fahh.ui.screens

/** Keeps preview entitlement local to this screen session and separate from unlocking. */
class LockedSoundPreviewGate {
    private val previewsBySoundId = mutableMapOf<String, Int>()

    fun tryConsume(soundId: String): Boolean {
        val used = previewsBySoundId[soundId] ?: 0
        if (used >= MAX_PREVIEWS) return false
        previewsBySoundId[soundId] = used + 1
        return true
    }

    fun usedPreviews(soundId: String): Int = previewsBySoundId[soundId] ?: 0

    companion object {
        const val MAX_PREVIEWS = 2
    }
}
