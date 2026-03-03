package com.dotbox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dotbox.app.ui.navigation.DotBoxNavGraph
import com.dotbox.app.ui.theme.DotBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DotBoxApplication

        setContent {
            DotBoxTheme {
                val navController = rememberNavController()
                DotBoxNavGraph(
                    navController = navController,
                    repository = app.toolsRepository,
                )
            }
        }
    }
}
