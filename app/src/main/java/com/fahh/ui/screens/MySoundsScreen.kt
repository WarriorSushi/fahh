package com.fahh.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val selectedSound by soundViewModel.selectedSound.collectAsState()
    val customSounds = sounds.filter { it.filePath != null }
    var name by remember { mutableStateOf("My Fahh") }
    var secondsRemaining by remember { mutableIntStateOf(0) }
    var pendingDelete by remember { mutableStateOf<Sound?>(null) }
    var lastSavedId by remember { mutableStateOf<String?>(null) }
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
            secondsRemaining = 8
            repeat(8) {
                delay(1_000)
                secondsRemaining = 7 - it
            }
            soundViewModel.stopCustomSoundRecording(name).onSuccess {
                soundViewModel.selectSound(it)
                lastSavedId = it.id
                name = "My Fahh"
            }.onFailure { error = it.message }
        } else {
            secondsRemaining = 0
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

    fun saveRecording() {
        soundViewModel.stopCustomSoundRecording(name).onSuccess {
            soundViewModel.selectSound(it)
            lastSavedId = it.id
            name = "My Fahh"
        }.onFailure { error = it.message }
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
            Text("Make your own sounds", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Record a sound, name it, then use it with the Fahh button. Sounds stay on this device.", color = Color.White.copy(alpha = 0.62f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(16.dp))

            Surface(color = SurfaceHigh, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("${customSounds.size} saved sounds · $customSoundSlots slots unlocked forever", color = Primary, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (customSounds.size < customSoundSlots) "You have a sound slot ready to use." else "Watch one optional ad to unlock another sound slot forever.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(24) },
                label = { Text("Name this sound") },
                supportingText = { Text("This name appears on the Fahh button") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (recording) saveRecording()
                    else if (customSounds.size >= customSoundSlots) unlockSlotWithReward()
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                enabled = recording || rewardedAd != null || !loadingAd || customSounds.size < customSoundSlots,
                colors = ButtonDefaults.buttonColors(containerColor = if (recording) Color(0xFFE53935) else Primary),
                modifier = Modifier.fillMaxWidth().height(58.dp)
            ) {
                Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (recording) "Stop and save (${secondsRemaining}s)"
                    else if (customSounds.size >= customSoundSlots) if (loadingAd) "Loading ad…" else "Watch 1 ad to add a slot"
                    else "Record up to 8 seconds",
                    fontWeight = FontWeight.Bold
                )
            }
            if (recording) {
                LinearProgressIndicator(
                    progress = secondsRemaining / 8f,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    color = Primary,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                OutlinedButton(
                    onClick = { soundViewModel.cancelCustomSoundRecording() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Discard recording") }
            }
            Spacer(Modifier.height(18.dp))
            Text("YOUR SOUNDS", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            if (customSounds.isEmpty()) {
                Text("Your saved sounds appear here. Record one, then tap Use on button.", color = Color.White.copy(alpha = 0.52f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(customSounds, key = { it.id }) { sound ->
                        CustomSoundRow(
                            sound = sound,
                            isSelected = selectedSound.id == sound.id,
                            isNewlySaved = lastSavedId == sound.id,
                            onPreview = { soundViewModel.playSoundPreview(sound) },
                            onSelect = { soundViewModel.selectSound(sound) },
                            onDelete = { pendingDelete = sound }
                        )
                    }
                }
            }
            error?.let { Text(it, color = Color(0xFFFF8A80), modifier = Modifier.padding(top = 12.dp)) }
        }
    }

    pendingDelete?.let { sound ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${sound.name}?") },
            text = { Text("This removes the private recording from this device and frees its slot.") },
            confirmButton = { TextButton(onClick = { soundViewModel.deleteCustomSound(sound.id); pendingDelete = null }) { Text("Delete", color = Color(0xFFFF8A80)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Keep it") } }
        )
    }
}

@Composable
private fun CustomSoundRow(sound: Sound, isSelected: Boolean, isNewlySaved: Boolean, onPreview: () -> Unit, onSelect: () -> Unit, onDelete: () -> Unit) {
    Surface(color = if (isSelected) Primary.copy(alpha = 0.18f) else SurfaceHigh, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✦", color = Primary, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(sound.name, color = Color.White, fontWeight = FontWeight.Bold)
                    if (isSelected || isNewlySaved) Text(if (isSelected) "Using on button" else "Saved and ready", color = Primary, fontSize = 11.sp)
                }
                IconButton(onClick = onPreview) { Icon(Icons.Default.PlayArrow, "Preview ${sound.name}", tint = Color.White) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete ${sound.name}", tint = Color.White.copy(alpha = 0.7f)) }
            }
            Button(
                onClick = onSelect,
                enabled = !isSelected,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White.copy(alpha = 0.12f) else Primary)
            ) { Text(if (isSelected) "Using on button" else "Use on button") }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.findActivity(); else -> null }
