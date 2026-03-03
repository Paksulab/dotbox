package com.dotbox.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun DotPattern(
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    spacing: Float = 24f,
    radius: Float = 1.5f,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
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
