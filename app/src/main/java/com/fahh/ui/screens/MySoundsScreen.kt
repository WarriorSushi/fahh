package com.fahh.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.MediaMetadataRetriever
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import java.io.File
import kotlin.math.max

private const val TestRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"
private const val ProductionRewardedAdUnitId = "ca-app-pub-1006057089920582/1635547968"
private const val RecordingLimitSeconds = 5

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
    var draftName by remember { mutableStateOf("My Fahh") }
    var secondsRemaining by remember { mutableIntStateOf(0) }
    var pendingDelete by remember { mutableStateOf<Sound?>(null) }
    var editingSound by remember { mutableStateOf<Sound?>(null) }
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

    fun finishRecording() {
        soundViewModel.stopCustomSoundRecording(draftName).onSuccess {
            soundViewModel.selectSound(it)
            lastSavedId = it.id
            draftName = "My Fahh"
        }.onFailure { error = it.message }
    }

    LaunchedEffect(Unit) { loadRewardedAd() }
    LaunchedEffect(recording) {
        if (!recording) {
            secondsRemaining = 0
            return@LaunchedEffect
        }
        secondsRemaining = RecordingLimitSeconds
        repeat(RecordingLimitSeconds) { tick ->
            delay(1_000)
            secondsRemaining = RecordingLimitSeconds - tick - 1
        }
        if (recording) finishRecording()
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) soundViewModel.startCustomSoundRecording().onFailure { error = it.message }
        else error = "Microphone permission is needed to record a sound."
    }

    fun unlockSlotWithReward() {
        val ad = rewardedAd
        when {
            activity == null -> error = "Could not open the ad from this screen."
            ad == null -> loadRewardedAd()
            else -> {
                rewardedAd = null
                AdManager.showRewardedAd(
                    activity, ad,
                    onRewardEarned = { soundViewModel.unlockNextCustomSoundSlot() },
                    onDismissed = { loadRewardedAd() },
                    onShowFailed = { error = "Could not show the ad."; loadRewardedAd() }
                )
            }
        }
    }

    fun leaveScreen() {
        if (recording) soundViewModel.cancelCustomSoundRecording()
        onBack()
    }
    BackHandler { leaveScreen() }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Custom sounds", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = ::leaveScreen) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Record your own sound", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("Name it, record up to 5 seconds, and put it on your Fahh button.", color = Color.White.copy(alpha = 0.58f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    Text("Your saved recordings also appear in Unlocked sounds with a mic icon.", color = Primary.copy(alpha = 0.88f), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }

            items(customSounds, key = { it.id }) { sound ->
                CustomSoundCard(
                    sound = sound,
                    isSelected = selectedSound.id == sound.id,
                    isNewlySaved = lastSavedId == sound.id,
                    onPreview = { soundViewModel.playSoundPreview(sound) },
                    onUse = { soundViewModel.selectSound(sound) },
                    onEdit = { editingSound = sound },
                    onDelete = { pendingDelete = sound }
                )
            }

            if (customSounds.size < customSoundSlots) {
                item("ready-slot") {
                    ReadyToRecordCard(
                        name = draftName,
                        onNameChange = { draftName = it.take(24) },
                        recording = recording,
                        secondsRemaining = secondsRemaining,
                        onRecord = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onStop = ::finishRecording,
                        onDiscard = { soundViewModel.cancelCustomSoundRecording() }
                    )
                }
            } else {
                item("locked-slot") {
                    LockedSlotCard(
                        loading = loadingAd,
                        onUnlock = ::unlockSlotWithReward
                    )
                }
            }

            item("add-slot") {
                GhostAddCard(onClick = ::unlockSlotWithReward)
            }
            error?.let { message ->
                item("error") { Text(message, color = Color(0xFFFF8A80), fontSize = 12.sp) }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }

    pendingDelete?.let { sound ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${sound.name}?") },
            text = { Text("This removes the recording from this device and frees this slot.") },
            confirmButton = { TextButton(onClick = { soundViewModel.deleteCustomSound(sound.id); pendingDelete = null }) { Text("Delete", color = Color(0xFFFF8A80)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Keep it") } }
        )
    }
    editingSound?.let { sound ->
        EditSoundSheet(
            sound = sound,
            onDismiss = { editingSound = null },
            onSave = { name, startMs, endMs ->
                soundViewModel.editCustomSound(sound.id, name, startMs, endMs)
                    .onFailure { error = it.message }
                editingSound = null
            }
        )
    }
}

@Composable
private fun ReadyToRecordCard(
    name: String,
    onNameChange: (String) -> Unit,
    recording: Boolean,
    secondsRemaining: Int,
    onRecord: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    SlotSurface(accent = Primary) {
        Text("Your next sound", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("This slot is ready to record.", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Sound name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
        if (recording) {
            RecordingMeter(secondsRemaining)
        }
        Button(
            onClick = if (recording) onStop else onRecord,
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (recording) Color(0xFFE53E3E) else Primary)
        ) {
            Icon(if (recording) Icons.Default.Stop else Icons.Default.Mic, null)
            Spacer(Modifier.width(8.dp))
            Text(if (recording) "Stop and save" else "Record sound", fontWeight = FontWeight.Bold)
        }
        if (recording) {
            TextButton(onClick = onDiscard, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Discard recording") }
        }
    }
}

@Composable
private fun LockedSlotCard(loading: Boolean, onUnlock: () -> Unit) {
    SlotSurface(accent = Color.White.copy(alpha = 0.16f)) {
        Text("Add a sound slot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Give your next custom sound a permanent place.", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Button(
            onClick = onUnlock,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 12.dp)
        ) { Text(if (loading) "Loading ad…" else "Watch 1 ad to unlock", fontWeight = FontWeight.Bold) }
        Text("Unlock this slot forever on this device", color = Color.White.copy(alpha = 0.48f), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 5.dp))
    }
}

@Composable
private fun GhostAddCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.035f)).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(7.dp))
        Text("Add another sound slot", color = Color.White.copy(alpha = 0.76f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun SlotSurface(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = SurfaceHigh, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(accent))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RecordingMeter(secondsRemaining: Int) {
    val progress = (RecordingLimitSeconds - secondsRemaining) / RecordingLimitSeconds.toFloat()
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawArc(Color.White.copy(alpha = 0.12f), -90f, 360f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                drawArc(Primary, -90f, progress * 360f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(5.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("${max(secondsRemaining, 0)}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Recording", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            SoundWaveform(active = true, modifier = Modifier.fillMaxWidth().height(30.dp).padding(top = 4.dp))
        }
    }
}

@Composable
private fun CustomSoundCard(sound: Sound, isSelected: Boolean, isNewlySaved: Boolean, onPreview: () -> Unit, onUse: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(color = if (isSelected) Primary.copy(alpha = 0.16f) else SurfaceHigh, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Primary.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Mic, null, tint = Primary, modifier = Modifier.size(19.dp)) }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(sound.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (isSelected) "Using on button" else if (isNewlySaved) "Saved and ready" else "Custom sound", color = if (isSelected) Primary else Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
                IconButton(onClick = onPreview) { Icon(Icons.Default.PlayArrow, "Preview ${sound.name}", tint = Color.White) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit ${sound.name}", tint = Color.White.copy(alpha = 0.75f)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete ${sound.name}", tint = Color.White.copy(alpha = 0.65f)) }
            }
            SoundWaveform(active = false, modifier = Modifier.fillMaxWidth().height(28.dp).padding(top = 8.dp))
            Button(onClick = onUse, enabled = !isSelected, modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 8.dp)) {
                Text(if (isSelected) "Using on button" else "Use on button", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SoundWaveform(active: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val bars = 36
        val gap = size.width / (bars * 2f)
        repeat(bars) { index ->
            val heightFraction = 0.23f + ((index * 13 % 17) / 20f)
            val height = size.height * heightFraction
            val x = gap + index * gap * 2
            drawLine(
                color = if (active) Primary else Color.White.copy(alpha = 0.26f),
                start = androidx.compose.ui.geometry.Offset(x, (size.height - height) / 2f),
                end = androidx.compose.ui.geometry.Offset(x, (size.height + height) / 2f),
                strokeWidth = gap.coerceAtMost(4.dp.toPx()),
                cap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSoundSheet(sound: Sound, onDismiss: () -> Unit, onSave: (String, Long, Long) -> Unit) {
    val durationMs = remember(sound.filePath) { readAudioDurationMs(sound.filePath) }
    var name by remember(sound.id) { mutableStateOf(sound.name) }
    var range by remember(sound.id, durationMs) { mutableStateOf(0f..durationMs.toFloat()) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceHigh) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 30.dp)) {
            Text("Edit custom sound", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Drag the handles to keep the best part. The saved sound stays on your device.", color = Color.White.copy(alpha = 0.56f), fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
            OutlinedTextField(value = name, onValueChange = { name = it.take(24) }, label = { Text("Sound name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
            Text("Keep ${formatAudioTime(range.start)} – ${formatAudioTime(range.endInclusive)}", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            SoundWaveform(active = false, modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 6.dp))
            RangeSlider(
                value = range,
                onValueChange = { selected ->
                    if (selected.endInclusive - selected.start >= 250f) range = selected
                },
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSave(name, range.start.toLong(), range.endInclusive.toLong()) },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(50.dp)
            ) { Text("Save changes", fontWeight = FontWeight.Bold) }
        }
    }
}

private fun readAudioDurationMs(path: String?): Long = runCatching {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    val result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 5_000L
    retriever.release()
    result.coerceAtLeast(250L)
}.getOrDefault(5_000L)

private fun formatAudioTime(valueMs: Float): String = "%.1fs".format(valueMs / 1_000f)

private fun Context.findActivity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.findActivity(); else -> null }
