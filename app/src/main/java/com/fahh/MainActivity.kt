package com.fahh

import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.fahh.ui.screens.*
import com.fahh.ui.theme.FahhTheme
import com.fahh.utils.ShareUtils
import com.fahh.viewmodel.SoundViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashProvider ->
            val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 300 }
            splashProvider.view.startAnimation(fadeOut)
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    splashProvider.remove()
                }
                override fun onAnimationStart(animation: Animation?) = Unit
                override fun onAnimationRepeat(animation: Animation?) = Unit
            })
        }

        setContent {
            FahhTheme {
                val soundViewModel: SoundViewModel = hiltViewModel()
                val isFirstRun by soundViewModel.isFirstRun.collectAsState()
                val startScreen = if (isFirstRun) "onboarding" else "main"
                var currentScreen by remember { mutableStateOf(startScreen) }
                var previousScreen by remember { mutableStateOf("main") }
                var lastVideoFile by remember { mutableStateOf<File?>(null) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        when {
                            targetState == "camera" -> {
                                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.85f))
                                    .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 1.1f))
                            }
                            initialState == "camera" && targetState == "main" -> {
                                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 1.15f))
                                    .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.9f))
                            }
                            targetState == "ad_transition" -> {
                                fadeIn(animationSpec = tween(400))
                                    .togetherWith(fadeOut(animationSpec = tween(400)))
                            }
                            targetState == "share" || targetState == "trim" || targetState == "onboarding" -> {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(450))
                                    .togetherWith(slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(450)))
                            }
                            initialState == "share" || initialState == "trim" || initialState == "onboarding" -> {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(450))
                                    .togetherWith(slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(450)))
                            }
                            else -> {
                                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                            }
                        }
                    },
                    label = "AppNavigation"
                ) { state ->
                    when (state) {
                        "onboarding" -> OnboardingScreen(onFinish = {
                            soundViewModel.completeOnboarding()
                            currentScreen = "main"
                        })

                        "main" -> MainScreen(
                            onCameraClick = {
                                previousScreen = "main"
                                currentScreen = "camera"
                            },
                            onPrivacyClick = {
                                previousScreen = "main"
                                currentScreen = "privacy"
                            },
                            viewModel = soundViewModel
                        )

                        "privacy" -> PrivacyPolicyScreen(
                            onBack = { currentScreen = previousScreen }
                        )

                        "camera" -> CameraScreen(
                            onBack = { currentScreen = "main" },
                            onVideoSaved = { file ->
                                lastVideoFile = file
                                previousScreen = "camera"
                                kotlinx.coroutines.MainScope().launch {
                                    val shouldShowAd = soundViewModel.onRecordingFinished()
                                    currentScreen = if (shouldShowAd) "ad_transition" else "share"
                                }
                            },
                            soundViewModel = soundViewModel
                        )

                        "ad_transition" -> AdTransitionScreen(
                            onFinished = { currentScreen = "share" }
                        )

                        "share" -> {
                            val context = LocalContext.current
                            val file = lastVideoFile
                            if (file == null) {
                                currentScreen = "camera"
                            } else {
                                ShareScreen(
                                    videoFile = file,
                                    onBack = { currentScreen = previousScreen },
                                    onShare = { ShareUtils.shareVideo(context, file) },
                                    onTrim = { currentScreen = "trim" },
                                    onDelete = {
                                        runCatching { file.delete() }
                                        lastVideoFile = null
                                        currentScreen = previousScreen
                                    }
                                )
                            }
                        }

                        "trim" -> {
                            val file = lastVideoFile
                            if (file == null) {
                                currentScreen = "share"
                            } else {
                                TrimScreen(
                                    sourceFile = file,
                                    onBack = { currentScreen = "share" },
                                    onTrimmed = { trimmed ->
                                        lastVideoFile = trimmed
                                        currentScreen = "share"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

