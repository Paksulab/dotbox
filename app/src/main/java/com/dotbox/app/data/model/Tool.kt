package com.dotbox.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dotbox.app.ui.theme.AccentConvert
import com.dotbox.app.ui.theme.AccentGenerate
import com.dotbox.app.ui.theme.AccentMeasure
import com.dotbox.app.ui.theme.AccentScan
import com.dotbox.app.ui.theme.AccentUtility

enum class ToolCategory(
    val displayName: String,
    val accentColor: Color,
) {
    UTILITIES("Utilities", AccentUtility),
    MEASUREMENT("Measurement", AccentMeasure),
    CONVERTERS("Converters", AccentConvert),
    GENERATORS("Generators", AccentGenerate),
    SCANNERS("Scanners", AccentScan),
}

enum class ToolId(
    val toolName: String,
    val description: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val route: String,
) {
    CALCULATOR(
        toolName = "Calculator",
        description = "Scientific calculator",
        icon = Icons.Outlined.Calculate,
        category = ToolCategory.UTILITIES,
        route = "tool_calculator",
    ),
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
    NOTES(
        toolName = "Notes",
        description = "Quick scratch pad",
        icon = Icons.Outlined.EditNote,
        category = ToolCategory.UTILITIES,
        route = "tool_notes",
    ),
    RANDOM_GENERATOR(
        toolName = "Random Generator",
        description = "Dice, coins, passwords & more",
        icon = Icons.Outlined.Casino,
        category = ToolCategory.UTILITIES,
        route = "tool_random_generator",
    ),
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
}
