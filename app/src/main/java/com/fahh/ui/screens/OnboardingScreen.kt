package com.fahh.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fahh.R
import com.fahh.ui.theme.Background
import kotlinx.coroutines.launch

private val onboardingImages = listOf(
    R.drawable.onboard_wasting_time,
    R.drawable.onboard_explain,
    R.drawable.onboard_button,
    R.drawable.onboard_points,
    R.drawable.onboard_update_new,
    R.drawable.onboard_update_custom
)

private val onboardingDescriptions = listOf(
    "Welcome to Fahh, a fast meme sound reaction camera.",
    "Record a reaction while Fahh plays the selected sound through your speaker.",
    "Press the large red button to play the selected reaction sound instantly.",
    "Rapid presses unlock playful local combo titles and add to your press count.",
    "Browse the new sound drop and watch one optional rewarded ad to unlock a named sound on this device.",
    "Record private custom sounds locally, with one optional rewarded ad for each saved slot."
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingImages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingImages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = onboardingImages[page]),
                    contentDescription = onboardingDescriptions[page],
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
        }

        // Bottom bar: dots + button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            // Page dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingImages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 28.dp else 6.dp)
                            .background(
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TactileOnboardingButton(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                text = if (isLastPage) "LET'S GO" else "Next",
                isPrimary = isLastPage
            )
        }
    }
}

@Composable
internal fun TactileOnboardingButton(onClick: () -> Unit, text: String, isPrimary: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressDepth by animateDpAsState(
        targetValue = if (pressed) 9.dp else 0.dp,
        animationSpec = tween(durationMillis = if (pressed) 55 else 130),
        label = "onboardingPressDepth"
    )
    val faceColor = if (isPrimary) Color(0xFFF05252) else Color(0xFF313B4A)
    val baseColor = if (isPrimary) Color(0xFF8E252B) else Color(0xFF141C27)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(baseColor, RoundedCornerShape(16.dp))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = pressDepth)
                .background(faceColor, RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}
