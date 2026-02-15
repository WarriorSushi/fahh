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
import androidx.compose.material3.Surface
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

    // Premium Squash & Stretch Physics
    val scaleX by animateFloatAsState(
        targetValue = if (isPressed) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "squashX"
    )
    val scaleY by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "stretchY"
    )
    
    val pressDepth by animateDpAsState(
        targetValue = if (isPressed) buttonSize * 0.04f else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "pressDepth"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            },
        contentAlignment = Alignment.Center
    ) {
        // Particle Burst Layer
        ParticleBurst(
            trigger = triggerBurst,
            onFinish = { triggerBurst = false }
        )

        // 1. External Luminous Glow
        Box(
            modifier = Modifier
                .size(buttonSize * 0.85f)
                .blur(32.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.35f), Color.Transparent),
                    ),
                    shape = CircleShape
                )
        )

        // 2. Button Side/Depth (3D Base)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = buttonSize * 0.08f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF8B1A1A), Color(0xFF4A0808))
                    )
                )
        )

        // 3. Main Interactive Surface
        Box(
            modifier = Modifier
                .size(buttonSize)
                .offset(y = pressDepth)
                .shadow(
                    elevation = if (isPressed) 4.dp else 24.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black,
                    spotColor = Primary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFF7E7E), // Peak light
                            Color(0xFFEF4444), // Main color
                            Color(0xFF991B1B)  // Depth shade
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
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
            // 4. Glassy Specular Highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawOval(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.45f
                            ),
                            size = size
                        )
                    }
            )

            // 5. Bevel Border (Inner Light)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(5.dp, Color.White.copy(alpha = 0.05f), CircleShape)
            )

            // Content
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

