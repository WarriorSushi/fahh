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
        val sounds = listOf(
            R.raw.air_horn,
            R.raw.bruh,
            R.raw.directed_by,
            R.raw.dun_dun_dunn,
            R.raw.fahh,
            R.raw.gop_gop_gop,
            R.raw.oh_my_god_wow,
            R.raw.romance_saxophone,
            R.raw.sudden_suspense,
            R.raw.vine_boom,
            R.raw.wow,
            R.raw.yoooooo_japan
        )
        
        sounds.forEach { resId ->
            val soundId = soundPool.load(context, resId, 1)
            soundMap[resId] = soundId
        }
    }

    fun playSound(resId: Int, volume: Float = 1.0f) {
        val soundId = soundMap[resId]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            Log.e("SoundManager", "Sound not loaded for resource ID: $resId")
        }
    }

    fun release() {
        soundPool.release()
    }
}
