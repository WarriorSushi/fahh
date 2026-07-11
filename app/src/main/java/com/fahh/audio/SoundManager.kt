package com.fahh.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
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
    private var customPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var customStopAction: Runnable? = null
    
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
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // Load on demand then play after a short delay.
            loadSound(resId)
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    soundPool.play(sampleId, volume, volume, 1, 0, 1.0f)
                }
            }
        }
    }

    private fun playCustomSound(filePath: String, volume: Float) {
        val file = File(filePath)
        if (!file.exists()) return
        releaseCustomPlayer()
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

    /** Plays only the in-progress trim selection, before it is saved. */
    fun playCustomSelection(filePath: String, startMs: Long, endMs: Long, volume: Float = 1.0f) {
        val file = File(filePath)
        if (!file.exists() || endMs <= startMs) return
        releaseCustomPlayer()
        customPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(file.absolutePath)
            setVolume(volume, volume)
            setOnPreparedListener { player ->
                player.seekTo(startMs.toInt())
                player.start()
                val stop = Runnable {
                    if (customPlayer === player) releaseCustomPlayer()
                }
                customStopAction = stop
                mainHandler.postDelayed(stop, (endMs - startMs).coerceAtLeast(120L))
            }
            setOnCompletionListener { releaseCustomPlayer() }
            setOnErrorListener { _, _, _ -> releaseCustomPlayer(); true }
            prepareAsync()
        }
    }

    private fun releaseCustomPlayer() {
        customStopAction?.let(mainHandler::removeCallbacks)
        customStopAction = null
        customPlayer?.release()
        customPlayer = null
    }

    fun release() {
        releaseCustomPlayer()
        soundPool.release()
    }
}
