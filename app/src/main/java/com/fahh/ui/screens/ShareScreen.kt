package com.fahh.ui.screens

import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "MASTERPIECE SAVED",
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Video Preview Card
            Box(
                modifier = Modifier
                    .weight(1f)
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
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Share to Socials", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onTrim,
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trim", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Surface(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", fontWeight = FontWeight.Bold, color = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

