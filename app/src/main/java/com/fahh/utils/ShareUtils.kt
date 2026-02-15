package com.fahh.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {
    private const val LogTag = "ShareUtils"

    fun shareVideo(context: Context, videoFile: File) {
        val contentUri = videoFile.toContentUri(context) ?: return

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "video/mp4"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("shared_video", contentUri)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        grantUriPermissionsForIntent(context, shareIntent, contentUri)
        runSafely(context, "Unable to share this video.") {
            context.startActivity(Intent.createChooser(shareIntent, "Share video via"))
        }
    }

    fun openVideoEditor(context: Context, videoFile: File): Boolean {
        val contentUri = videoFile.toContentUri(context) ?: return false

        val editIntent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(contentUri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra("mimeType", "video/mp4")
            clipData = android.content.ClipData.newRawUri("editable_video", contentUri)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        grantUriPermissionsForIntent(context, editIntent, contentUri)
        if (editIntent.resolveActivity(context.packageManager) != null) {
            return runSafely(context, "Video editor could not be opened.") {
                context.startActivity(editIntent)
                true
            } ?: false
        }

        val trimIntent = Intent("com.android.camera.action.TRIM").apply {
            setDataAndType(contentUri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri("trim_video", contentUri)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        grantUriPermissionsForIntent(context, trimIntent, contentUri)
        if (trimIntent.resolveActivity(context.packageManager) != null) {
            return runSafely(context, "Video trimmer could not be opened.") {
                context.startActivity(trimIntent)
                true
            } ?: false
        }

        return try {
            throw ActivityNotFoundException("No compatible editor")
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No compatible video editor found on this device.",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    private fun File.toContentUri(context: Context): Uri? {
        if (!exists()) {
            Toast.makeText(context, "Video file is missing.", Toast.LENGTH_SHORT).show()
            return null
        }
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                this
            )
        } catch (exception: IllegalArgumentException) {
            Log.e(LogTag, "Failed to resolve file provider URI", exception)
            Toast.makeText(context, "Could not open this video file.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun grantUriPermissionsForIntent(context: Context, intent: Intent, uri: Uri) {
        val handlers = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        handlers.forEach { resolveInfo ->
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    private fun <T> runSafely(context: Context, toastMessage: String, block: () -> T): T? {
        return try {
            block()
        } catch (exception: Exception) {
            Log.e(LogTag, "Share/edit action failed", exception)
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            null
        }
    }
}
