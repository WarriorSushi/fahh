package com.fahh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.data.model.Sound
import com.fahh.ui.theme.Primary

@Composable
fun SoundButton(
    sound: Sound,
    onClick: () -> Unit,
    buttonSize: Dp = 260.dp,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var triggerBurst by remember { mutableStateOf(false) }

    // The total depth the button sits above the surface (the "side" height)
    val totalDepth = buttonSize * 0.07f

    // Spring physics: button presses INTO the screen
    val springSpec = spring<Float>(
        dampingRatio = 0.5f,       // bouncy on release
        stiffness = Spring.StiffnessMedium
    )

    // How far the button has sunk (0 = fully raised, 1 = fully pressed in)
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = springSpec,
        label = "pressProgress"
    )

    // Derived values from press progress
    val sinkOffset = totalDepth * pressProgress         // how far down the face moves
    val remainingDepth = totalDepth * (1f - pressProgress)  // visible side height
    val elevation = 24.dp * (1f - pressProgress) + 2.dp    // shadow shrinks as pressed
    val scale = 1f - (pressProgress * 0.03f)                // subtle scale reduction

    Box(
        modifier = modifier.size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        // Particle Burst Layer
        ParticleBurst(
            trigger = triggerBurst,
            onFinish = { triggerBurst = false }
        )

        // Ambient glow underneath
        Box(
            modifier = Modifier
                .size(buttonSize * 0.8f)
                .blur(28.dp)
                .graphicsLayer { alpha = 0.35f - (pressProgress * 0.15f) }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // 3D Side / Depth (the "wall" visible when button is raised)
        // This sits at the bottom and gets shorter as the button presses in
        Box(
            modifier = Modifier
                .size(buttonSize * 0.96f)
                .offset(y = totalDepth)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7A1616), Color(0xFF3D0505))
                    )
                )
                .graphicsLayer { alpha = 1f - (pressProgress * 0.4f) }
        )

        // Main button face — moves down as pressed
        Box(
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer {
                    translationY = sinkOffset.toPx()
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    ambientColor = Color.Black,
                    spotColor = Primary.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFF8A8A), // top highlight
                            Color(0xFFEF4444), // main red
                            Color(0xFFC62828)  // bottom shade
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        triggerBurst = true
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular highlight — fades when pressed (light changes)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawOval(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f * (1f - pressProgress * 0.6f)),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.4f
                            ),
                            size = size
                        )
                    }
            )

            // Text label
            Text(
                text = sound.name.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontSize = (buttonSize.value * 0.15f).sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
