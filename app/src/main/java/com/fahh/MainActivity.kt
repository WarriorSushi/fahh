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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fahh.navigation.Screen
import com.fahh.ui.components.RateUsDialog
import com.fahh.ui.screens.*
import com.fahh.ui.theme.FahhTheme
import com.fahh.utils.ConsentManager
import com.fahh.utils.FahhWatermarkExporter
import com.fahh.utils.ShareUtils
import com.fahh.viewmodel.SoundViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

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

        // Request GDPR/EEA consent before loading ads
        ConsentManager.requestConsent(this) {
            // Consent gathered or not required — ads can now load
        }

        setContent {
            FahhTheme {
                val soundViewModel: SoundViewModel = hiltViewModel()
                val navController = rememberNavController()
                var lastVideoFile by remember { mutableStateOf<File?>(null) }
                val showRatingPrompt by soundViewModel.showRatingPrompt.collectAsState()

                // Wait for DataStore to resolve, then navigate once
                LaunchedEffect(Unit) {
                    val firstRun = soundViewModel.isFirstRunResolved()
                    val startRoute = when {
                        firstRun -> Screen.Onboarding.route
                        soundViewModel.shouldShowUpdateOnboarding() -> Screen.UpdateOnboarding.route
                        else -> Screen.Main.route
                    }
                    navController.navigate(startRoute) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }

                // Rating dialog — shown over any screen
                if (showRatingPrompt) {
                    RateUsDialog(
                        onRate = {
                            soundViewModel.onRatingAccepted()
                            val reviewManager = ReviewManagerFactory.create(this@MainActivity)
                            reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
                                reviewManager.launchReviewFlow(this@MainActivity, reviewInfo)
                            }
                        },
                        onDismiss = { soundViewModel.onRatingDismissed() }
                    )
                }

                // Slide transition helper
                val slideLeft: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(450))
                }
                val slideOutLeft: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(450))
                }
                val slideRight: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(450))
                }
                val slideOutRight: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(450))
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Loading.route,
                    enterTransition = { fadeIn(tween(500)) },
                    exitTransition = { fadeOut(tween(500)) }
                ) {
                    composable(Screen.Loading.route) {
                        // Empty — splash screen is still visible
                    }

                    composable(
                        Screen.Onboarding.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        OnboardingScreen(onFinish = {
                            soundViewModel.completeOnboarding()
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        })
                    }

                    composable(
                        Screen.UpdateOnboarding.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        UpdateOnboardingScreen(onFinish = {
                            soundViewModel.completeUpdateOnboarding()
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.UpdateOnboarding.route) { inclusive = true }
                            }
                        })
                    }

                    composable(Screen.Main.route) {
                        MainScreen(
                            onCameraClick = {
                                navController.navigate(Screen.Camera.route)
                            },
                            onPrivacyClick = {
                                navController.navigate(Screen.Privacy.route)
                            },
                            onComingSoonClick = {
                                navController.navigate(Screen.ComingSoon.route)
                            },
                            onGalleryClick = {
                                navController.navigate(Screen.Gallery.route)
                            },
                            onMySoundsClick = {
                                navController.navigate(Screen.MySounds.route)
                            },
                            viewModel = soundViewModel
                        )
                    }

                    composable(
                        Screen.Privacy.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        PrivacyPolicyScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        Screen.ComingSoon.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        ComingSoonScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        Screen.Camera.route,
                        enterTransition = {
                            fadeIn(tween(400)) + scaleIn(initialScale = 0.85f)
                        },
                        exitTransition = {
                            fadeOut(tween(400)) + scaleOut(targetScale = 1.1f)
                        },
                        popEnterTransition = {
                            fadeIn(tween(400)) + scaleIn(initialScale = 1.15f)
                        },
                        popExitTransition = {
                            fadeOut(tween(400)) + scaleOut(targetScale = 0.9f)
                        }
                    ) {
                        CameraScreen(
                            onBack = { navController.popBackStack() },
                            onCustomSoundsClick = { navController.navigate(Screen.MySounds.route) },
                            onVideoSaved = { file ->
                                val watermarkedFile = File(
                                    file.parentFile,
                                    "fahh_${file.nameWithoutExtension}.mp4"
                                )
                                FahhWatermarkExporter(this@MainActivity).export(
                                    inputFile = file,
                                    outputFile = watermarkedFile,
                                    onSuccess = { exported ->
                                        runOnUiThread {
                                            runCatching { file.delete() }
                                            lastVideoFile = exported
                                            soundViewModel.onRecordingFinished()
                                            navController.navigate(Screen.Share.route)
                                        }
                                    },
                                    onError = {
                                        // Never strand a user's recording if the device exporter fails.
                                        runOnUiThread {
                                            lastVideoFile = file
                                            soundViewModel.onRecordingFinished()
                                            navController.navigate(Screen.Share.route)
                                        }
                                    }
                                )
                            },
                            soundViewModel = soundViewModel
                        )
                    }

                    composable(
                        Screen.Share.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        val context = LocalContext.current
                        val file = lastVideoFile
                        if (file == null) {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        } else {
                            ShareScreen(
                                videoFile = file,
                                onBack = { navController.popBackStack() },
                                onShare = {
                                    ShareUtils.shareVideo(context, file)
                                    soundViewModel.onShareCompleted()
                                },
                                onTrim = {
                                    navController.navigate(Screen.Trim.route)
                                },
                                onDelete = {
                                    runCatching { file.delete() }
                                    lastVideoFile = null
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        Screen.Trim.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        val file = lastVideoFile
                        if (file == null) {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        } else {
                            TrimScreen(
                                sourceFile = file,
                                onBack = { navController.popBackStack() },
                                onTrimmed = { trimmed ->
                                    lastVideoFile = trimmed
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        Screen.Gallery.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        val context = LocalContext.current
                        GalleryScreen(
                            // Gallery is only entered from Home. Returning explicitly avoids a
                            // transient empty NavHost after a fast back press during its exit animation.
                            onBack = {
                                navController.popBackStack(Screen.Main.route, inclusive = false)
                            },
                            onShare = { file ->
                                ShareUtils.shareVideo(context, file)
                            },
                            onDelete = { file ->
                                runCatching { file.delete() }
                            }
                        )
                    }

                    composable(
                        Screen.MySounds.route,
                        enterTransition = slideLeft,
                        exitTransition = slideOutLeft,
                        popEnterTransition = slideRight,
                        popExitTransition = slideOutRight
                    ) {
                        MySoundsScreen(
                            onBack = { navController.popBackStack() },
                            soundViewModel = soundViewModel
                        )
                    }
                }
            }
        }
    }
}
