package com.fahh.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    // === 3D DEPTH CONFIG ===
    val totalDepth = buttonSize * 0.06f  // side wall height visible when unpressed

    // Spring physics — snappy press, bouncy release
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = 800f
        ),
        label = "press"
    )

    // Button sinks exactly totalDepth so side wall is fully hidden when pressed
    val sinkDistance = totalDepth * pressProgress
    val elevation = (20f * (1f - pressProgress) + 2f).dp
    val scale = 1f - (pressProgress * 0.015f)

    // === COLOR SHIFT ON PRESS ===
    // Button gets darker/warmer when pressed (like real plastic under pressure)
    val faceTopColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFE85A5A) else Color(0xFFFF9494),
        animationSpec = tween(100),
        label = "faceTop"
    )
    val faceMidColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFCC3333) else Color(0xFFEF4444),
        animationSpec = tween(100),
        label = "faceMid"
    )
    val faceBotColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF8B1A1A) else Color(0xFFB91C1C),
        animationSpec = tween(100),
        label = "faceBot"
    )

    // Extra space for the side depth below the button
    Box(
        modifier = modifier
            .size(buttonSize, buttonSize + totalDepth),
        contentAlignment = Alignment.TopCenter
    ) {
        // Particle Burst Layer
        Box(
            modifier = Modifier.size(buttonSize).align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            ParticleBurst(
                trigger = triggerBurst,
                onFinish = { triggerBurst = false }
            )
        }

        // === AMBIENT GLOW ===
        Box(
            modifier = Modifier
                .size(buttonSize * 0.9f)
                .align(Alignment.TopCenter)
                .blur(40.dp)
                .graphicsLayer { alpha = 0.4f - (pressProgress * 0.2f) }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // === RECESSED WELL (the hole the button sits in) ===
        Box(
            modifier = Modifier
                .size(buttonSize * 1.04f)
                .align(Alignment.TopCenter)
                .offset(y = totalDepth * 0.3f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0F1318),  // center — darker
                            Color(0xFF161C24)   // edge — slightly lighter
                        )
                    )
                )
        )

        // === 3D SIDE WALL ===
        // Sits directly below button face, only visible when unpressed
        Box(
            modifier = Modifier
                .size(buttonSize * 0.96f)
                .align(Alignment.TopCenter)
                .offset(y = totalDepth)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFB83838),  // top — lit
                            Color(0xFF7A2020),  // mid
                            Color(0xFF4A1010)   // bottom — shadow
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF992828), Color(0xFF3A0808))
                    ),
                    shape = CircleShape
                )
        )

        // === MAIN BUTTON FACE ===
        Box(
            modifier = Modifier
                .size(buttonSize)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    translationY = sinkDistance.toPx()
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    ambientColor = Color(0xFF330000),
                    spotColor = Primary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(faceTopColor, faceMidColor, faceBotColor)
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f * (1f - pressProgress * 0.5f)),
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            // Fire sound immediately on press (finger down)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            triggerBurst = true
                            onClick()

                            val press = PressInteraction.Press(offset)
                            scope.launch { interactionSource.emit(press) }
                            tryAwaitRelease()
                            scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // === SPECULAR HIGHLIGHT (top shine) ===
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        // Top crescent highlight — fades when pressed
                        drawOval(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.30f * (1f - pressProgress * 0.7f)),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.35f
                            ),
                            size = size
                        )
                    }
            )

            // === INNER BEVEL RING ===
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.12f * (1f - pressProgress * 0.5f)),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // === TEXT LABEL ===
            Text(
                text = sound.name.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = 0.95f - (pressProgress * 0.1f)),
                fontSize = (buttonSize.value * 0.14f).sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }
    }
}
