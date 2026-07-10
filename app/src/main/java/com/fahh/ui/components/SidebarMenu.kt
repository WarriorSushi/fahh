package com.fahh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
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
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(352.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF080C12)
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
                onMoreSoundsClick = if (showMoreSoundsAction && !fullLibraryOpen) ({
                    onOpenMoreSounds?.invoke() ?: run { fullLibraryOpen = true }
                }) else null,
                modifier = Modifier.weight(1f)
            )

            Divider(color = Color.White.copy(alpha = 0.06f))

            if (showUtilityDock) Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121A26))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Surface(
                    onClick = onMySoundsClick,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF202C3C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Text(text = "\uD83C\uDFA4", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Custom sounds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Record and keep your own", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                        }
                        Text("→", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Master Volume",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = "Low",
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "High",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Settings + Tip Jar row
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
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        onClick = onTipJarClick,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.06f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "\u2615",
                                fontSize = 14.sp,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Support Us",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD700).copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
