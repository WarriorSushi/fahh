package com.fahh.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.BuildConfig
import com.fahh.data.model.Sound
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.Primary
import com.fahh.ui.theme.SurfaceHigh
import com.fahh.utils.AdManager
import com.fahh.utils.ConsentManager
import com.fahh.viewmodel.SoundViewModel
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.delay

private const val TestRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"
private const val ProductionRewardedAdUnitId = "ca-app-pub-1006057089920582/1635547968"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySoundsScreen(onBack: () -> Unit, soundViewModel: SoundViewModel) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val customSoundSlots by soundViewModel.customSoundSlots.collectAsState()
    val recording by soundViewModel.isCustomRecording.collectAsState()
    val sounds by soundViewModel.allSounds.collectAsState()
    val customSounds = sounds.filter { it.filePath != null }
    var name by remember { mutableStateOf("My Fahh") }
    var error by remember { mutableStateOf<String?>(null) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var loadingAd by remember { mutableStateOf(false) }

    fun loadRewardedAd() {
        if (loadingAd || rewardedAd != null || !ConsentManager.canRequestAds(context)) return
        loadingAd = true
        AdManager.loadRewardedAd(
            context,
            if (BuildConfig.DEBUG) TestRewardedAdUnitId else ProductionRewardedAdUnitId,
            onAdLoaded = { rewardedAd = it; loadingAd = false },
            onAdFailed = { loadingAd = false; error = "Ad unavailable. Try again in a moment." }
        )
    }

    LaunchedEffect(Unit) { loadRewardedAd() }
    LaunchedEffect(recording) {
        if (recording) {
            delay(8_000)
            soundViewModel.stopCustomSoundRecording(name).onSuccess { soundViewModel.selectSound(it) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) soundViewModel.startCustomSoundRecording().onFailure { error = it.message }
        else error = "Microphone permission is needed to record a sound."
    }

    fun leaveScreen() {
        if (recording) soundViewModel.cancelCustomSoundRecording()
        onBack()
    }

    BackHandler { leaveScreen() }

    fun unlockSlotWithReward() {
        val ad = rewardedAd
        if (activity == null) error = "Could not open the ad from this screen."
        else if (ad == null) loadRewardedAd()
        else {
            rewardedAd = null
            AdManager.showRewardedAd(
                activity,
                ad,
                onRewardEarned = { soundViewModel.unlockNextCustomSoundSlot() },
                onDismissed = { loadRewardedAd() },
                onShowFailed = { error = "Could not show the ad."; loadRewardedAd() }
            )
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Custom sounds", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = ::leaveScreen) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("Your private reaction vault. Clips stay on this device.", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
            Spacer(Modifier.height(18.dp))
            Text("${customSounds.size} saved · $customSoundSlots unlocked slots", color = Primary, fontWeight = FontWeight.Bold)
            Text("Add another slot whenever you want. There is no five-sound cap.", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(value = name, onValueChange = { name = it.take(24) }, label = { Text("Sound name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (recording) {
                            soundViewModel.stopCustomSoundRecording(name).onSuccess { soundViewModel.selectSound(it) }.onFailure { error = it.message }
                        } else if (customSounds.size >= customSoundSlots) {
                            unlockSlotWithReward()
                        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    enabled = recording || rewardedAd != null || !loadingAd || customSounds.size < customSoundSlots,
                    colors = ButtonDefaults.buttonColors(containerColor = if (recording) Color(0xFFE53935) else Primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text(if (recording) "Stop and save" else if (customSounds.size >= customSoundSlots) if (loadingAd) "Loading ad…" else "Watch 1 ad for a sound slot" else "Record up to 8 seconds") }
                if (recording) Text("Recording… tap stop when the chaos is perfect.", color = Primary, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(18.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(customSounds, key = { it.id }) { sound -> CustomSoundRow(sound, { soundViewModel.playSoundPreview(sound) }, { soundViewModel.selectSound(sound) }, { soundViewModel.deleteCustomSound(sound.id) }) }
                }
            error?.let { Text(it, color = Color(0xFFFF8A80), modifier = Modifier.padding(top = 12.dp)) }
        }
    }
}

@Composable
private fun CustomSoundRow(sound: Sound, onPreview: () -> Unit, onSelect: () -> Unit, onDelete: () -> Unit) {
    Surface(onClick = onSelect, color = SurfaceHigh, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = Primary, fontSize = 20.sp); Spacer(Modifier.width(10.dp)); Text(sound.name, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            IconButton(onClick = onPreview) { Icon(Icons.Default.PlayArrow, "Preview ${sound.name}", tint = Color.White) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete ${sound.name}", tint = Color.White.copy(alpha = 0.7f)) }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.findActivity(); else -> null }
