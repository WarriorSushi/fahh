package com.fahh.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.res.painterResource
import com.fahh.R
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
import com.fahh.viewmodel.SoundViewModel
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RewardedAdUnitId = "ca-app-pub-1006057089920582/1635547968"

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
    viewModel: SoundViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sounds by viewModel.allSounds.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val totalFahhCount by viewModel.totalFahhCount.collectAsState()
    val highestComboTier by viewModel.highestComboTier.collectAsState()

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
        if (!ConsentManager.canRequestAds(context)) return
        isRewardedAdLoading = true
        AdManager.loadRewardedAd(
            context = context,
            adUnitId = RewardedAdUnitId,
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

    BackHandler(enabled = drawerState.isOpen) {
        if (showSettings) {
            showSettings = false
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
            title = { Text("Unlock ${sound.packName} Pack", fontWeight = FontWeight.Bold) },
            text = {
                Text("Watch a short rewarded ad to unlock this sound pack.")
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
                                viewModel.unlockSound(sound.name)
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
                                onBack = { showSettings = false }
                            )
                        } else {
                            SidebarMenu(
                                sounds = sounds,
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
                                onSettingsClick = { showSettings = true },
                                onTipJarClick = { showTipJarDialog = true }
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
    walkthroughStep: Int = -1,
    onWalkthroughAdvance: () -> Unit = {}
) {
    // Combo system — sliding 3 second window
    val tapTimestamps = remember { mutableStateListOf<Long>() }
    var highestTierShown by remember { mutableIntStateOf(0) }

    // Flying text queue
    data class FlyingText(val id: Int, val label: String, val color: Color)
    val flyingTexts = remember { mutableStateListOf<FlyingText>() }
    var flyingId by remember { mutableIntStateOf(0) }

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
        val now = System.currentTimeMillis()

        // Add this tap and prune anything older than 3 seconds
        tapTimestamps.add(now)
        tapTimestamps.removeAll { now - it > 3000 }

        val count = tapTimestamps.size

        // If count dropped (after pruning), reset tier tracking
        // Find the highest tier we currently qualify for
        val currentHighestTier = comboTiers.lastOrNull { count >= it.threshold }?.index ?: 0
        if (currentHighestTier < highestTierShown) {
            // Window effectively reset — new combo chain
            highestTierShown = 0
        }

        // Show any new tiers we just crossed
        comboTiers.forEach { tier ->
            if (count >= tier.threshold && tier.index > highestTierShown) {
                highestTierShown = tier.index
                onComboTierUnlocked(tier.index)
                flyingId++
                flyingTexts.add(FlyingText(flyingId, tier.label, tier.color))
            }
        }
    }

    // Get current rank label for top bar
    val currentRank = comboTiers.lastOrNull { it.index <= highestComboTier }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
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
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.fahh_logo_wide),
                            contentDescription = "Fahh",
                            modifier = Modifier.height(44.dp)
                        )
                    },
                    actions = {
                        if (currentRank != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = currentRank.color.copy(alpha = 0.12f),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = currentRank.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentRank.color,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .premiumGlass(CircleShape, alpha = 0.05f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Open sound menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    Text(
                        text = "\uD83D\uDD25 $streak day streak \u00B7 $totalFahhCount presses",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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

                    // Flying combo texts — start above the button, fly upward
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-170).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        flyingTexts.forEach { ft ->
                            key(ft.id) {
                                FlyingComboText(
                                    text = ft.label,
                                    color = ft.color,
                                    onFinish = { flyingTexts.removeAll { it.id == ft.id } }
                                )
                            }
                        }
                    }
                }

                // Camera + Gallery buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    Surface(
                        onClick = onCameraClick,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Camera",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        onClick = onGalleryClick,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Gallery",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Swipe hint on right edge — tappable to open sidebar (must be after Scaffold to receive taps)
        SwipeEdgeTab(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 140.dp)
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
                .padding(end = 36.dp, bottom = 160.dp)
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
                            text = "psst… more sounds",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "hiding over there →",
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
private fun FlyingComboText(
    text: String,
    color: Color,
    onFinish: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.3f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Pop in
        launch { alpha.animateTo(1f, tween(120)) }
        launch { scale.animateTo(1.15f, spring(dampingRatio = 0.45f, stiffness = 800f)) }
        // Brief hold at center
        delay(200)
        // Shrink slightly to normal
        launch { scale.animateTo(1f, tween(150)) }
        // Float upward and fade out
        delay(600)
        launch { offsetY.animateTo(-160f, tween(700, easing = LinearOutSlowInEasing)) }
        alpha.animateTo(0f, tween(700))
        onFinish()
    }

    Text(
        text = text,
        color = color,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier
            .offset(y = offsetY.value.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
    )
}

@Composable
private fun SwipeEdgeTab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "edgeTab")
    val nudge by transition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edgeNudge"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
        color = Primary.copy(alpha = 0.35f),
        modifier = modifier
            .offset(x = nudge.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Open sounds",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(18.dp)
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
