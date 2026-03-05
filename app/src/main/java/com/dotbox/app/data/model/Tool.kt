package com.dotbox.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Exposure
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dotbox.app.ui.theme.AccentCalculator
import com.dotbox.app.ui.theme.AccentConvert
import com.dotbox.app.ui.theme.AccentGenerate
import com.dotbox.app.ui.theme.AccentMeasure
import com.dotbox.app.ui.theme.AccentScan
import com.dotbox.app.ui.theme.AccentMedical
import com.dotbox.app.ui.theme.AccentUtility

enum class ToolCategory(
    val displayName: String,
    val accentColor: Color,
) {
    UTILITIES("Utilities", AccentUtility),
    CALCULATORS("Calculators", AccentCalculator),
    MEASUREMENT("Measurement", AccentMeasure),
    CONVERTERS("Converters", AccentConvert),
    GENERATORS("Generators", AccentGenerate),
    SCANNERS("Scanners", AccentScan),
    MEDICAL("Medical", AccentMedical),
}

enum class ToolId(
    val toolName: String,
    val description: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val route: String,
) {
    // ── Utilities ──
    FLASHLIGHT(
        toolName = "Flashlight",
        description = "Torch with strobe & SOS",
        icon = Icons.Outlined.FlashlightOn,
        category = ToolCategory.UTILITIES,
        route = "tool_flashlight",
    ),
    STOPWATCH(
        toolName = "Stopwatch",
        description = "Stopwatch & countdown timer",
        icon = Icons.Outlined.Timer,
        category = ToolCategory.UTILITIES,
        route = "tool_stopwatch",
    ),
    RANDOM_GENERATOR(
        toolName = "Random Generator",
        description = "Dice, coins, passwords & more",
        icon = Icons.Outlined.Casino,
        category = ToolCategory.UTILITIES,
        route = "tool_random_generator",
    ),
    COUNTDOWN_TIMER(
        toolName = "Countdown",
        description = "Timer to a target date",
        icon = Icons.Outlined.HourglassEmpty,
        category = ToolCategory.UTILITIES,
        route = "tool_countdown",
    ),
    CLIPBOARD_MANAGER(
        toolName = "Clipboard",
        description = "Clipboard history & pins",
        icon = Icons.Outlined.ContentPaste,
        category = ToolCategory.UTILITIES,
        route = "tool_clipboard",
    ),
    NETWORK_INFO(
        toolName = "Network Info",
        description = "IP, WiFi & connectivity",
        icon = Icons.Outlined.Wifi,
        category = ToolCategory.UTILITIES,
        route = "tool_network_info",
    ),
    BATTERY_INFO(
        toolName = "Battery Info",
        description = "Health, temp & voltage",
        icon = Icons.Outlined.BatteryStd,
        category = ToolCategory.UTILITIES,
        route = "tool_battery_info",
    ),
    HABIT_TRACKER(
        toolName = "Habit Tracker",
        description = "Daily check-in grid",
        icon = Icons.Outlined.GridView,
        category = ToolCategory.UTILITIES,
        route = "tool_habit_tracker",
    ),
    POMODORO_TIMER(
        toolName = "Pomodoro",
        description = "Focus timer 25/5 cycles",
        icon = Icons.Outlined.AvTimer,
        category = ToolCategory.UTILITIES,
        route = "tool_pomodoro",
    ),
    COUNTER(
        toolName = "Counter",
        description = "Tap to count & tally",
        icon = Icons.Outlined.Exposure,
        category = ToolCategory.UTILITIES,
        route = "tool_counter",
    ),
    PASSWORD_STRENGTH(
        toolName = "Password Check",
        description = "Strength & entropy",
        icon = Icons.Outlined.Lock,
        category = ToolCategory.UTILITIES,
        route = "tool_password_strength",
    ),
    SCREEN_INFO(
        toolName = "Screen Info",
        description = "DPI, size & refresh rate",
        icon = Icons.Outlined.Smartphone,
        category = ToolCategory.UTILITIES,
        route = "tool_screen_info",
    ),

    // ── Calculators ──
    CALCULATOR(
        toolName = "Calculator",
        description = "Scientific calculator",
        icon = Icons.Outlined.Calculate,
        category = ToolCategory.CALCULATORS,
        route = "tool_calculator",
    ),
    TIP_CALCULATOR(
        toolName = "Tip Calculator",
        description = "Split bills & tips",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        category = ToolCategory.CALCULATORS,
        route = "tool_tip_calculator",
    ),
    PERCENTAGE_CALCULATOR(
        toolName = "Percentage",
        description = "% of, change & difference",
        icon = Icons.Outlined.Percent,
        category = ToolCategory.CALCULATORS,
        route = "tool_percentage",
    ),
    DATE_CALCULATOR(
        toolName = "Date Calculator",
        description = "Age, days between dates",
        icon = Icons.Outlined.CalendarMonth,
        category = ToolCategory.CALCULATORS,
        route = "tool_date_calculator",
    ),
    LOAN_CALCULATOR(
        toolName = "Loan Calculator",
        description = "Monthly payments & interest",
        icon = Icons.Outlined.AccountBalance,
        category = ToolCategory.CALCULATORS,
        route = "tool_loan_calculator",
    ),
    ASPECT_RATIO_CALCULATOR(
        toolName = "Aspect Ratio",
        description = "Width, height & diagonals",
        icon = Icons.Outlined.AspectRatio,
        category = ToolCategory.CALCULATORS,
        route = "tool_aspect_ratio",
    ),

    // ── Measurement ──
    COMPASS(
        toolName = "Compass",
        description = "Digital compass",
        icon = Icons.Outlined.Explore,
        category = ToolCategory.MEASUREMENT,
        route = "tool_compass",
    ),
    LEVEL(
        toolName = "Level",
        description = "Spirit level",
        icon = Icons.Outlined.Tune,
        category = ToolCategory.MEASUREMENT,
        route = "tool_level",
    ),
    RULER(
        toolName = "Ruler",
        description = "On-screen ruler",
        icon = Icons.Outlined.Straighten,
        category = ToolCategory.MEASUREMENT,
        route = "tool_ruler",
    ),
    SOUND_METER(
        toolName = "Sound Meter",
        description = "Decibel meter",
        icon = Icons.Outlined.Speed,
        category = ToolCategory.MEASUREMENT,
        route = "tool_sound_meter",
    ),
    SPEEDOMETER(
        toolName = "Speedometer",
        description = "GPS speed & altitude",
        icon = Icons.Outlined.DirectionsCar,
        category = ToolCategory.MEASUREMENT,
        route = "tool_speedometer",
    ),
    MAGNIFIER(
        toolName = "Magnifier",
        description = "Camera zoom & torch",
        icon = Icons.Outlined.ZoomIn,
        category = ToolCategory.MEASUREMENT,
        route = "tool_magnifier",
    ),
    LIGHT_METER(
        toolName = "Light Meter",
        description = "Ambient lux sensor",
        icon = Icons.Outlined.LightMode,
        category = ToolCategory.MEASUREMENT,
        route = "tool_light_meter",
    ),

    // ── Converters ──
    UNIT_CONVERTER(
        toolName = "Unit Converter",
        description = "Length, weight, temp & more",
        icon = Icons.Outlined.SwapHoriz,
        category = ToolCategory.CONVERTERS,
        route = "tool_unit_converter",
    ),
    CURRENCY_CONVERTER(
        toolName = "Currency Converter",
        description = "Live exchange rates",
        icon = Icons.Outlined.AttachMoney,
        category = ToolCategory.CONVERTERS,
        route = "tool_currency_converter",
    ),
    NUMBER_BASE_CONVERTER(
        toolName = "Number Base",
        description = "Binary, hex, octal & decimal",
        icon = Icons.Outlined.Tag,
        category = ToolCategory.CONVERTERS,
        route = "tool_number_base",
    ),
    TIME_ZONE_CONVERTER(
        toolName = "Time Zones",
        description = "Compare world clocks",
        icon = Icons.Outlined.Public,
        category = ToolCategory.CONVERTERS,
        route = "tool_time_zones",
    ),
    COOKING_CONVERTER(
        toolName = "Cooking",
        description = "Cups, tbsp, grams & more",
        icon = Icons.Outlined.Kitchen,
        category = ToolCategory.CONVERTERS,
        route = "tool_cooking",
    ),
    CLOTHING_SIZE(
        toolName = "Clothing Size",
        description = "US, EU & UK sizes",
        icon = Icons.Outlined.Checkroom,
        category = ToolCategory.CONVERTERS,
        route = "tool_clothing_size",
    ),
    MORSE_CODE(
        toolName = "Morse Code",
        description = "Text ↔ morse translator",
        icon = Icons.Outlined.Code,
        category = ToolCategory.CONVERTERS,
        route = "tool_morse_code",
    ),

    // ── Generators ──
    QR_GENERATOR(
        toolName = "QR Generator",
        description = "Create QR codes",
        icon = Icons.Outlined.QrCode2,
        category = ToolCategory.GENERATORS,
        route = "tool_qr_generator",
    ),
    COLOR_PICKER(
        toolName = "Color Picker",
        description = "Pick & mix colors",
        icon = Icons.Outlined.ColorLens,
        category = ToolCategory.GENERATORS,
        route = "tool_color_picker",
    ),
    TEXT_TOOLS(
        toolName = "Text Tools",
        description = "Word count, case & more",
        icon = Icons.Outlined.TextFields,
        category = ToolCategory.GENERATORS,
        route = "tool_text_tools",
    ),
    FREQUENCY_GENERATOR(
        toolName = "Tone Generator",
        description = "Audio frequency 20Hz–20kHz",
        icon = Icons.Outlined.GraphicEq,
        category = ToolCategory.GENERATORS,
        route = "tool_frequency",
    ),

    // ── Scanners ──
    QR_SCANNER(
        toolName = "QR Scanner",
        description = "Scan barcodes & QR codes",
        icon = Icons.Outlined.QrCodeScanner,
        category = ToolCategory.SCANNERS,
        route = "tool_qr_scanner",
    ),
    DOCUMENT_SCANNER(
        toolName = "Doc Scanner",
        description = "Scan documents to images",
        icon = Icons.Outlined.DocumentScanner,
        category = ToolCategory.SCANNERS,
        route = "tool_doc_scanner",
    ),

    // ── Medical ──
    BMI_CALCULATOR(
        toolName = "BMI Calculator",
        description = "Body Mass Index",
        icon = Icons.Outlined.FitnessCenter,
        category = ToolCategory.MEDICAL,
        route = "tool_bmi",
    ),
    BMR_CALCULATOR(
        toolName = "BMR / TDEE",
        description = "Metabolic rate & calories",
        icon = Icons.Outlined.LocalFireDepartment,
        category = ToolCategory.MEDICAL,
        route = "tool_bmr",
    ),
    HEART_RATE_ZONES(
        toolName = "Heart Rate Zones",
        description = "Training zones by HR",
        icon = Icons.Outlined.FavoriteBorder,
        category = ToolCategory.MEDICAL,
        route = "tool_heart_rate",
    ),
    BODY_FAT_CALCULATOR(
        toolName = "Body Fat %",
        description = "Navy method estimate",
        icon = Icons.Outlined.Accessibility,
        category = ToolCategory.MEDICAL,
        route = "tool_body_fat",
    ),
    BAC_CALCULATOR(
        toolName = "BAC Calculator",
        description = "Blood alcohol estimate",
        icon = Icons.Outlined.LocalBar,
        category = ToolCategory.MEDICAL,
        route = "tool_bac",
    ),
    DUE_DATE_CALCULATOR(
        toolName = "Due Date",
        description = "Pregnancy due date",
        icon = Icons.Outlined.ChildCare,
        category = ToolCategory.MEDICAL,
        route = "tool_due_date",
    ),
    DOSE_CALCULATOR(
        toolName = "Dose Calculator",
        description = "Weight-based dosing",
        icon = Icons.Outlined.MedicalServices,
        category = ToolCategory.MEDICAL,
        route = "tool_dose",
    ),
    IV_DRIP_RATE(
        toolName = "IV Drip Rate",
        description = "Drops/min & mL/hr",
        icon = Icons.Outlined.Opacity,
        category = ToolCategory.MEDICAL,
        route = "tool_iv_drip",
    ),
    WATER_INTAKE(
        toolName = "Water Intake",
        description = "Daily hydration target",
        icon = Icons.Outlined.LocalDrink,
        category = ToolCategory.MEDICAL,
        route = "tool_water_intake",
    ),
    IDEAL_BODY_WEIGHT(
        toolName = "Ideal Weight",
        description = "Devine, Robinson & more",
        icon = Icons.Outlined.Person,
        category = ToolCategory.MEDICAL,
        route = "tool_ideal_weight",
    ),
}
