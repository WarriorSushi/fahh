package com.fahh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.R
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Sequenced animation
        launch {
            reveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
            )
        }
        launch {
            delay(400)
            glowAlpha.animateTo(
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        delay(2600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Background Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .graphicsLayer { alpha = glowAlpha.value * reveal.value }
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = reveal.value
                scaleX = 0.9f + (reveal.value * 0.1f)
                scaleY = 0.9f + (reveal.value * 0.1f)
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.fahh_logo),
                contentDescription = "Fahh logo",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Premium Meme Effects",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        // Loader at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .height(2.dp)
                .width(120.dp)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                progress.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing))
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value)
                    .background(Primary)
            )
        }
    }
}
