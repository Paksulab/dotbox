package com.dotbox.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.data.repository.ToolsRepository
import com.dotbox.app.ui.screens.home.HomeScreen
import com.dotbox.app.ui.screens.tools.CalculatorScreen
import com.dotbox.app.ui.screens.tools.ColorPickerScreen
import com.dotbox.app.ui.screens.tools.CompassScreen
import com.dotbox.app.ui.screens.tools.CurrencyConverterScreen
import com.dotbox.app.ui.screens.tools.DocumentScannerScreen
import com.dotbox.app.ui.screens.tools.FlashlightScreen
import com.dotbox.app.ui.screens.tools.LevelScreen
import com.dotbox.app.ui.screens.tools.MagnifierScreen
import com.dotbox.app.ui.screens.tools.NotesScreen
import com.dotbox.app.ui.screens.tools.NumberBaseConverterScreen
import com.dotbox.app.ui.screens.tools.QRGeneratorScreen
import com.dotbox.app.ui.screens.tools.QRScannerScreen
import com.dotbox.app.ui.screens.tools.RandomGeneratorScreen
import com.dotbox.app.ui.screens.tools.RulerScreen
import com.dotbox.app.ui.screens.tools.SoundMeterScreen
import com.dotbox.app.ui.screens.tools.SpeedometerScreen
import com.dotbox.app.ui.screens.tools.StopwatchScreen
import com.dotbox.app.ui.screens.tools.TextToolsScreen
import com.dotbox.app.ui.screens.tools.TimeZoneConverterScreen
import com.dotbox.app.ui.screens.tools.UnitConverterScreen

@Composable
fun DotBoxNavGraph(
    navController: NavHostController,
    repository: ToolsRepository,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffset = { it / 4 },
                )
        },
        exitTransition = {
            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        },
        popEnterTransition = {
            fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffset = { it / 4 },
                )
        },
        popExitTransition = {
            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        },
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                repository = repository,
                onToolClick = { tool -> navController.navigate(tool.route) },
            )
        }

        // Utilities
        composable(ToolId.CALCULATOR.route) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.FLASHLIGHT.route) {
            FlashlightScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.STOPWATCH.route) {
            StopwatchScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.NOTES.route) {
            NotesScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.RANDOM_GENERATOR.route) {
            RandomGeneratorScreen(onBack = { navController.popBackStack() })
        }

        // Measurement
        composable(ToolId.COMPASS.route) {
            CompassScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.LEVEL.route) {
            LevelScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.RULER.route) {
            RulerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.SOUND_METER.route) {
            SoundMeterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.SPEEDOMETER.route) {
            SpeedometerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.MAGNIFIER.route) {
            MagnifierScreen(onBack = { navController.popBackStack() })
        }

        // Converters
        composable(ToolId.UNIT_CONVERTER.route) {
            UnitConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.CURRENCY_CONVERTER.route) {
            CurrencyConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.NUMBER_BASE_CONVERTER.route) {
            NumberBaseConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.TIME_ZONE_CONVERTER.route) {
            TimeZoneConverterScreen(onBack = { navController.popBackStack() })
        }

        // Generators
        composable(ToolId.QR_GENERATOR.route) {
            QRGeneratorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.COLOR_PICKER.route) {
            ColorPickerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.TEXT_TOOLS.route) {
            TextToolsScreen(onBack = { navController.popBackStack() })
        }

        // Scanners
        composable(ToolId.QR_SCANNER.route) {
            QRScannerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.DOCUMENT_SCANNER.route) {
            DocumentScannerScreen(onBack = { navController.popBackStack() })
        }
    }
}
