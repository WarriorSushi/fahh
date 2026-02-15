package com.fahh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.*

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1f
)

@Composable
fun ParticleBurst(
    trigger: Boolean,
    onFinish: () -> Unit
) {
    if (!trigger) return

    val particles = remember { mutableStateListOf<Particle>() }
    val random = remember { Random() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        particles.clear()
        repeat(30) {
            val angle = random.nextFloat() * 2 * Math.PI
            val speed = random.nextFloat() * 15 + 5
            particles.add(
                Particle(
                    x = 0f,
                    y = 0f,
                    vx = (speed * Math.cos(angle)).toFloat(),
                    vy = (speed * Math.sin(angle)).toFloat(),
                    color = primaryColor.copy(alpha = random.nextFloat() * 0.5f + 0.5f),
                    size = random.nextFloat() * 10 + 5
                )
            )
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = LinearOutSlowInEasing)
        )
        onFinish()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val p = progress.value

        particles.forEach { particle ->
            val alpha = (1f - p).coerceIn(0f, 1f)
            if (alpha > 0f) {
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size * (1f - p * 0.5f),
                    center = Offset(
                        centerX + particle.vx * p * 20,
                        centerY + particle.vy * p * 20
                    )
                )
            }
        }
    }
}
