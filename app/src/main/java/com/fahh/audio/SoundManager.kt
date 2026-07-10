package com.fahh.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.fahh.R

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    
    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
            
        preloadSounds()
    }

    private fun preloadSounds() {
        // Only preload the 4 free sounds; others load on demand
        val freeSounds = listOf(
            R.raw.fahh,
            R.raw.bruh,
            R.raw.vine_boom,
            R.raw.wow
        )

        freeSounds.forEach { resId ->
            loadSound(resId)
        }
    }

    private fun loadSound(resId: Int) {
        val soundId = soundPool.load(context, resId, 1)
        soundMap[resId] = soundId
    }

    fun playSound(resId: Int, volume: Float = 1.0f) {
        val soundId = soundMap[resId]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // Load on demand then play after a short delay
            loadSound(resId)
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    soundPool.play(sampleId, volume, volume, 1, 0, 1.0f)
                }
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}
