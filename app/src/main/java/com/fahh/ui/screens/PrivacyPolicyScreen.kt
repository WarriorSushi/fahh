package com.fahh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Privacy Policy for Fahh",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fahh is committed to protecting your privacy. This policy explains how we handle your information.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrivacySection(title = "1. Data Collection", content = "We do not collect any personal data. All videos and settings are stored locally on your device.")
            PrivacySection(title = "2. Permissions", content = "Fahh requires Camera and Microphone access only to record videos with sound effects.")
            PrivacySection(title = "3. Advertisements", content = "We use Google AdMob for advertisements. AdMob may collect and use pseudonymous data for ad personalization.")
            PrivacySection(title = "4. Third-Party Links", content = "Our app may contain links to social media (TikTok, Instagram) for video sharing.")
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Last updated: Feb 2026",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.White)
        Text(text = content, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
    }
}
