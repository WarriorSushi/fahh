package com.fahh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val isRect: Boolean
)

@Composable
fun ConfettiCelebration(
    trigger: Boolean,
    onFinish: () -> Unit
) {
    if (!trigger) return

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val random = remember { Random }
    val colors = listOf(Color.Yellow, Color.Cyan, Color.Magenta, Color.Green, Color.Red)
    val progress = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        particles.clear()
        repeat(100) {
            particles.add(
                ConfettiParticle(
                    x = random.nextFloat(),
                    y = -0.1f,
                    vx = (random.nextFloat() - 0.5f) * 0.08f,
                    vy = random.nextFloat() * 0.07f + 0.02f,
                    color = colors.random(),
                    size = random.nextFloat() * 15 + 5,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 720f,
                    isRect = random.nextBoolean()
                )
            )
        }

        // Golden glow flash
        glowAlpha.snapTo(0.15f)
        glowAlpha.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = LinearEasing)
        )
        onFinish()
    }

    // Golden glow overlay
    if (glowAlpha.value > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD700).copy(alpha = glowAlpha.value))
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        particles.forEach { particle ->
            val x = (particle.x + particle.vx * p * 50) * size.width
            val y = (particle.y + particle.vy * p * 50) * size.height
            if (y < size.height) {
                val alpha = 1f - (y / size.height).coerceIn(0f, 1f)
                val rot = particle.rotation + particle.rotationSpeed * p
                rotate(degrees = rot, pivot = Offset(x, y)) {
                    if (particle.isRect) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                            size = Size(particle.size, particle.size * 0.6f)
                        )
                    } else {
                        drawCircle(
                            color = particle.color.copy(alpha = alpha),
                            radius = particle.size,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}
