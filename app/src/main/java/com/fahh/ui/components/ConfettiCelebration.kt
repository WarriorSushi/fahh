package com.fahh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.*

@Composable
fun ConfettiCelebration(
    trigger: Boolean,
    onFinish: () -> Unit
) {
    if (!trigger) return

    val particles = remember { mutableStateListOf<Particle>() }
    val random = remember { Random() }
    val colors = listOf(Color.Yellow, Color.Cyan, Color.Magenta, Color.Green, Color.Red)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        particles.clear()
        repeat(100) {
            particles.add(
                Particle(
                    x = random.nextFloat(),
                    y = -0.1f,
                    vx = (random.nextFloat() - 0.5f) * 0.05f,
                    vy = random.nextFloat() * 0.05f + 0.02f,
                    color = colors.random(),
                    size = random.nextFloat() * 15 + 5
                )
            )
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = LinearEasing)
        )
        onFinish()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        particles.forEach { particle ->
            val x = (particle.x + particle.vx * p * 50) * size.width
            val y = (particle.y + particle.vy * p * 50) * size.height
            if (y < size.height) {
                drawCircle(
                    color = particle.color.copy(alpha = 1f - (y / size.height).coerceIn(0f, 1f)),
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}
