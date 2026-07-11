package com.fahh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.data.model.Sound
import com.fahh.ui.theme.Primary

@Composable
fun SidebarMenu(
    sounds: List<Sound>,
    selectedSound: Sound,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundPreview: (Sound) -> Unit,
    onSoundSelected: (Sound) -> Unit,
    soundPressCounts: Map<String, Int> = emptyMap(),
    noticeMessage: String?,
    onDismissNotice: () -> Unit,
    onClose: () -> Unit,
    onPrivacyClick: () -> Unit,
    onMySoundsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTipJarClick: () -> Unit = {},
    title: String = "Sounds",
    subtitle: String = "Pick one, then hit Fahh",
    showMoreSoundsAction: Boolean = true,
    showUtilityDock: Boolean = true,
    onOpenMoreSounds: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var fullLibraryOpen by rememberSaveable { mutableStateOf(false) }
    var volumeExpanded by rememberSaveable { mutableStateOf(false) }
    val supportPulseTransition = rememberInfiniteTransition(label = "supportPulse")
    val supportPulse by supportPulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "supportPulseScale"
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(352.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF211117),
                        Color(0xFF13090D)
                    )
                ),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
            )
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)
            ) {
                Surface(
                    color = Primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
                    Text(
                        text = if (fullLibraryOpen) "All sounds" else title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (fullLibraryOpen) "Keep browsing, unlock when ready" else subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                if (fullLibraryOpen) {
                    IconButton(onClick = { fullLibraryOpen = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to quick sounds", tint = Color.White.copy(alpha = 0.65f))
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close menu",
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notice bar
            AnimatedVisibility(
                visible = !noticeMessage.isNullOrBlank(),
                enter = slideInVertically(initialOffsetY = { -it / 3 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut()
            ) {
                if (!noticeMessage.isNullOrBlank()) {
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF9B8A),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = noticeMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp)
                            )
                            IconButton(
                                onClick = onDismissNotice,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Divider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = Color.White.copy(alpha = 0.06f)
            )

            // Sound grid
            SoundGrid(
                sounds = sounds,
                selectedSound = selectedSound,
                onSoundPreview = onSoundPreview,
                onSoundSelected = onSoundSelected,
                soundPressCounts = soundPressCounts,
                onMoreSoundsClick = if (showMoreSoundsAction && !fullLibraryOpen) ({
                    onOpenMoreSounds?.invoke() ?: run { fullLibraryOpen = true }
                }) else null,
                modifier = Modifier.weight(1f)
            )

            Divider(color = Color.White.copy(alpha = 0.06f))

            if (showUtilityDock) Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4A2028))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = onMySoundsClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF642C36),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Icon(Icons.Default.Mic, contentDescription = "Custom sounds", tint = Color.White, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Custom sounds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { volumeExpanded = !volumeExpanded },
                            shape = RoundedCornerShape(14.dp),
                            color = if (volumeExpanded) Color(0xFFA33C42) else Color(0xFF642C36),
                            border = if (volumeExpanded) BorderStroke(1.dp, Primary.copy(alpha = 0.82f)) else null,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Master volume", tint = Color.White, modifier = Modifier.size(19.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Volume", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${(volume * 100).toInt()}%", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = volumeExpanded,
                            onDismissRequest = { volumeExpanded = false }
                        ) {
                            Column(
                                modifier = Modifier.width(76.dp).padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    color = Primary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Box(modifier = Modifier.size(width = 76.dp, height = 260.dp), contentAlignment = Alignment.Center) {
                                Slider(
                                    value = volume,
                                    onValueChange = onVolumeChange,
                                    // requiredWidth prevents the narrow popup from constraining
                                    // the horizontal slider before it is rotated vertically.
                                    modifier = Modifier.requiredWidth(236.dp).graphicsLayer { rotationZ = -90f },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Primary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // More cool + Tip Jar row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = onSettingsClick,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.06f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "More cool",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "More cool",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        onClick = onTipJarClick,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFC857),
                        shadowElevation = 8.dp,
                        modifier = Modifier.weight(1f).graphicsLayer {
                            scaleX = supportPulse
                            scaleY = supportPulse
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "\u2615",
                                fontSize = 14.sp,
                                color = Color(0xFF28160A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Support Us",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF28160A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
