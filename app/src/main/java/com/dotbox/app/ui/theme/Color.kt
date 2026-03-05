package com.dotbox.app.ui.theme

import androidx.compose.ui.graphics.Color

// Nothing-inspired monochrome palette
val NothingBlack = Color(0xFF0A0A0A)
val NothingDarkGray = Color(0xFF141414)
val NothingCardGray = Color(0xFF1A1A1A)
val NothingBorderGray = Color(0xFF2A2A2A)
val NothingMediumGray = Color(0xFF3A3A3A)
val NothingLightGray = Color(0xFF8A8A8A)
val NothingOffWhite = Color(0xFFE0E0E0)
val NothingWhite = Color(0xFFF5F5F5)

// Nothing accent red
val NothingRed = Color(0xFFD32F2F)
val NothingRedLight = Color(0xFFEF5350)
val NothingRedDark = Color(0xFFB71C1C)

// Category accent colors (muted, Nothing-style)
val AccentUtility = Color(0xFFE0E0E0)    // White/light for utilities
val AccentMeasure = Color(0xFFD32F2F)    // Nothing red for measurement
val AccentConvert = Color(0xFF78909C)    // Blue-gray for converters
val AccentGenerate = Color(0xFF66BB6A)   // Muted green for generators
val AccentScan = Color(0xFFFFB74D)       // Warm amber for scanners
val AccentCalculator = Color(0xFF4DD0E1) // Cyan for calculators
val AccentMedical = Color(0xFFEF9A9A)   // Soft rose for medical

// Material 3 dark color scheme mapped to Nothing aesthetic
val DarkPrimary = NothingWhite
val DarkOnPrimary = NothingBlack
val DarkPrimaryContainer = NothingCardGray
val DarkOnPrimaryContainer = NothingWhite

val DarkSecondary = NothingLightGray
val DarkOnSecondary = NothingBlack
val DarkSecondaryContainer = NothingBorderGray
val DarkOnSecondaryContainer = NothingOffWhite

val DarkTertiary = NothingRed
val DarkOnTertiary = NothingWhite
val DarkTertiaryContainer = NothingRedDark
val DarkOnTertiaryContainer = NothingWhite

val DarkBackground = NothingBlack
val DarkOnBackground = NothingWhite
val DarkSurface = NothingDarkGray
val DarkOnSurface = NothingWhite
val DarkSurfaceVariant = NothingCardGray
val DarkOnSurfaceVariant = NothingLightGray
val DarkOutline = NothingBorderGray
val DarkOutlineVariant = NothingMediumGray

val DarkError = Color(0xFFCF6679)
val DarkOnError = NothingBlack
