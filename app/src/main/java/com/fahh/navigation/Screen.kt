package com.fahh.navigation

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Camera : Screen("camera")
    object Share : Screen("share")
    object Trim : Screen("trim")
    object Privacy : Screen("privacy")
    object ComingSoon : Screen("coming_soon")
    object Gallery : Screen("gallery")
    object MySounds : Screen("my_sounds")
}
