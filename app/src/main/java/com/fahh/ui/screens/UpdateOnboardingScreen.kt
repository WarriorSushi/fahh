package com.fahh.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fahh.R
import com.fahh.ui.theme.Background
import kotlinx.coroutines.launch

private val updateImages = listOf(R.drawable.onboard_update_new, R.drawable.onboard_update_custom)
private val updateDescriptions = listOf(
    "New sound drop: browse more reactions and unlock a named sound with an optional rewarded ad.",
    "Custom sounds: record short private clips and keep them locally in rewarded slots."
)

/** A one-time announcement for existing users after they install this app update. */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun UpdateOnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { updateImages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == updateImages.lastIndex

    Box(Modifier.fillMaxSize().background(Background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Image(
                painter = painterResource(updateImages[page]),
                contentDescription = updateDescriptions[page],
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(updateImages.size) { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (pagerState.currentPage == index) 28.dp else 6.dp)
                            .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            TactileOnboardingButton(
                onClick = {
                    if (isLastPage) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                text = if (isLastPage) "LET'S GO" else "Next",
                isPrimary = isLastPage
            )
        }
    }
}
