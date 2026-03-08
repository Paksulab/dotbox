package com.dotbox.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.dotbox.app.data.preferences.AppPreferences
import com.dotbox.app.ui.navigation.DotBoxNavGraph
import com.dotbox.app.ui.navigation.Screen
import com.dotbox.app.ui.screens.onboarding.hasSeenOnboarding
import com.dotbox.app.ui.theme.DotBoxTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DotBoxApplication

        // Determine start destination before setContent (synchronous read)
        val startDest = if (hasSeenOnboarding(this)) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            val prefs = remember { AppPreferences.get(this@MainActivity) }
            val themeMode = remember {
                mutableStateOf(prefs.getString(AppPreferences.KEY_THEME, "dark") ?: "dark")
            }

            // React to SharedPreferences changes (fires immediately when Settings writes)
            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (key == AppPreferences.KEY_THEME) {
                        themeMode.value = sp.getString(AppPreferences.KEY_THEME, "dark") ?: "dark"
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val isDark = when (themeMode.value) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true // "dark" default
            }

            DotBoxTheme(darkTheme = isDark) {
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
                val twoPaneEnabled = prefs.getBoolean(AppPreferences.KEY_TWO_PANE, false)

                val navController = rememberNavController()
                DotBoxNavGraph(
                    navController = navController,
                    repository = app.toolsRepository,
                    startDestination = startDest,
                    useTwoPane = isExpanded && twoPaneEnabled,
                )
            }
        }
    }
}
