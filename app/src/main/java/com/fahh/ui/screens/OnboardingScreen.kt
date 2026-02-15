package com.fahh.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.premiumGlass
import kotlinx.coroutines.launch

private data class OnboardingStep(
    val title: String,
    val line: String,
    val hint: String,
    val icon: ImageVector,
    val accent: Color
)

private val onboardingSteps = listOf(
    OnboardingStep(
        title = "Tap. Boom.",
        line = "One press, instant sound.",
        hint = "Built for real-time reaction moments.",
        icon = Icons.Default.PlayArrow,
        accent = Color(0xFFFF9C89)
    ),
    OnboardingStep(
        title = "Record & React",
        line = "Camera mode + live sound trigger.",
        hint = "Capture pranks and crowd reactions.",
        icon = Icons.Default.CameraAlt,
        accent = Color(0xFF7FB9FF)
    ),
    OnboardingStep(
        title = "Unlock Magic",
        line = "Preview premium sounds twice.",
        hint = "Unlock packs whenever you need fresh gold.",
        icon = Icons.Default.LockOpen,
        accent = Color(0xFFFFC778)
    ),
    OnboardingStep(
        title = "Full Library",
        line = "Swipe right for the full sound vault.",
        hint = "Set your favorite sound and go live.",
        icon = Icons.Default.AutoAwesome,
        accent = Color(0xFF6DE4BC)
    )
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingSteps.size })
    val scope = rememberCoroutineScope()
    val currentStep = onboardingSteps[pagerState.currentPage]

    val animatedAccent by animateColorAsState(
        targetValue = currentStep.accent,
        animationSpec = tween(600),
        label = "AccentTransition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Dynamic Luminous Glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(450.dp)
                .blur(100.dp)
                .graphicsLayer { alpha = 0.15f }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(animatedAccent, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "fahh",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                if (pagerState.currentPage < onboardingSteps.lastIndex) {
                    TextButton(onClick = onFinish) {
                        Text("Skip", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Main Content Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp),
                pageSpacing = 24.dp
            ) { page ->
                FunStepCard(step = onboardingSteps[page])
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Animated Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(onboardingSteps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 32.dp else 6.dp)
                                .background(
                                    color = if (isSelected) animatedAccent else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage == onboardingSteps.lastIndex) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pagerState.currentPage == onboardingSteps.lastIndex) 
                            animatedAccent else Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == onboardingSteps.lastIndex) "Get Started" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun FunStepCard(step: OnboardingStep) {
    val infiniteTransition = rememberInfiniteTransition(label = "Floating")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "IconFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .premiumGlass(RoundedCornerShape(32.dp), alpha = 0.04f)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = step.accent.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { translationY = floatAnim }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = step.accent,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = step.line,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = step.hint,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
