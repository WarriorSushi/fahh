package com.fahh.ui.screens

import android.media.MediaMetadataRetriever
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.Primary
import com.fahh.ui.theme.premiumGlass
import com.fahh.utils.VideoTrimUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TrimScreen(
    sourceFile: File,
    onBack: () -> Unit,
    onTrimmed: (File) -> Unit
) {
    BackHandler { onBack() }

    val snackbar = remember { SnackbarHostState() }
    val durationMs = remember(sourceFile.absolutePath) { readDurationMs(sourceFile) }
    val durationSec = (durationMs / 1000f).coerceAtLeast(1f)

    var trimRange by remember(durationSec) { mutableStateOf(0f..durationSec) }
    var isSaving by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var extraRotation by remember { mutableStateOf(0) } // 0, 90, 180, 270

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Custom HUD
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.premiumGlass(CircleShape, alpha = 0.1f).size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "EDIT REACTION",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { extraRotation = (extraRotation + 90) % 360 },
                    modifier = Modifier.premiumGlass(CircleShape, alpha = 0.1f).size(48.dp)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .premiumGlass(RoundedCornerShape(24.dp), alpha = 0.05f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            setVideoURI(sourceFile.toUri())
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                seekTo((trimRange.start * 1000f).toInt())
                            }
                            videoViewRef = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = extraRotation.toFloat() }
                )

                if (!isPlaying) {
                    Surface(
                        onClick = {
                            videoViewRef?.let { view ->
                                view.start()
                                isPlaying = true
                            }
                        },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp).premiumGlass(CircleShape, alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Range Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "SELECT RANGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Black
                    )

                    RangeSlider(
                        value = trimRange,
                        onValueChange = { range ->
                            if (!isSaving) {
                                val minSpan = 1f
                                trimRange = if ((range.endInclusive - range.start) < minSpan) {
                                    range.start..(range.start + minSpan).coerceAtMost(durationSec)
                                } else {
                                    range
                                }
                                // Live scrub: pause and seek to the start of the selected range
                                videoViewRef?.let { view ->
                                    if (view.isPlaying) {
                                        view.pause()
                                        isPlaying = false
                                    }
                                    view.seekTo((trimRange.start * 1000f).toInt())
                                }
                            }
                        },
                        valueRange = 0f..durationSec,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Primary,
                            thumbColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TimeBadge(label = "START", time = formatSeconds(trimRange.start))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = formatSeconds(trimRange.endInclusive - trimRange.start),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        TimeBadge(label = "END", time = formatSeconds(trimRange.endInclusive))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isSaving = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val startMs = (trimRange.start * 1000f).toLong()
                        val endMs = (trimRange.endInclusive * 1000f).toLong()
                        val outputDir = sourceFile.parentFile ?: sourceFile.absoluteFile.parentFile
                        if (outputDir == null) {
                            snackbar.showSnackbar("Error: Output directory not found")
                            isSaving = false
                            return@launch
                        }
                        val outputFile = File(
                            outputDir,
                            "trimmed_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.mp4"
                        )

                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                VideoTrimUtils.trimVideo(sourceFile, outputFile, startMs, endMs, extraRotation)
                                outputFile
                            }
                        }

                        isSaving = false
                        result.onSuccess { onTrimmed(it) }.onFailure {
                            snackbar.showSnackbar("Trim failed. Please try a different range.")
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(strokeWidth = 3.dp, color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.ContentCut, contentDescription = "Trim")
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Finish Trimming", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun TimeBadge(label: String, time: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = time, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private fun readDurationMs(file: File): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(file.absolutePath)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (_: Exception) {
        0L
    } finally {
        runCatching { retriever.release() }
    }
}

private fun formatSeconds(seconds: Float): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val minutes = total / 60
    val secs = total % 60
    return String.format(Locale.US, "%02d:%02d", minutes, secs)
}
