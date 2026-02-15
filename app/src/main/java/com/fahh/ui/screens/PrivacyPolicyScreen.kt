package com.fahh.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.premiumGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy & Terms",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .premiumGlass(CircleShape, alpha = 0.08f)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PrivacySection(
                title = "Data Collection",
                content = "Fahh does not collect, store, or transmit any personal data. All your videos, sound preferences, and settings stay on your device. We have zero servers — your data is yours."
            )
            PrivacySection(
                title = "Permissions",
                content = "Camera & Microphone: Used only during recording to capture video with sound effects. Storage: Used to save recorded videos to your gallery. No data is uploaded anywhere."
            )
            PrivacySection(
                title = "Advertisements",
                content = "We use Google AdMob to show occasional ads that help keep the app free. AdMob may collect device identifiers and usage data for ad personalization. You can opt out of personalized ads in your device settings."
            )
            PrivacySection(
                title = "Sound Unlocks",
                content = "Watching a rewarded ad unlocks sounds permanently on your device. This preference is stored locally and never shared."
            )
            PrivacySection(
                title = "Children's Privacy",
                content = "Fahh is not directed at children under 13. We do not knowingly collect data from children."
            )
            PrivacySection(
                title = "Contact",
                content = "Questions? Reach us at the privacy policy page:\nhttps://tracker.dog/fahh-privay-policy/"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Last updated: Feb 2026",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
                lineHeight = 20.sp
            )
        }
    }
}
