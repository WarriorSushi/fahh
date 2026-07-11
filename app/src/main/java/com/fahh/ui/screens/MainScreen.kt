package com.fahh.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import com.fahh.R
import com.fahh.BuildConfig
import com.fahh.data.catalog.SoundCatalog
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahh.ui.theme.Primary
import com.fahh.ui.theme.Background
import com.fahh.ui.theme.premiumGlass
import com.fahh.data.model.Sound
import com.fahh.ui.components.ConfettiCelebration
import com.fahh.ui.components.SettingsSheet
import com.fahh.ui.components.SidebarMenu
import com.fahh.ui.components.SoundButton
import com.fahh.utils.AdManager
import com.fahh.utils.ConsentManager
import com.fahh.utils.ShareUtils
import com.fahh.viewmodel.SoundViewModel
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TestRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"
private const val ProductionRewardedAdUnitId = "ca-app-pub-1006057089920582/1635547968"

private data class SidebarNotice(
    val token: Long,
    val message: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    onCameraClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onComingSoonClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onMySoundsClick: () -> Unit,
    showAdPrivacyOptions: Boolean,
    onAdPrivacyClick: () -> Unit,
    viewModel: SoundViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val newSoundsDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sounds by viewModel.allSounds.collectAsState()
    val newSoundIds = remember { SoundCatalog.sounds.drop(12).map { it.id }.toSet() }
    val newSounds = sounds.filter { it.id in newSoundIds }
    val rightDrawerSounds = sounds.filter { it.id !in newSoundIds }
    val selectedSound by viewModel.selectedSound.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val totalFahhCount by viewModel.totalFahhCount.collectAsState()
    val soundPressCounts by viewModel.soundPressCounts.collectAsState()
    val highestComboTier by viewModel.highestComboTier.collectAsState()
    val newSoundsSeen by viewModel.newSoundsSeen.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val walkthroughDone by viewModel.walkthroughDone.collectAsState()
    // Walkthrough steps: 0 = "press the button", 1 = "feels good" meme, 2 = "more sounds →", -1 = done
    var walkthroughStep by remember { mutableStateOf(-1) }

    var showSettings by remember { mutableStateOf(false) }
    var showTipJarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(walkthroughDone) {
        if (!walkthroughDone) {
            walkthroughStep = 0
        }
    }

    var soundToUnlock by remember { mutableStateOf<Sound?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }
    var adErrorText by remember { mutableStateOf<String?>(null) }
    var sidebarNotice by remember { mutableStateOf<SidebarNotice?>(null) }
    val lockedPreviewCounts = remember { mutableStateMapOf<Int, Int>() }

    fun showSidebarNotice(message: String) {
        sidebarNotice = SidebarNotice(token = System.nanoTime(), message = message)
    }

    fun clearSidebarNotice() {
        sidebarNotice = null
    }

    fun loadRewardedAd() {
        if (isRewardedAdLoading || rewardedAd != null) return
        if (!ConsentManager.canRequestAds(context)) {
            adErrorText = "Ad privacy setup is not ready yet. Close this and try again shortly."
            return
        }
        isRewardedAdLoading = true
        AdManager.loadRewardedAd(
            context = context,
            adUnitId = if (BuildConfig.DEBUG) TestRewardedAdUnitId else ProductionRewardedAdUnitId,
            onAdLoaded = { ad ->
                rewardedAd = ad
                isRewardedAdLoading = false
                adErrorText = null
            },
            onAdFailed = {
                isRewardedAdLoading = false
                adErrorText = "Reward ad is unavailable right now. Try again."
            }
        )
    }

    BackHandler(enabled = drawerState.isOpen || newSoundsDrawerState.isOpen) {
        if (showSettings) {
            showSettings = false
        } else if (newSoundsDrawerState.isOpen) {
            scope.launch { newSoundsDrawerState.close() }
        } else {
            scope.launch { drawerState.close() }
        }
    }

    // Reset settings panel when drawer closes
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) showSettings = false
    }

    LaunchedEffect(Unit) { loadRewardedAd() }

    LaunchedEffect(sidebarNotice?.token) {
        if (sidebarNotice != null) {
            delay(3000)
            sidebarNotice = null
        }
    }

    soundToUnlock?.let { sound ->
        AlertDialog(
            onDismissRequest = { soundToUnlock = null },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("Unlock ${sound.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Watch one short ad to unlock this sound permanently on this device.")
                    adErrorText?.let { message ->
                        Text(
                            message,
                            color = Color(0xFFFFB4AB),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = rewardedAd != null || !isRewardedAdLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    onClick = {
                        val ad = rewardedAd
                        if (activity == null) {
                            adErrorText = "Unable to start ad from this screen."
                            return@Button
                        }

                        if (ad == null) {
                            loadRewardedAd()
                            return@Button
                        }

                        rewardedAd = null
                        AdManager.showRewardedAd(
                            activity = activity,
                            rewardedAd = ad,
                            onRewardEarned = {
                                viewModel.unlockSound(sound.id)
                                viewModel.selectSound(sound.copy(isLocked = false))
                                showConfetti = true
                                clearSidebarNotice()
                                soundToUnlock = null
                            },
                            onDismissed = {
                                soundToUnlock = null
                                loadRewardedAd()
                            },
                            onShowFailed = {
                                adErrorText = "Could not show ad. Please try again."
                                soundToUnlock = null
                                loadRewardedAd()
                            }
                        )
                    }
                ) {
                    if (isRewardedAdLoading && rewardedAd == null) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Watch Ad", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { soundToUnlock = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    ConfettiCelebration(trigger = showConfetti, onFinish = { showConfetti = false })

    // Tip Jar dialog
    if (showTipJarDialog) {
        AlertDialog(
            onDismissRequest = { showTipJarDialog = false },
            containerColor = Color(0xFF161B22),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("You're the best \uD83E\uDEF6", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your support keeps the memes alive and the lights on. Every coffee helps a small indie dev keep building weird, wonderful apps.\n\nTapping below will open Buy Me a Coffee \u2014 no pressure, just vibes."
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    onClick = {
                        showTipJarDialog = false
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/warriorsushi"))
                        )
                    }
                ) {
                    Text("Buy Me a Coffee \u2615", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTipJarDialog = false }) {
                    Text("Maybe later", color = Color.White.copy(alpha = 0.35f))
                }
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = newSoundsDrawerState,
            drawerContent = {
                SidebarMenu(
                    sounds = newSounds,
                    selectedSound = selectedSound,
                    volume = volume,
                    onVolumeChange = { viewModel.updateVolume(it) },
                    onSoundPreview = { sound -> viewModel.playSoundPreview(sound) },
                    onSoundSelected = { sound ->
                        if (sound.isLocked) soundToUnlock = sound
                        else {
                            viewModel.selectSound(sound)
                            scope.launch { newSoundsDrawerState.close() }
                        }
                    },
                    noticeMessage = null,
                    onDismissNotice = {},
                    onClose = { scope.launch { newSoundsDrawerState.close() } },
                    onPrivacyClick = {},
                    soundPressCounts = soundPressCounts,
                    title = "New sounds",
                    subtitle = "Watch 1 ad to unlock a sound forever",
                    showMoreSoundsAction = false,
                    showUtilityDock = false
                )
            }
        ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AnimatedContent(
                        targetState = showSettings,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "SidebarContent"
                    ) { settingsVisible ->
                        if (settingsVisible) {
                            SettingsSheet(
                                highestComboTier = highestComboTier,
                                totalPresses = totalFahhCount,
                                sounds = sounds,
                                soundPressCounts = soundPressCounts,
                                onShareText = { text -> ShareUtils.shareText(context, text, "Share Fahh stats") },
                                onPrivacyClick = {
                                    scope.launch { drawerState.close() }
                                    showSettings = false
                                    onPrivacyClick()
                                },
                                onComingSoonClick = {
                                    scope.launch { drawerState.close() }
                                    showSettings = false
                                    onComingSoonClick()
                                },
                                showAdPrivacyOptions = showAdPrivacyOptions,
                                onAdPrivacyClick = onAdPrivacyClick,
                                onBack = { showSettings = false }
                            )
                        } else {
                            SidebarMenu(
                                sounds = rightDrawerSounds,
                                selectedSound = selectedSound,
                                volume = volume,
                                onVolumeChange = { viewModel.updateVolume(it) },
                                onSoundPreview = { sound ->
                                    if (!sound.isLocked) {
                                        clearSidebarNotice()
                                        viewModel.playSoundPreview(sound)
                                    } else {
                                        val usedPreviews = lockedPreviewCounts[sound.resId] ?: 0
                                        if (usedPreviews < 2) {
                                            lockedPreviewCounts[sound.resId] = usedPreviews + 1
                                            clearSidebarNotice()
                                            viewModel.playSoundPreview(sound)
                                        } else {
                                            showSidebarNotice("Previews finished for ${sound.name}. Watch an ad to unlock.")
                                        }
                                    }
                                },
                                onSoundSelected = { sound ->
                                    if (sound.isLocked) {
                                        val usedPreviews = lockedPreviewCounts[sound.resId] ?: 0
                                        if (usedPreviews >= 2) {
                                            showSidebarNotice("Watch an ad to unlock ${sound.name}.")
                                        }
                                        soundToUnlock = sound
                                    } else {
                                        clearSidebarNotice()
                                        viewModel.selectSound(sound)
                                        scope.launch { drawerState.close() }
                                    }
                                },
                                noticeMessage = sidebarNotice?.message,
                                onDismissNotice = { clearSidebarNotice() },
                                onClose = { scope.launch { drawerState.close() } },
                                onPrivacyClick = {
                                    scope.launch { drawerState.close() }
                                    onPrivacyClick()
                                },
                                soundPressCounts = soundPressCounts,
                                onMySoundsClick = {
                                    scope.launch { drawerState.close() }
                                    onMySoundsClick()
                                },
                                onSettingsClick = { showSettings = true },
                                onTipJarClick = { showTipJarDialog = true },
                                title = "Unlocked sounds",
                                subtitle = "Your collection and original reactions",
                                onOpenMoreSounds = {
                                    viewModel.markNewSoundsSeen()
                                    scope.launch {
                                        drawerState.close()
                                        delay(80)
                                        newSoundsDrawerState.open()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MainContent(
                    selectedSound = selectedSound,
                    streak = streak,
                    totalFahhCount = totalFahhCount,
                    highestComboTier = highestComboTier,
                    onComboTierUnlocked = { tier -> viewModel.updateHighestComboTier(tier) },
                    onPlayClick = {
                        clearSidebarNotice()
                        viewModel.playSelectedSound()
                        if (walkthroughStep == 0) walkthroughStep = 1
                    },
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNewSoundsClick = {
                        viewModel.markNewSoundsSeen()
                        scope.launch { newSoundsDrawerState.open() }
                    },
                    showNewSoundsPrompt = !newSoundsSeen,
                    walkthroughStep = walkthroughStep,
                    onWalkthroughAdvance = {
                        walkthroughStep++
                        if (walkthroughStep > 2) {
                            walkthroughStep = -1
                            viewModel.completeWalkthrough()
                        }
                    }
                )
            }
        }
        }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainContent(
    selectedSound: Sound,
    streak: Int,
    totalFahhCount: Int,
    highestComboTier: Int,
    onComboTierUnlocked: (Int) -> Unit,
    onPlayClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNewSoundsClick: () -> Unit,
    showNewSoundsPrompt: Boolean,
    walkthroughStep: Int = -1,
    onWalkthroughAdvance: () -> Unit = {}
) {
    // A fixed three-second window starts on the first tap, then resets at its boundary.
    val comboCounter = remember { ComboWindowCounter() }
    var highestTierShown by remember { mutableIntStateOf(0) }
    data class ComboCelebration(val id: Int, val label: String, val color: Color)
    val celebrations = remember { mutableStateListOf<ComboCelebration>() }
    var celebrationId by remember { mutableIntStateOf(0) }
    val comboScope = rememberCoroutineScope()

    // Tier definitions
    data class ComboTier(val threshold: Int, val index: Int, val label: String, val color: Color)
    val comboTiers = remember { listOf(
        ComboTier(2, 1, "\u26A1 2x COMBO", Color(0xFF90CAF9)),
        ComboTier(4, 2, "\uD83D\uDD25 4x COMBO", Color(0xFFFF9100)),
        ComboTier(7, 3, "\uD83D\uDCA5 7x COMBO", Color(0xFFFFAB40)),
        ComboTier(10, 4, "\uD83C\uDF1F 10x COMBO", Color(0xFFFFD740)),
        ComboTier(15, 5, "\uD83D\uDC7E TAP MONSTER", Color(0xFF76FF03)),
        ComboTier(17, 6, "\uD83E\uDD2F SPEED DEMON", Color(0xFFE040FB)),
        ComboTier(20, 7, "\uD83D\uDC51 ULTRA LEGENDARY PRO", Color(0xFFFFD700)),
        ComboTier(25, 8, "\uD83D\uDEA8 CHEATER", Color(0xFFFF1744)),
        ComboTier(30, 9, "\uD83D\uDC80 FINGER GOD", Color(0xFFBB86FC)),
        ComboTier(40, 10, "\uD83C\uDF00 DIMENSION BREAKER", Color(0xFF00E5FF)),
        ComboTier(50, 11, "\uD83E\uDEE0 TOUCH GRASS", Color(0xFFFF4081)),
        ComboTier(60, 12, "\uD83E\uDDE0 ARE YOU OK?", Color(0xFF00E676))
    ) }

    fun onButtonTap() {
        val count = comboCounter.registerTap(SystemClock.elapsedRealtime())
        if (count == 1) {
            highestTierShown = 0
            celebrations.clear()
        }

        val newlyReachedTier = comboTiers.firstOrNull { count == it.threshold }
        if (newlyReachedTier != null && newlyReachedTier.index > highestTierShown) {
            highestTierShown = newlyReachedTier.index
            onComboTierUnlocked(newlyReachedTier.index)
            celebrationId++
            val item = ComboCelebration(
                id = celebrationId,
                label = newlyReachedTier.label,
                color = newlyReachedTier.color
            )
            if (celebrations.size == 5) celebrations.removeAt(0)
            celebrations.add(item)
            comboScope.launch {
                delay(4_000)
                celebrations.removeAll { it.id == item.id }
            }
        }
    }

    // Get current rank label for top bar
    val currentRank = comboTiers.lastOrNull { it.index <= highestComboTier }
    var achievementsExpanded by remember { mutableStateOf(false) }
    val achievementScale = remember { Animatable(1f) }
    LaunchedEffect(currentRank?.index) {
        if (currentRank != null) {
            achievementScale.snapTo(0.92f)
            achievementScale.animateTo(1.12f, spring(dampingRatio = 0.48f, stiffness = 680f))
            achievementScale.animateTo(1f, tween(160))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                var horizontalTravel = 0f
                var drawerOpened = false
                detectHorizontalDragGestures(
                    onDragStart = {
                        horizontalTravel = 0f
                        drawerOpened = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (drawerOpened) return@detectHorizontalDragGestures
                        horizontalTravel += dragAmount
                        // The home screen has no horizontal content to protect. A deliberate
                        // side swipe can therefore begin away from the physical screen edge.
                        if (showNewSoundsPrompt && horizontalTravel >= 42.dp.toPx()) {
                            onNewSoundsClick()
                            drawerOpened = true
                        } else if (horizontalTravel <= -42.dp.toPx()) {
                            onMenuClick()
                            drawerOpened = true
                        }
                    },
                    onDragEnd = {
                        horizontalTravel = 0f
                        drawerOpened = false
                    },
                    onDragCancel = {
                        horizontalTravel = 0f
                        drawerOpened = false
                    }
                )
            }
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(400.dp)
                .blur(120.dp)
                .graphicsLayer { alpha = 0.12f }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    IconButton(
                        onClick = onGalleryClick,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Reaction gallery", tint = Color.White)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.fahh_logo_wide),
                        contentDescription = "Fahh",
                        modifier = Modifier.align(Alignment.Center).height(36.dp)
                    )
                    Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) {
                        Surface(
                            onClick = { achievementsExpanded = true },
                            shape = RoundedCornerShape(50),
                            color = (currentRank?.color ?: Primary).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, (currentRank?.color ?: Primary).copy(alpha = 0.35f)),
                            shadowElevation = 4.dp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = achievementScale.value
                                scaleY = achievementScale.value
                            }
                        ) {
                            Text(
                                text = currentRank?.label ?: "ACHIEVEMENTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentRank?.color ?: Primary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = achievementsExpanded,
                            onDismissRequest = { achievementsExpanded = false }
                        ) {
                            comboTiers.forEach { tier ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (tier.index <= highestComboTier) "✓ ${tier.label}" else "${tier.threshold} taps · ${tier.label}",
                                            color = if (tier.index <= highestComboTier) tier.color else Color.White.copy(alpha = 0.55f),
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = { achievementsExpanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("The first tap starts a fixed 3-second combo window.", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp) },
                                onClick = { achievementsExpanded = false }
                            )
                        }
                    }
                }
            },
            bottomBar = {
                FahhBottomBar(
                    onCameraClick = onCameraClick,
                    onNewSoundsClick = onNewSoundsClick,
                    onMenuClick = onMenuClick,
                    showNewSoundsPrompt = showNewSoundsPrompt
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stats row at top
                if (totalFahhCount > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        if (streak > 0) {
                            Text(
                                text = "\uD83D\uDD25 $streak day streak",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$totalFahhCount presses",
                                color = Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Main button area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SoundButton(
                        sound = selectedSound,
                        onClick = onPlayClick,
                        onTap = { onButtonTap() },
                        buttonSize = 260.dp
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(84.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.animateContentSize(tween(180))
                        ) {
                            celebrations.forEachIndexed { index, item ->
                                val relativeAge = if (celebrations.lastIndex <= 0) 1f
                                else index.toFloat() / celebrations.lastIndex.toFloat()
                                Text(
                                    text = item.label,
                                    color = item.color,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.35.sp,
                                    maxLines = 1,
                                    modifier = Modifier.graphicsLayer {
                                        alpha = 0.22f + (0.78f * relativeAge)
                                    }
                                )
                            }
                        }
                    }
                }

            }
        }

        if (showNewSoundsPrompt) {
            SwipeEdgeTab(
                fromLeft = true,
                onClick = onNewSoundsClick,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 142.dp)
            )
        }
        SwipeEdgeTab(
            fromLeft = false,
            onClick = onMenuClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 142.dp)
        )

        // ═══ WALKTHROUGH OVERLAYS ═══

        // Step 0 — "Press the button" prompt with pointer
        AnimatedVisibility(
            visible = walkthroughStep == 0,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center).offset(y = (-180).dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WalkthroughBubble(
                    text = "that big red button.\nit needs to be pressed.\ngo on. press it."
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Pointer arrow down to button
                Text(
                    text = "▼",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 20.sp
                )
            }
        }

        // Step 1 — "Feels good" meme after first press
        AnimatedVisibility(
            visible = walkthroughStep == 1,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
            exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it / 3 },
            modifier = Modifier.align(Alignment.Center).offset(y = (-160).dp)
        ) {
            LaunchedEffect(Unit) {
                delay(3000)
                onWalkthroughAdvance()
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    shadowElevation = 0.dp,
                    border = BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.feels_good),
                            contentDescription = "feels good",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "admit it.\nthat felt amazing.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "do it again. we won't judge.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Step 2 — Sidebar hint
        AnimatedVisibility(
            visible = walkthroughStep == 2,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
            exit = fadeOut(tween(400)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 96.dp)
        ) {
            LaunchedEffect(Unit) {
                delay(3500)
                onWalkthroughAdvance()
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(
                    1.dp, Color.White.copy(alpha = 0.2f)
                ),
                shadowElevation = 0.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "psst… sounds live down here",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "tap Sounds to browse →",
                            color = Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("👉", fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun FahhBottomBar(
    onCameraClick: () -> Unit,
    onNewSoundsClick: () -> Unit,
    onMenuClick: () -> Unit,
    showNewSoundsPrompt: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth().height(116.dp)) {
    NavigationBar(
        containerColor = Color(0xFF111923),
        contentColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        if (showNewSoundsPrompt) {
            NavigationBarItem(
                selected = false,
                onClick = onNewSoundsClick,
                icon = { NewSoundsStar() },
                label = { Text("New sounds", color = Color.White.copy(alpha = 0.72f)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.White.copy(alpha = 0.72f)
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = false,
            onClick = onMenuClick,
            icon = { Icon(Icons.Default.Menu, contentDescription = "Open sounds") },
            label = { Text("Sounds") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
    }
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                onClick = onCameraClick,
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 16.dp,
                border = BorderStroke(2.dp, Color(0xFFFF9B8A)),
                modifier = Modifier.size(88.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF8A78), Primary, Color(0xFFA82225))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = "Open camera", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            Text("Camera", color = Color.White.copy(alpha = 0.76f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NewSoundsStar() {
    val transition = rememberInfiniteTransition(label = "newSoundsStar")
    val glow by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2_400), RepeatMode.Reverse),
        label = "newSoundsStarGlow"
    )
    Icon(
        Icons.Default.AutoAwesome,
        contentDescription = "New sounds",
        tint = Color(0xFFFFC56D).copy(alpha = glow)
    )
}

@Composable
private fun SwipeEdgeTab(
    fromLeft: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "edgeTab")
    val nudge by transition.animateFloat(
        initialValue = 0f,
            targetValue = if (fromLeft) 2f else -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edgeNudge"
    )

    Surface(
        onClick = onClick,
        shape = if (fromLeft) RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
        else RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
        color = Primary.copy(alpha = 0.18f),
        modifier = modifier
            .offset(x = nudge.dp)
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = if (fromLeft) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                contentDescription = if (fromLeft) "Open new sounds" else "Open sounds",
                tint = Color.White.copy(alpha = 0.58f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun WalkthroughBubble(text: String) {
    val transition = rememberInfiniteTransition(label = "bubble")
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(
            1.dp, Color.White.copy(alpha = 0.35f)
        ),
        shadowElevation = 0.dp,
        modifier = Modifier.offset(y = floatY.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
