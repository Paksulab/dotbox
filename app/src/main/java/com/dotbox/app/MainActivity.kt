package com.dotbox.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.dotbox.app.ui.navigation.DotBoxNavGraph
import com.dotbox.app.ui.theme.DotBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DotBoxApplication

        setContent {
            val prefs = remember {
                getSharedPreferences("dotbox_settings", Context.MODE_PRIVATE)
            }
            val themeMode = remember {
                mutableStateOf(prefs.getString("theme_mode", "dark") ?: "dark")
            }

            // React to SharedPreferences changes (fires immediately when Settings writes)
            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (key == "theme_mode") {
                        themeMode.value = sp.getString("theme_mode", "dark") ?: "dark"
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
                val navController = rememberNavController()
                DotBoxNavGraph(
                    navController = navController,
                    repository = app.toolsRepository,
                )
            }
        }
    }
}
