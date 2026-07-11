package com.fahh.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.fahh.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FahhWatermarkExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var activeTransformer: Transformer? = null

    @OptIn(UnstableApi::class)
    fun export(
        inputFile: File,
        outputFile: File,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (activeTransformer != null) {
            onError(IllegalStateException("A watermark export is already running."))
            return
        }

        val sourceBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fahh_logo_wide)
        if (sourceBitmap == null) {
            onError(IllegalStateException("The Fahh watermark asset could not be decoded."))
            return
        }

        val overlayBitmap = Bitmap.createScaledBitmap(sourceBitmap, 180, 70, true)
        if (overlayBitmap !== sourceBitmap) sourceBitmap.recycle()

        val overlaySettings = OverlaySettings.Builder()
            .setBackgroundFrameAnchor(0.94f, -0.92f)
            .setOverlayFrameAnchor(1f, -1f)
            .setAlphaScale(0.82f)
            .build()
        val overlay = BitmapOverlay.createStaticBitmapOverlay(overlayBitmap, overlaySettings)
        val videoEffects: List<Effect> = listOf(OverlayEffect(listOf(overlay)))
        val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(inputFile.toUri()))
            .setEffects(Effects(emptyList(), videoEffects))
            .build()

        outputFile.delete()
        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    activeTransformer = null
                    overlayBitmap.recycle()
                    onSuccess()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    activeTransformer = null
                    overlayBitmap.recycle()
                    outputFile.delete()
                    onError(exportException)
                }
            })
            .build()
        activeTransformer = transformer

        try {
            transformer.start(editedMediaItem, outputFile.absolutePath)
        } catch (error: Exception) {
            activeTransformer = null
            overlayBitmap.recycle()
            outputFile.delete()
            onError(error)
        }
    }
}
