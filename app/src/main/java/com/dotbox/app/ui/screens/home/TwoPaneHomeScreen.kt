package com.dotbox.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.data.repository.ToolsRepository
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
import com.dotbox.app.ui.screens.tools.WifiQRScreen
import com.dotbox.app.ui.screens.tools.WorkoutTimerScreen
import com.dotbox.app.ui.screens.tools.RepCounterScreen
import com.dotbox.app.ui.screens.tools.OneRepMaxScreen
import com.dotbox.app.ui.screens.tools.PaceCalculatorScreen
import com.dotbox.app.ui.screens.tools.SplitTimerScreen
import com.dotbox.app.ui.screens.tools.CameraColorPickerScreen

private const val EMPTY_ROUTE = "two_pane_empty"

@Composable
fun TwoPaneHomeScreen(
    repository: ToolsRepository,
    onSettingsClick: () -> Unit,
) {
    val detailNavController = rememberNavController()
    var selectedTool by remember { mutableStateOf<ToolId?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane: tool grid
        HomeScreen(
            repository = repository,
            onToolClick = { tool ->
                selectedTool = tool
                detailNavController.navigate(tool.route) {
                    popUpTo(EMPTY_ROUTE)
                }
            },
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
        )

        // Divider
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        // Right pane: selected tool detail
        NavHost(
            navController = detailNavController,
            startDestination = EMPTY_ROUTE,
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
        ) {
            composable(EMPTY_ROUTE) {
                TwoPaneEmptyState()
            }

            // All tool routes — each with onBack navigating back to empty
            val onBack: () -> Unit = {
                selectedTool = null
                detailNavController.navigate(EMPTY_ROUTE) {
                    popUpTo(EMPTY_ROUTE) { inclusive = true }
                }
            }

            // ── Utilities ──
            composable(ToolId.FLASHLIGHT.route) { FlashlightScreen(onBack = onBack) }
            composable(ToolId.STOPWATCH.route) { StopwatchScreen(onBack = onBack) }
            composable(ToolId.RANDOM_GENERATOR.route) { RandomGeneratorScreen(onBack = onBack) }
            composable(ToolId.COUNTDOWN_TIMER.route) { CountdownTimerScreen(onBack = onBack) }
            composable(ToolId.CLIPBOARD_MANAGER.route) { ClipboardManagerScreen(onBack = onBack) }
            composable(ToolId.NETWORK_INFO.route) { NetworkInfoScreen(onBack = onBack) }
            composable(ToolId.BATTERY_INFO.route) { BatteryInfoScreen(onBack = onBack) }
            composable(ToolId.HABIT_TRACKER.route) { HabitTrackerScreen(onBack = onBack) }
            composable(ToolId.POMODORO_TIMER.route) { PomodoroTimerScreen(onBack = onBack) }
            composable(ToolId.COUNTER.route) { CounterScreen(onBack = onBack) }
            composable(ToolId.PASSWORD_STRENGTH.route) { PasswordStrengthScreen(onBack = onBack) }
            composable(ToolId.SCREEN_INFO.route) { ScreenInfoScreen(onBack = onBack) }

            // ── Calculators ──
            composable(ToolId.CALCULATOR.route) { CalculatorScreen(onBack = onBack) }
            composable(ToolId.TIP_CALCULATOR.route) { TipCalculatorScreen(onBack = onBack) }
            composable(ToolId.PERCENTAGE_CALCULATOR.route) { PercentageCalculatorScreen(onBack = onBack) }
            composable(ToolId.DATE_CALCULATOR.route) { DateCalculatorScreen(onBack = onBack) }
            composable(ToolId.LOAN_CALCULATOR.route) { LoanCalculatorScreen(onBack = onBack) }
            composable(ToolId.ASPECT_RATIO_CALCULATOR.route) { AspectRatioCalculatorScreen(onBack = onBack) }

            // ── Measurement ──
            composable(ToolId.COMPASS.route) { CompassScreen(onBack = onBack) }
            composable(ToolId.LEVEL.route) { LevelScreen(onBack = onBack) }
            composable(ToolId.RULER.route) { RulerScreen(onBack = onBack) }
            composable(ToolId.SOUND_METER.route) { SoundMeterScreen(onBack = onBack) }
            composable(ToolId.SPEEDOMETER.route) { SpeedometerScreen(onBack = onBack) }
            composable(ToolId.MAGNIFIER.route) { MagnifierScreen(onBack = onBack) }
            composable(ToolId.LIGHT_METER.route) { LightMeterScreen(onBack = onBack) }

            // ── Converters ──
            composable(ToolId.UNIT_CONVERTER.route) { UnitConverterScreen(onBack = onBack) }
            composable(ToolId.CURRENCY_CONVERTER.route) { CurrencyConverterScreen(onBack = onBack) }
            composable(ToolId.NUMBER_BASE_CONVERTER.route) { NumberBaseConverterScreen(onBack = onBack) }
            composable(ToolId.TIME_ZONE_CONVERTER.route) { TimeZoneConverterScreen(onBack = onBack) }
            composable(ToolId.COOKING_CONVERTER.route) { CookingConverterScreen(onBack = onBack) }
            composable(ToolId.CLOTHING_SIZE.route) { ClothingSizeScreen(onBack = onBack) }
            composable(ToolId.MORSE_CODE.route) { MorseCodeScreen(onBack = onBack) }

            // ── Generators ──
            composable(ToolId.QR_GENERATOR.route) { QRGeneratorScreen(onBack = onBack) }
            composable(ToolId.COLOR_PICKER.route) { ColorPickerScreen(onBack = onBack) }
            composable(ToolId.TEXT_TOOLS.route) { TextToolsScreen(onBack = onBack) }
            composable(ToolId.FREQUENCY_GENERATOR.route) { FrequencyGeneratorScreen(onBack = onBack) }
            composable(ToolId.WIFI_QR.route) { WifiQRScreen(onBack = onBack) }
            composable(ToolId.COLOR_FROM_CAMERA.route) { CameraColorPickerScreen(onBack = onBack) }

            // ── Scanners ──
            composable(ToolId.QR_SCANNER.route) { QRScannerScreen(onBack = onBack) }
            composable(ToolId.DOCUMENT_SCANNER.route) { DocumentScannerScreen(onBack = onBack) }

            // ── Fitness ──
            composable(ToolId.BMI_CALCULATOR.route) { BMICalculatorScreen(onBack = onBack) }
            composable(ToolId.BMR_CALCULATOR.route) { BMRCalculatorScreen(onBack = onBack) }
            composable(ToolId.HEART_RATE_ZONES.route) { HeartRateZonesScreen(onBack = onBack) }
            composable(ToolId.BODY_FAT_CALCULATOR.route) { BodyFatCalculatorScreen(onBack = onBack) }
            composable(ToolId.WATER_INTAKE.route) { WaterIntakeScreen(onBack = onBack) }
            composable(ToolId.IDEAL_BODY_WEIGHT.route) { IdealBodyWeightScreen(onBack = onBack) }
            composable(ToolId.WORKOUT_TIMER.route) { WorkoutTimerScreen(onBack = onBack) }
            composable(ToolId.REP_COUNTER.route) { RepCounterScreen(onBack = onBack) }
            composable(ToolId.ONE_REP_MAX.route) { OneRepMaxScreen(onBack = onBack) }
            composable(ToolId.PACE_CALCULATOR.route) { PaceCalculatorScreen(onBack = onBack) }
            composable(ToolId.SPLIT_TIMER.route) { SplitTimerScreen(onBack = onBack) }

            // ── Medical ──
            composable(ToolId.BAC_CALCULATOR.route) { BACCalculatorScreen(onBack = onBack) }
            composable(ToolId.DUE_DATE_CALCULATOR.route) { DueDateCalculatorScreen(onBack = onBack) }
            composable(ToolId.DOSE_CALCULATOR.route) { DoseCalculatorScreen(onBack = onBack) }
            composable(ToolId.IV_DRIP_RATE.route) { IVDripRateScreen(onBack = onBack) }
        }
    }
}

@Composable
private fun TwoPaneEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select a tool",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
