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
    private val loadedSamples = mutableSetOf<Int>()
    private val pendingPlays = mutableMapOf<Int, MutableList<Float>>()
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

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            val queuedVolumes = pendingPlays.remove(sampleId).orEmpty()
            if (status == 0) {
                loadedSamples += sampleId
                queuedVolumes.forEach { volume ->
                    soundPool.play(sampleId, volume, volume, 1, 0, 1.0f)
                }
            } else {
                soundMap.entries.removeAll { it.value == sampleId }
            }
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

    private fun loadSound(resId: Int): Int {
        soundMap[resId]?.let { return it }
        val soundId = soundPool.load(context, resId, 1)
        if (soundId != 0) soundMap[resId] = soundId
        return soundId
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
        val soundId = soundMap[resId] ?: loadSound(resId)
        if (soundId == 0) return

        if (soundId in loadedSamples) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            pendingPlays.getOrPut(soundId) { mutableListOf() }.add(volume)
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
                val durationMs = player.duration.toLong().coerceAtLeast(1L)
                val selectionStartMs = startMs.coerceIn(0L, durationMs - 1L)
                val selectionEndMs = endMs.coerceIn(selectionStartMs + 1L, durationMs)
                player.setOnSeekCompleteListener { seekedPlayer ->
                    if (customPlayer === seekedPlayer) {
                        seekedPlayer.start()
                        stopCustomSelectionAt(seekedPlayer, selectionEndMs)
                    }
                }
                // Seek even when the selection begins at zero. That guarantees playback
                // cannot start until the selected range has been established.
                player.seekTo(selectionStartMs.toInt())
            }
            setOnCompletionListener { releaseCustomPlayer() }
            setOnErrorListener { _, _, _ -> releaseCustomPlayer(); true }
            prepareAsync()
        }
    }

    /** Poll the real playback position so a delayed seek cannot turn a trim preview into a full clip. */
    private fun stopCustomSelectionAt(player: MediaPlayer, endMs: Long) {
        val stop = object : Runnable {
            override fun run() {
                if (customPlayer !== player) return
                val currentPosition = runCatching { player.currentPosition.toLong() }.getOrElse {
                    releaseCustomPlayer()
                    return
                }
                if (currentPosition >= endMs) {
                    releaseCustomPlayer()
                } else {
                    mainHandler.postDelayed(this, 25L)
                }
            }
        }
        customStopAction = stop
        mainHandler.post(stop)
    }

    private fun releaseCustomPlayer() {
        customStopAction?.let(mainHandler::removeCallbacks)
        customStopAction = null
        customPlayer?.release()
        customPlayer = null
    }

    fun release() {
        releaseCustomPlayer()
        pendingPlays.clear()
        loadedSamples.clear()
        soundMap.clear()
        soundPool.release()
    }
}
