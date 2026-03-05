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
import com.dotbox.app.ui.screens.tools.AspectRatioCalculatorScreen
import com.dotbox.app.ui.screens.tools.BatteryInfoScreen
import com.dotbox.app.ui.screens.tools.CalculatorScreen
import com.dotbox.app.ui.screens.tools.ClipboardManagerScreen
import com.dotbox.app.ui.screens.tools.ColorPickerScreen
import com.dotbox.app.ui.screens.tools.CompassScreen
import com.dotbox.app.ui.screens.tools.CountdownTimerScreen
import com.dotbox.app.ui.screens.tools.CurrencyConverterScreen
import com.dotbox.app.ui.screens.tools.DateCalculatorScreen
import com.dotbox.app.ui.screens.tools.DocumentScannerScreen
import com.dotbox.app.ui.screens.tools.FlashlightScreen
import com.dotbox.app.ui.screens.tools.LevelScreen
import com.dotbox.app.ui.screens.tools.LoanCalculatorScreen
import com.dotbox.app.ui.screens.tools.MagnifierScreen
import com.dotbox.app.ui.screens.tools.NetworkInfoScreen
import com.dotbox.app.ui.screens.tools.NumberBaseConverterScreen
import com.dotbox.app.ui.screens.tools.PercentageCalculatorScreen
import com.dotbox.app.ui.screens.tools.QRGeneratorScreen
import com.dotbox.app.ui.screens.tools.QRScannerScreen
import com.dotbox.app.ui.screens.tools.RandomGeneratorScreen
import com.dotbox.app.ui.screens.tools.RulerScreen
import com.dotbox.app.ui.screens.tools.SoundMeterScreen
import com.dotbox.app.ui.screens.tools.SpeedometerScreen
import com.dotbox.app.ui.screens.tools.StopwatchScreen
import com.dotbox.app.ui.screens.tools.TextToolsScreen
import com.dotbox.app.ui.screens.tools.TimeZoneConverterScreen
import com.dotbox.app.ui.screens.tools.TipCalculatorScreen
import com.dotbox.app.ui.screens.tools.UnitConverterScreen
import com.dotbox.app.ui.screens.tools.BMICalculatorScreen
import com.dotbox.app.ui.screens.tools.BMRCalculatorScreen
import com.dotbox.app.ui.screens.tools.HeartRateZonesScreen
import com.dotbox.app.ui.screens.tools.BodyFatCalculatorScreen
import com.dotbox.app.ui.screens.tools.BACCalculatorScreen
import com.dotbox.app.ui.screens.tools.DueDateCalculatorScreen
import com.dotbox.app.ui.screens.tools.DoseCalculatorScreen
import com.dotbox.app.ui.screens.tools.IVDripRateScreen
import com.dotbox.app.ui.screens.tools.WaterIntakeScreen
import com.dotbox.app.ui.screens.tools.IdealBodyWeightScreen
import com.dotbox.app.ui.screens.tools.HabitTrackerScreen
import com.dotbox.app.ui.screens.tools.PomodoroTimerScreen
import com.dotbox.app.ui.screens.tools.CounterScreen
import com.dotbox.app.ui.screens.tools.PasswordStrengthScreen
import com.dotbox.app.ui.screens.tools.ScreenInfoScreen
import com.dotbox.app.ui.screens.tools.LightMeterScreen
import com.dotbox.app.ui.screens.tools.CookingConverterScreen
import com.dotbox.app.ui.screens.tools.ClothingSizeScreen
import com.dotbox.app.ui.screens.tools.MorseCodeScreen
import com.dotbox.app.ui.screens.tools.FrequencyGeneratorScreen
import com.dotbox.app.ui.screens.settings.SettingsScreen

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
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ── Utilities ──
        composable(ToolId.FLASHLIGHT.route) {
            FlashlightScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.STOPWATCH.route) {
            StopwatchScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.RANDOM_GENERATOR.route) {
            RandomGeneratorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.COUNTDOWN_TIMER.route) {
            CountdownTimerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.CLIPBOARD_MANAGER.route) {
            ClipboardManagerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.NETWORK_INFO.route) {
            NetworkInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.BATTERY_INFO.route) {
            BatteryInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.HABIT_TRACKER.route) {
            HabitTrackerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.POMODORO_TIMER.route) {
            PomodoroTimerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.COUNTER.route) {
            CounterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.PASSWORD_STRENGTH.route) {
            PasswordStrengthScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.SCREEN_INFO.route) {
            ScreenInfoScreen(onBack = { navController.popBackStack() })
        }

        // ── Calculators ──
        composable(ToolId.CALCULATOR.route) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.TIP_CALCULATOR.route) {
            TipCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.PERCENTAGE_CALCULATOR.route) {
            PercentageCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.DATE_CALCULATOR.route) {
            DateCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.LOAN_CALCULATOR.route) {
            LoanCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.ASPECT_RATIO_CALCULATOR.route) {
            AspectRatioCalculatorScreen(onBack = { navController.popBackStack() })
        }

        // ── Measurement ──
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
        composable(ToolId.LIGHT_METER.route) {
            LightMeterScreen(onBack = { navController.popBackStack() })
        }

        // ── Converters ──
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
        composable(ToolId.COOKING_CONVERTER.route) {
            CookingConverterScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.CLOTHING_SIZE.route) {
            ClothingSizeScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.MORSE_CODE.route) {
            MorseCodeScreen(onBack = { navController.popBackStack() })
        }

        // ── Generators ──
        composable(ToolId.QR_GENERATOR.route) {
            QRGeneratorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.COLOR_PICKER.route) {
            ColorPickerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.TEXT_TOOLS.route) {
            TextToolsScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.FREQUENCY_GENERATOR.route) {
            FrequencyGeneratorScreen(onBack = { navController.popBackStack() })
        }

        // ── Scanners ──
        composable(ToolId.QR_SCANNER.route) {
            QRScannerScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.DOCUMENT_SCANNER.route) {
            DocumentScannerScreen(onBack = { navController.popBackStack() })
        }

        // ── Medical ──
        composable(ToolId.BMI_CALCULATOR.route) {
            BMICalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.BMR_CALCULATOR.route) {
            BMRCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.HEART_RATE_ZONES.route) {
            HeartRateZonesScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.BODY_FAT_CALCULATOR.route) {
            BodyFatCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.BAC_CALCULATOR.route) {
            BACCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.DUE_DATE_CALCULATOR.route) {
            DueDateCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.DOSE_CALCULATOR.route) {
            DoseCalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.IV_DRIP_RATE.route) {
            IVDripRateScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.WATER_INTAKE.route) {
            WaterIntakeScreen(onBack = { navController.popBackStack() })
        }
        composable(ToolId.IDEAL_BODY_WEIGHT.route) {
            IdealBodyWeightScreen(onBack = { navController.popBackStack() })
        }
    }
}
