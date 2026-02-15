package com.fahh.utils

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

object VideoTrimUtils {
    fun trimVideo(
        inputFile: File,
        outputFile: File,
        startMs: Long,
        endMs: Long,
        extraRotationDegrees: Int = 0
    ) {
        require(startMs >= 0L) { "startMs must be >= 0" }
        require(endMs > startMs) { "endMs must be > startMs" }
        require(inputFile.exists()) { "Input file does not exist" }

        // Read rotation from source so trimmed output preserves orientation
        val rotation = try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(inputFile.absolutePath)
            val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            retriever.release()
            rot
        } catch (_: Exception) { 0 }

        val extractor = MediaExtractor()
        val muxer: MediaMuxer

        try {
            extractor.setDataSource(inputFile.absolutePath)
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val finalRotation = (rotation + extraRotationDegrees) % 360
            if (finalRotation != 0) {
                muxer.setOrientationHint(finalRotation)
            }

            val trackIndexMap = HashMap<Int, Int>()
            var maxBufferSize = 1024 * 1024

            for (index in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(index)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/") || mime.startsWith("video/")) {
                    val dstIndex = muxer.addTrack(format)
                    trackIndexMap[index] = dstIndex
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        val inputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                        if (inputSize > maxBufferSize) maxBufferSize = inputSize
                    }
                }
            }

            if (trackIndexMap.isEmpty()) {
                throw IllegalStateException("No audio/video tracks found")
            }

            val startUs = startMs * 1000L
            val endUs = endMs * 1000L

            for (sourceTrack in trackIndexMap.keys) {
                extractor.selectTrack(sourceTrack)
            }

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            muxer.start()

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            var wroteAnySample = false

            while (true) {
                val sampleTrackIndex = extractor.sampleTrackIndex
                if (sampleTrackIndex < 0) break

                val sampleTime = extractor.sampleTime
                if (sampleTime > endUs) break

                if (!trackIndexMap.containsKey(sampleTrackIndex)) {
                    extractor.advance()
                    continue
                }

                if (sampleTime < startUs) {
                    extractor.advance()
                    continue
                }

                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) break

                bufferInfo.presentationTimeUs = sampleTime - startUs
                bufferInfo.flags = extractor.sampleFlags

                val destinationTrack = trackIndexMap[sampleTrackIndex] ?: break
                muxer.writeSampleData(destinationTrack, buffer, bufferInfo)
                wroteAnySample = true

                extractor.advance()
            }

            if (!wroteAnySample) {
                throw IllegalStateException("No samples were written for selected trim range")
            }

            muxer.stop()
            muxer.release()
        } finally {
            runCatching { extractor.release() }
        }
    }
}
