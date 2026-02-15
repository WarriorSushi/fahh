package com.fahh.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    var shouldLoadAd by remember { mutableStateOf(false) }
    var hasLoadedAd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        shouldLoadAd = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ads keep Fahh free. Thanks for supporting the app.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                }
            },
            update = { adView ->
                if (shouldLoadAd && !hasLoadedAd) {
                    adView.loadAd(AdRequest.Builder().build())
                    hasLoadedAd = true
                }
            }
        )
    }
}
