package com.fahh.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.fahh.ui.components.SidebarMenu
import com.fahh.ui.components.SoundButton
import com.fahh.ui.theme.Primary
import com.fahh.ui.theme.premiumGlass
import com.fahh.viewmodel.CameraViewModel
import com.fahh.viewmodel.SoundViewModel
import java.io.File
import kotlinx.coroutines.launch

private fun requiredCameraPermissions(): Array<String> {
    val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
    return permissions.toTypedArray()
}

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onVideoSaved: (File) -> Unit,
    soundViewModel: SoundViewModel,
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isRecording by cameraViewModel.isRecording.collectAsState()
    val timer by cameraViewModel.recordingTimer.collectAsState()
    val selectedSound by soundViewModel.selectedSound.collectAsState()
    val sounds by soundViewModel.allSounds.collectAsState()
    val volume by soundViewModel.volume.collectAsState()
    val cameraSelector by cameraViewModel.cameraSelector.collectAsState()
    val cameraPermissions = remember { requiredCameraPermissions() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var hasPermissions by remember { mutableStateOf(false) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    fun checkPermissions() {
        hasPermissions = cameraPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkPermissions()
    }

    LaunchedEffect(Unit) {
        checkPermissions()
        if (!hasPermissions) {
            permissionLauncher.launch(cameraPermissions)
        }
    }

    LaunchedEffect(hasPermissions, cameraSelector, previewViewRef) {
        if (!hasPermissions) return@LaunchedEffect
        val previewView = previewViewRef ?: return@LaunchedEffect
        cameraViewModel.startCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onCaptureReady = { videoCapture = it },
            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
        )
    }

    BackHandler { onBack() }

    DisposableEffect(Unit) {
        onDispose {
            cameraViewModel.releaseCamera()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isRecording,
        drawerContent = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SidebarMenu(
                    sounds = sounds,
                    selectedSound = selectedSound,
                    volume = volume,
                    onVolumeChange = { soundViewModel.updateVolume(it) },
                    onSoundPreview = { sound ->
                        if (!sound.isLocked) soundViewModel.playSoundPreview(sound)
                    },
                    onSoundSelected = { sound ->
                        if (!sound.isLocked) {
                            soundViewModel.selectSound(sound)
                            scope.launch { drawerState.close() }
                        }
                    },
                    noticeMessage = null,
                    onDismissNotice = {},
                    onClose = { scope.launch { drawerState.close() } },
                    onPrivacyClick = {}
                )
            }
        }
    ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermissions) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Custom Transparent HUD
            Box(modifier = Modifier.fillMaxSize()) {
                // Top HUD Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .premiumGlass(CircleShape, alpha = 0.1f)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.premiumGlass(RoundedCornerShape(20.dp), alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRecording) {
                                val infiniteTransition = rememberInfiniteTransition(label = "RecDot")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 1f, targetValue = 0.3f,
                                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = ""
                                )
                                Box(modifier = Modifier.size(8.dp).graphicsLayer { this.alpha = alpha }.background(Primary, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isRecording) timer else "RECORDING MODE",
                                color = if (isRecording) Primary else Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Sounds drawer button (top right)
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .premiumGlass(CircleShape, alpha = 0.1f)
                            .size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = "Sounds", tint = Color.White)
                    }
                }

                // Center Warning Hint (Subtle)
                AnimatedVisibility(
                    visible = !isRecording,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier.align(Alignment.Center).padding(bottom = 200.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.premiumGlass(RoundedCornerShape(12.dp), alpha = 0.05f)
                    ) {
                        Text(
                            text = "Keep hands away from mic for best sound",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Bottom Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera switch — left
                    IconButton(
                        onClick = { if (!isRecording) cameraViewModel.toggleCamera() },
                        modifier = Modifier
                            .premiumGlass(CircleShape, alpha = 0.1f)
                            .size(60.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White, modifier = Modifier.size(26.dp))
                    }

                    // Record button — center
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) Color.Transparent else Color.White,
                            border = if (isRecording) null else BorderStroke(4.dp, Color.Black.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .size(72.dp)
                                .clickable {
                                    if (isRecording) cameraViewModel.stopRecording()
                                    else videoCapture?.let {
                                        cameraViewModel.startRecording(it, onVideoSaved, { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        })
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRecording) {
                                    Surface(
                                        modifier = Modifier.size(28.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Primary
                                    ) {}
                                } else {
                                    Box(modifier = Modifier.size(24.dp).background(Color.Red, CircleShape))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isRecording) "STOP" else "TAP TO RECORD",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Sound button — right (same height as record)
                    SoundButton(
                        sound = selectedSound,
                        onClick = { soundViewModel.playSelectedSound() },
                        buttonSize = 60.dp
                    )
                }
            }
        } else {
            PermissionRequiredContent(onGrantClick = { permissionLauncher.launch(cameraPermissions) })
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    } // CompositionLocalProvider Ltr
    } // ModalNavigationDrawer
    } // CompositionLocalProvider Rtl
}

@Composable
private fun PermissionRequiredContent(onGrantClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Camera Access Required", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("We need camera and microphone permissions to record your reactions with sounds.", 
                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp).fillMaxWidth()
            ) {
                Text("Enable Camera", fontWeight = FontWeight.Bold)
            }
        }
    }
}

