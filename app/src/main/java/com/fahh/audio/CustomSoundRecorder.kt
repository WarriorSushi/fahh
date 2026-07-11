package com.fahh.audio

import android.content.Context
import android.media.MediaRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomSoundRecorder @Inject constructor(@ApplicationContext private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): File {
        check(recorder == null) { "A custom sound recording is already active." }
        val directory = File(context.filesDir, "custom_sounds").apply { mkdirs() }
        val output = File(directory, "custom_${UUID.randomUUID()}.m4a")
        val newRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setMaxDuration(5_000)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        recorder = newRecorder
        outputFile = output
        return output
    }

    fun stop(): File {
        val activeRecorder = checkNotNull(recorder) { "No custom sound recording is active." }
        val output = checkNotNull(outputFile)
        try {
            activeRecorder.stop()
            return output
        } catch (exception: RuntimeException) {
            output.delete()
            throw IllegalStateException("Recording was too short. Hold record a little longer.", exception)
        } finally {
            activeRecorder.release()
            recorder = null
            outputFile = null
        }
    }

    fun cancel() {
        runCatching { recorder?.reset() }
        recorder?.release()
        outputFile?.delete()
        recorder = null
        outputFile = null
    }
}
