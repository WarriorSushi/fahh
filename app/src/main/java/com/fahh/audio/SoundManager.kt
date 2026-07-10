package com.fahh.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.fahh.R
import com.fahh.data.model.Sound
import java.io.File

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private val resourceBySampleId = mutableMapOf<Int, Int>()
    private val pendingPlayback = mutableMapOf<Int, Float>()
    private var activePoolStreamId: Int? = null
    private var customPlayer: MediaPlayer? = null
    
    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
            
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            val resId = resourceBySampleId[sampleId] ?: return@setOnLoadCompleteListener
            pendingPlayback.remove(resId)?.let { volume -> playLoadedSample(sampleId, volume) }
        }
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
        resourceBySampleId[soundId] = resId
    }

    fun playSound(sound: Sound, volume: Float = 1.0f) {
        val filePath = sound.filePath
        if (filePath != null) {
            playCustomSound(filePath, volume)
        } else {
            playSound(sound.resId, volume)
        }
    }

    private fun playSound(resId: Int, volume: Float = 1.0f) {
        val soundId = soundMap[resId]
        if (soundId != null && soundId != 0) {
            playLoadedSample(soundId, volume)
        } else {
            // The load callback starts only the most recently requested sound.
            pendingPlayback.clear()
            pendingPlayback[resId] = volume
            loadSound(resId)
        }
    }

    private fun playLoadedSample(sampleId: Int, volume: Float) {
        pendingPlayback.clear()
        stopActiveSound()
        activePoolStreamId = soundPool.play(sampleId, volume, volume, 1, 0, 1.0f)
    }

    private fun playCustomSound(filePath: String, volume: Float) {
        val file = File(filePath)
        if (!file.exists()) return
        stopActiveSound()
        customPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(file.absolutePath)
            setVolume(volume, volume)
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                it.release()
                if (customPlayer === it) customPlayer = null
            }
            setOnErrorListener { player, _, _ ->
                player.release()
                if (customPlayer === player) customPlayer = null
                true
            }
            prepareAsync()
        }
    }

    fun release() {
        stopActiveSound()
        soundPool.release()
    }

    /** Fahh is a reaction trigger, not a mixer. The latest tap always wins. */
    private fun stopActiveSound() {
        activePoolStreamId?.let(soundPool::stop)
        activePoolStreamId = null
        customPlayer?.release()
        customPlayer = null
    }
}
