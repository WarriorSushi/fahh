package com.fahh.utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

/** Copies the selected AAC section into a new local .m4a file without re-encoding it. */
object AudioTrimUtils {
    fun trimAudio(inputFile: File, outputFile: File, startMs: Long, endMs: Long) {
        require(inputFile.exists()) { "Recording file no longer exists." }
        require(startMs >= 0 && endMs > startMs) { "Choose a valid section to keep." }

        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        try {
            extractor.setDataSource(inputFile.absolutePath)
            val sourceTrack = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: error("No audio track found in this recording.")
            val format = extractor.getTrackFormat(sourceTrack)
            val bufferSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(64 * 1024)
            } else 256 * 1024
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val destinationTrack = muxer.addTrack(format)
            extractor.selectTrack(sourceTrack)
            extractor.seekTo(startMs * 1000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            muxer.start()
            muxerStarted = true

            val buffer = ByteBuffer.allocate(bufferSize)
            val info = MediaCodec.BufferInfo()
            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            var wroteSample = false
            while (extractor.sampleTrackIndex >= 0) {
                val sampleTime = extractor.sampleTime
                if (sampleTime > endUs) break
                if (sampleTime >= startUs) {
                    info.offset = 0
                    info.size = extractor.readSampleData(buffer, 0)
                    if (info.size < 0) break
                    info.presentationTimeUs = sampleTime - startUs
                    info.flags = extractor.sampleFlags
                    muxer.writeSampleData(destinationTrack, buffer, info)
                    wroteSample = true
                }
                extractor.advance()
            }
            check(wroteSample) { "No audio was found in that selection." }
        } catch (error: Throwable) {
            outputFile.delete()
            throw error
        } finally {
            if (muxerStarted) runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
            extractor.release()
        }
    }
}
