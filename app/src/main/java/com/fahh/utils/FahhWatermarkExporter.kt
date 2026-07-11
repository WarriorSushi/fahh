package com.fahh.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Composition
import com.fahh.R
import java.io.File

/** Renders the same Fahh wordmark used in the app chrome into every newly exported clip. */
class FahhWatermarkExporter(private val context: Context) {
    fun export(inputFile: File, outputFile: File, onSuccess: (File) -> Unit, onError: (Exception) -> Unit) {
        val logo = loadLogo()
        val settings = OverlaySettings.Builder()
            .setBackgroundFrameAnchor(0.94f, -0.93f)
            .setOverlayFrameAnchor(1f, -1f)
            .setAlphaScale(0.56f)
            .build()
        val overlay = BitmapOverlay.createStaticBitmapOverlay(logo, settings)
        val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(inputFile.toUri()))
            .setEffects(Effects(emptyList(), listOf(OverlayEffect(listOf(overlay)))))
            .build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onSuccess(outputFile)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    runCatching { outputFile.delete() }
                    onError(exportException)
                }
            })
            .build()

        transformer.start(editedMediaItem, outputFile.absolutePath)
    }

    private fun loadLogo(): Bitmap {
        val source = requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.fahh_logo_wide))
        val targetWidth = 190
        val targetHeight = (source.height * (targetWidth.toFloat() / source.width)).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
