package com.fahh.ui.screens

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.R
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.Primary
import com.fahh.utils.AdManager
import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.delay

private val warmMessages = listOf(
    "Quick ad break — this keeps Fahh free for everyone.",
    "You're on fire. Quick ad, then back to creating.",
    "Fahh is free because of this moment.\nThanks for supporting us.",
    "Ad incoming — blame capitalism, not us."
)

private const val InterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712" // TEST ID

@Composable
fun AdTransitionScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val message = remember { warmMessages.random() }

    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2800),
        label = "progress"
    )

    var adLoaded by remember { mutableStateOf<InterstitialAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }

    // Load the interstitial ad immediately
    LaunchedEffect(Unit) {
        AdManager.loadInterstitialAd(
            context = context,
            adUnitId = InterstitialAdUnitId,
            onAdLoaded = { adLoaded = it },
            onAdFailed = { adFailed = true }
        )
    }

    // Progress bar + auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        progress = 1f
        delay(3000)

        val ad = adLoaded
        if (ad != null && activity != null) {
            AdManager.showInterstitialAd(
                activity = activity,
                interstitialAd = ad,
                onDismissed = { onFinished() },
                onShowFailed = { onFinished() }
            )
        } else {
            // Ad didn't load — skip, don't block the user
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.fahh_logo),
                contentDescription = "Fahh logo",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Warm message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Primary,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
        }
    }
}
