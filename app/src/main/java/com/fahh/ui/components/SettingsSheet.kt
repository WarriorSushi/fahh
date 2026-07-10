package com.fahh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.ui.theme.Primary

private data class MilestoneTier(
    val index: Int,
    val taps: Int,
    val label: String,
    val emoji: String,
    val color: Color,
    val desc: String
)

private val milestones = listOf(
    MilestoneTier(1, 2, "2x COMBO", "\u26A1", Color(0xFF90CAF9), "2 taps in 3 seconds"),
    MilestoneTier(2, 4, "4x COMBO", "\uD83D\uDD25", Color(0xFFFF9100), "4 taps in 3 seconds"),
    MilestoneTier(3, 7, "7x COMBO", "\uD83D\uDCA5", Color(0xFFFFAB40), "7 taps in 3 seconds"),
    MilestoneTier(4, 10, "10x COMBO", "\uD83C\uDF1F", Color(0xFFFFD740), "10 taps in 3 seconds"),
    MilestoneTier(5, 15, "TAP MONSTER", "\uD83D\uDC7E", Color(0xFF76FF03), "15 taps in 3 seconds"),
    MilestoneTier(6, 17, "SPEED DEMON", "\uD83E\uDD2F", Color(0xFFE040FB), "17 taps in 3 seconds"),
    MilestoneTier(7, 20, "ULTRA LEGENDARY PRO", "\uD83D\uDC51", Color(0xFFFFD700), "20 taps in 3 seconds"),
    MilestoneTier(8, 25, "CHEATER", "\uD83D\uDEA8", Color(0xFFFF1744), "25 taps in 3 seconds"),
    MilestoneTier(9, 30, "FINGER GOD", "\uD83D\uDC80", Color(0xFFBB86FC), "30 taps in 3 seconds"),
    MilestoneTier(10, 40, "DIMENSION BREAKER", "\uD83C\uDF00", Color(0xFF00E5FF), "40 taps in 3 seconds"),
    MilestoneTier(11, 50, "TOUCH GRASS", "\uD83E\uDEE0", Color(0xFFFF4081), "50 taps in 3 seconds"),
    MilestoneTier(12, 60, "ARE YOU OK?", "\uD83E\uDDE0", Color(0xFF00E676), "60 taps in 3 seconds")
)

@Composable
fun SettingsSheet(
    highestComboTier: Int = 0,
    onPrivacyClick: () -> Unit,
    onComingSoonClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(340.dp)
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
                    .padding(start = 8.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = Color.White.copy(alpha = 0.06f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Coming Soon button
                Surface(
                    onClick = onComingSoonClick,
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Coming Soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Privacy & Terms button
                Surface(
                    onClick = onPrivacyClick,
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Privacy & Terms",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Combo Milestones section
                Text(
                    text = "COMBO MILESTONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vertical timeline
                milestones.forEachIndexed { index, tier ->
                    val unlocked = highestComboTier >= tier.index
                    val isLast = index == milestones.lastIndex

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .height(IntrinsicSize.Min)
                    ) {
                        // Timeline column: dot + line
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(28.dp)
                        ) {
                            // Dot
                            Box(
                                modifier = Modifier
                                    .size(if (unlocked) 14.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (unlocked) tier.color
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                            )
                            // Vertical line
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .weight(1f)
                                        .background(
                                            if (unlocked && highestComboTier >= milestones.getOrNull(index + 1)?.index ?: 999)
                                                tier.color.copy(alpha = 0.4f)
                                            else Color.White.copy(alpha = 0.06f)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Content
                        Column(
                            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tier.emoji,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tier.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (unlocked) tier.color else Color.White.copy(alpha = 0.25f),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (unlocked) "Unlocked!" else tier.desc,
                                fontSize = 11.sp,
                                color = if (unlocked) Color.White.copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                Text(
                    text = "Every combo is counted inside a rolling 3-second window.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.42f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // App version
            Text(
                text = "Fahh v1.0.6",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
