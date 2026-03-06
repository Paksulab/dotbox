package com.dotbox.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object Onboarding : Screen("onboarding")
    data class ToolScreen(val toolRoute: String) : Screen(toolRoute)
}
