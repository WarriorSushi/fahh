package com.fahh.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.fahh.ui.components.SidebarMenu
import com.fahh.ui.components.SoundButton
import com.fahh.utils.AdManager
import com.fahh.viewmodel.SoundViewModel
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"

private data class SidebarNotice(
    val token: Long,
    val message: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    onCameraClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    viewModel: SoundViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sounds by viewModel.allSounds.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val volume by viewModel.volume.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

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
        scope.launch { drawerState.close() }
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MainContent(
                    selectedSound = selectedSound,
                    onPlayClick = {
                        clearSidebarNotice()
                        viewModel.playSelectedSound()
                    },
                    onCameraClick = onCameraClick,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainContent(
    selectedSound: Sound,
    onPlayClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMenuClick: () -> Unit
) {
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

        // Tiny swipe hint on right edge
        SwipeEdgeTab(
            modifier = Modifier
                .align(Alignment.CenterEnd)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.fahh_logo_wide),
                            contentDescription = "Fahh",
                            modifier = Modifier.height(36.dp)
                        )
                    },
                    actions = {
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
                        buttonSize = 260.dp
                    )
                }

                // Camera button
                Button(
                    onClick = onCameraClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "GO TO CAMERA MODE",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SwipeEdgeTab(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "edgeTab")
    val nudge by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edgeNudge"
    )

    Surface(
        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = modifier
            .offset(x = nudge.dp)
            .width(16.dp)
            .height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Swipe for sounds",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
