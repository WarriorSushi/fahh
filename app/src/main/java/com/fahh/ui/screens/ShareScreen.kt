package com.fahh.ui.screens

import android.content.Intent
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.Primary
import com.fahh.ui.theme.premiumGlass
import java.io.File

@Composable
fun ShareScreen(
    videoFile: File,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onTrim: () -> Unit,
    onDelete: () -> Unit
) {
    BackHandler { onBack() }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
            videoViewRef = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "VIDEO SAVED",
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Video Preview Card
            Box(
                modifier = Modifier
                    .height(320.dp)
                    .fillMaxWidth()
                    .premiumGlass(RoundedCornerShape(32.dp), alpha = 0.04f)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable {
                        val view = videoViewRef
                        if (view != null) {
                            if (isPlaying) view.pause() else view.start()
                            isPlaying = !isPlaying
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            setVideoPath(videoFile.absolutePath)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                seekTo(1)
                            }
                            videoViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (!isPlaying) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp).premiumGlass(CircleShape, alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Platform share buttons
                val context = LocalContext.current
                val videoUri = remember(videoFile) {
                    try {
                        FileProvider.getUriForFile(context, "com.fahh.fileprovider", videoFile)
                    } catch (e: Exception) { null }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SHARE TO",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SharePlatformButton(
                            icon = Icons.Default.Chat,
                            label = "WhatsApp",
                            tint = Color(0xFF25D366),
                            onClick = {
                                shareToApp(context, videoUri, "com.whatsapp", onShare)
                            }
                        )
                        SharePlatformButton(
                            icon = Icons.Default.CameraAlt,
                            label = "Reels",
                            tint = Color(0xFFE1306C),
                            onClick = {
                                shareToApp(context, videoUri, "com.instagram.android", onShare)
                            }
                        )
                        SharePlatformButton(
                            icon = Icons.Default.MusicNote,
                            label = "TikTok",
                            tint = Color.White,
                            onClick = {
                                shareToApp(context, videoUri, "com.zhiliaoapp.musically", onShare)
                            }
                        )
                        SharePlatformButton(
                            icon = Icons.Default.Share,
                            label = "More",
                            tint = Color.White,
                            bgAlpha = 0.12f,
                            onClick = onShare
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onTrim,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ContentCut, contentDescription = "Trim", modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trim", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }

                    // Saved indicator
                    Surface(
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Saved", modifier = Modifier.size(14.dp), tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saved", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Surface(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = Color.Red.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete this video?", fontWeight = FontWeight.Bold) },
                        text = { Text("This action cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                onDelete()
                            }) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go Back", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SharePlatformButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    bgAlpha: Float = 0.15f,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = bgAlpha),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun shareToApp(
    context: android.content.Context,
    uri: android.net.Uri?,
    packageName: String,
    fallback: () -> Unit
) {
    if (uri == null) {
        fallback()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            setPackage(packageName)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
        fallback()
    }
}
