package com.fahh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.data.model.Sound
import com.fahh.ui.theme.Primary

@Composable
fun SoundGrid(
    sounds: List<Sound>,
    selectedSound: Sound,
    onSoundPreview: (Sound) -> Unit,
    onSoundSelected: (Sound) -> Unit,
    modifier: Modifier = Modifier
) {
    val unlocked = sounds.filter { !it.isLocked }
    val locked = sounds.filter { it.isLocked }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        itemsIndexed(unlocked) { _, sound ->
            SoundTile(
                sound = sound,
                isSelected = sound == selectedSound,
                onPreview = { onSoundPreview(sound) },
                onSelect = { onSoundSelected(sound) }
            )
        }
        if (locked.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Divider(
                    color = Color.White.copy(alpha = 0.08f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            itemsIndexed(locked) { _, sound ->
                SoundTile(
                    sound = sound,
                    isSelected = sound == selectedSound,
                    onPreview = { onSoundPreview(sound) },
                    onSelect = { onSoundSelected(sound) }
                )
            }
        }
    }
}

@Composable
private fun SoundTile(
    sound: Sound,
    isSelected: Boolean,
    onPreview: () -> Unit,
    onSelect: () -> Unit
) {
    val tileColor = when {
        isSelected -> Primary.copy(alpha = 0.18f)
        sound.isLocked -> Color.White.copy(alpha = 0.02f)
        else -> Color.White.copy(alpha = 0.10f)
    }

    val borderBrush = when {
        isSelected -> Brush.verticalGradient(listOf(Primary, Primary.copy(alpha = 0.4f)))
        sound.isLocked -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.02f)))
        else -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f)))
    }


    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tileColor)
                .padding(12.dp)
        ) {
            // Preview button — top right
            Surface(
                shape = CircleShape,
                color = if (sound.isLocked) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clickable(onClick = onPreview)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = if (sound.isLocked) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.fillMaxSize()) {
                // Sound name
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sound.isLocked) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 36.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = when {
                        sound.isLocked -> Color(0xFFFF6B00).copy(alpha = 0.15f)
                        isSelected -> Primary.copy(alpha = 0.2f)
                        else -> Primary.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = if (sound.isLocked) "WATCH 1 AD TO UNLOCK" else if (isSelected) "ACTIVE" else "READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sound.isLocked) Color(0xFFFF9D42) else Primary,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom row: pack name + status icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sound.packName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    val statusIcon = when {
                        sound.isLocked -> Icons.Default.Lock
                        isSelected -> Icons.Default.Check
                        else -> null
                    }
                    if (statusIcon != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Primary else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
