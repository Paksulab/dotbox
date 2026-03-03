package com.dotbox.app.ui.screens.tools

import android.util.DisplayMetrics
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed

@Composable
fun RulerScreen(onBack: () -> Unit) {
    var useCm by rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val density = LocalDensity.current

    // Pixels per mm based on screen DPI
    val ydpi = displayMetrics.ydpi
    val pxPerMm = ydpi / 25.4f
    val pxPerInch = ydpi

    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    ToolScreenScaffold(title = "Ruler", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Unit toggle
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = { useCm = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (useCm) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Text(
                        text = "cm",
                        color = if (useCm) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { useCm = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (!useCm) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Text(
                        text = "inch",
                        color = if (!useCm) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "DPI: ${ydpi.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Ruler canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rulerWidth = 80.dp.toPx()
                    val startX = 0f

                    // Background line
                    drawLine(
                        color = outline.copy(alpha = 0.3f),
                        start = Offset(startX + rulerWidth, 0f),
                        end = Offset(startX + rulerWidth, size.height),
                        strokeWidth = 1f,
                    )

                    if (useCm) {
                        // Metric ruler
                        val totalMm = (size.height / pxPerMm).toInt()
                        for (mm in 0..totalMm) {
                            val y = mm * pxPerMm
                            val isCm = mm % 10 == 0
                            val isHalfCm = mm % 5 == 0

                            val tickLength = when {
                                isCm -> rulerWidth
                                isHalfCm -> rulerWidth * 0.6f
                                else -> rulerWidth * 0.3f
                            }
                            val tickWidth = when {
                                isCm -> 2.5f
                                isHalfCm -> 1.5f
                                else -> 1f
                            }
                            val tickColor = when {
                                isCm -> onSurface
                                isHalfCm -> onSurface.copy(alpha = 0.7f)
                                else -> outline.copy(alpha = 0.5f)
                            }

                            drawLine(
                                color = tickColor,
                                start = Offset(startX + rulerWidth - tickLength, y),
                                end = Offset(startX + rulerWidth, y),
                                strokeWidth = tickWidth,
                                cap = StrokeCap.Round,
                            )

                            if (isCm && mm > 0) {
                                val label = "${mm / 10}"
                                val style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = onSurface,
                                )
                                val measured = textMeasurer.measure(label, style)
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = label,
                                    style = style,
                                    topLeft = Offset(
                                        startX + rulerWidth + 8.dp.toPx(),
                                        y - measured.size.height / 2,
                                    ),
                                )
                            }
                        }
                    } else {
                        // Imperial ruler (1/16 inch divisions)
                        val totalSixteenths = (size.height / (pxPerInch / 16f)).toInt()
                        for (s in 0..totalSixteenths) {
                            val y = s * (pxPerInch / 16f)
                            val isInch = s % 16 == 0
                            val isHalf = s % 8 == 0
                            val isQuarter = s % 4 == 0
                            val isEighth = s % 2 == 0

                            val tickLength = when {
                                isInch -> rulerWidth
                                isHalf -> rulerWidth * 0.7f
                                isQuarter -> rulerWidth * 0.5f
                                isEighth -> rulerWidth * 0.35f
                                else -> rulerWidth * 0.2f
                            }
                            val tickWidth = when {
                                isInch -> 2.5f
                                isHalf -> 1.5f
                                else -> 1f
                            }
                            val tickColor = when {
                                isInch -> onSurface
                                isHalf -> onSurface.copy(alpha = 0.7f)
                                else -> outline.copy(alpha = 0.5f)
                            }

                            drawLine(
                                color = tickColor,
                                start = Offset(startX + rulerWidth - tickLength, y),
                                end = Offset(startX + rulerWidth, y),
                                strokeWidth = tickWidth,
                                cap = StrokeCap.Round,
                            )

                            if (isInch && s > 0) {
                                val label = "${s / 16}"
                                val style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = onSurface,
                                )
                                val measured = textMeasurer.measure(label, style)
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = label,
                                    style = style,
                                    topLeft = Offset(
                                        startX + rulerWidth + 8.dp.toPx(),
                                        y - measured.size.height / 2,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
