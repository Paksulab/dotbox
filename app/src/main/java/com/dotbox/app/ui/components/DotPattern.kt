package com.dotbox.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dotbox.app.ui.theme.NothingRed

@Composable
fun DotPattern(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    spacing: Float = 24f,
    radius: Float = 1.5f,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // ── Atmospheric radial glows (matching website) ──────────
        // Red glow: top-left (website: circle at 10% -10%, rgba(211,47,47,0.14))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NothingRed.copy(alpha = 0.12f),
                    NothingRed.copy(alpha = 0.04f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.1f, size.height * -0.05f),
                radius = size.width * 0.5f,
            ),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.1f, size.height * -0.05f),
        )

        // White glow: top-right (website: circle at 100% 0, rgba(255,255,255,0.07))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.06f),
                    Color.White.copy(alpha = 0.02f),
                    Color.Transparent,
                ),
                center = Offset(size.width, 0f),
                radius = size.width * 0.45f,
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width, 0f),
        )

        // ── Dot grid (noise texture) ────────────────────────────
        val cols = (size.width / spacing).toInt() + 1
        val rows = (size.height / spacing).toInt() + 1

        for (col in 0..cols) {
            for (row in 0..rows) {
                drawCircle(
                    color = dotColor,
                    radius = radius,
                    center = Offset(col * spacing, row * spacing),
                )
            }
        }
    }
}
