package com.fahh.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.ui.theme.Primary

@Composable
fun ComingSoonScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF080C12)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "Features we're cooking up",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
            )

            // Custom Sounds card
            FeatureCard(
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF65D892),
                title = "Custom Sounds",
                description = "Already delivered as promised. Record and keep private reaction sounds directly on your device.",
                delivered = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button Skins card
            FeatureCard(
                icon = Icons.Default.Palette,
                iconTint = Color(0xFFE879F9),
                title = "Button Skins, coming soon",
                description = "Customize your button with unique styles: arcade buttons, doorbells, nuclear launch buttons, and more."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF7DD3FC),
                title = "Share custom sounds, coming soon",
                description = "Send your five-second custom sounds through Android's share sheet, then preview and save them on another Fahh device."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.LightMode,
                iconTint = Color(0xFFFFD166),
                title = "Light theme, coming soon",
                description = "A purpose-built bright mode that keeps the big red button and camera controls crisp in daylight."
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Got ideas? Email us at",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@altcorp.in")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "contact@altcorp.in",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    delivered: Boolean = false
) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, Color.White.copy(alpha = 0.06f)
        ),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = iconTint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (delivered) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "DELIVERED",
                        color = Color(0xFF65D892),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 22.sp
            )
        }
    }
}
