package com.fahh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.data.model.Sound
import com.fahh.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SoundGrid(
    sounds: List<Sound>,
    selectedSound: Sound,
    onSoundPreview: (Sound) -> Unit,
    onSoundSelected: (Sound) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
        itemsIndexed(sounds) { index, sound ->
            SoundTile(
                sound = sound,
                isSelected = sound == selectedSound,
                index = index,
                onPreview = { onSoundPreview(sound) },
                onSelect = { onSoundSelected(sound) }
            )
        }
    }
}

@Composable
private fun SoundTile(
    sound: Sound,
    isSelected: Boolean,
    index: Int,
    onPreview: () -> Unit,
    onSelect: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 35L)
        isVisible = true
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "entranceScale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "entranceAlpha"
    )

    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "selectionScale"
    )

    val tileColor = when {
        sound.isLocked -> Color.White.copy(alpha = 0.03f)
        isSelected -> Primary.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.06f)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .height(160.dp)
            .graphicsLayer {
                scaleX = animatedScale * selectionScale
                scaleY = animatedScale * selectionScale
                alpha = animatedAlpha
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) 
                    Brush.verticalGradient(listOf(Primary, Primary.copy(alpha = 0.5f)))
                else 
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tileColor)
                .padding(14.dp)
        ) {
            // Preview Icon Highlighted
            Surface(
                shape = CircleShape,
                color = if (sound.isLocked) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(38.dp)
                    .clickable(onClick = onPreview)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = if (sound.isLocked) Color.White.copy(alpha = 0.4f) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Selection Status Icon
            val statusIcon = when {
                sound.isLocked -> Icons.Default.Lock
                isSelected -> Icons.Default.Check
                else -> Icons.Default.Add
            }

            Surface(
                shape = CircleShape,
                color = if (isSelected) Primary else Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 40.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (sound.isLocked) Color.White.copy(alpha = 0.06f) else Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (sound.isLocked) "LOCKED" else "READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sound.isLocked) Color.White.copy(alpha = 0.4f) else Primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = sound.packName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

