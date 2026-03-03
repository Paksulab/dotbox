package com.dotbox.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
)

// Light scheme following Nothing's clean aesthetic
private val LightColorScheme = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = NothingBlack,
    secondary = NothingMediumGray,
    onSecondary = NothingWhite,
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = NothingBlack,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = NothingRedDark,
    background = NothingWhite,
    onBackground = NothingBlack,
    surface = Color(0xFFFAFAFA),
    onSurface = NothingBlack,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = NothingMediumGray,
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE0E0E0),
    error = NothingRed,
    onError = NothingWhite,
)

@Composable
fun DotBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DotBoxTypography,
        content = content
    )
}
