package com.dotbox.app.ui.screens.tools

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.screens.settings.hapticEnabled
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlin.math.abs

@Composable
fun RulerScreen(onBack: () -> Unit) {
    var useCm by rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current
    val view = LocalView.current
    val hapticOn = hapticEnabled(context)
    val displayMetrics = context.resources.displayMetrics
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Pixels per mm based on screen DPI
    val ydpi = displayMetrics.ydpi
    val pxPerMm = ydpi / 25.4f
    val pxPerInch = ydpi

    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    // Draggable measurement markers
    var markerStartY by remember { mutableFloatStateOf(0f) }
    var markerEndY by remember { mutableFloatStateOf(200f) }
    var draggingMarker by remember { mutableStateOf<String?>(null) } // "start", "end", or null
    var lastHapticBoundary by remember { mutableIntStateOf(-1) }

    // Measurement between markers
    val markerDistancePx = abs(markerEndY - markerStartY)
    val measurement = if (useCm) {
        val mm = markerDistancePx / pxPerMm
        if (mm >= 10f) "%.1f cm".format(mm / 10f) else "%.1f mm".format(mm)
    } else {
        val inches = markerDistancePx / pxPerInch
        "%.2f in".format(inches)
    }

    ToolScreenScaffold(title = "Ruler", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Unit toggle + measurement
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

                // Measurement display
                Text(
                    text = measurement,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = NothingRed,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "DPI: ${ydpi.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Ruler canvas with draggable markers
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Determine which marker to drag (within 30px)
                                    val distToStart = abs(offset.y - markerStartY)
                                    val distToEnd = abs(offset.y - markerEndY)
                                    draggingMarker = when {
                                        distToStart < 40f && distToStart <= distToEnd -> "start"
                                        distToEnd < 40f -> "end"
                                        else -> null
                                    }
                                    lastHapticBoundary = -1
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    when (draggingMarker) {
                                        "start" -> markerStartY = (markerStartY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                                        "end" -> markerEndY = (markerEndY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                                    }
                                    // Haptic at unit boundaries
                                    if (hapticOn && draggingMarker != null) {
                                        val currentBoundary = if (useCm) {
                                            (markerDistancePx / pxPerMm / 10f).toInt() // cm boundaries
                                        } else {
                                            (markerDistancePx / pxPerInch).toInt() // inch boundaries
                                        }
                                        if (currentBoundary != lastHapticBoundary && currentBoundary > 0) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            lastHapticBoundary = currentBoundary
                                        }
                                    }
                                },
                                onDragEnd = { draggingMarker = null },
                                onDragCancel = { draggingMarker = null },
                            )
                        },
                ) {
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

                    // Draw measurement markers
                    val minY = minOf(markerStartY, markerEndY)
                    val maxY = maxOf(markerStartY, markerEndY)

                    // Shaded measurement area
                    drawRect(
                        color = NothingRed.copy(alpha = 0.08f),
                        topLeft = Offset(0f, minY),
                        size = androidx.compose.ui.geometry.Size(size.width, maxY - minY),
                    )

                    // Start marker line
                    drawLine(
                        color = NothingRed,
                        start = Offset(0f, markerStartY),
                        end = Offset(size.width, markerStartY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)),
                    )

                    // End marker line
                    drawLine(
                        color = NothingRed,
                        start = Offset(0f, markerEndY),
                        end = Offset(size.width, markerEndY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)),
                    )

                    // Drag handle circles
                    val handleRadius = 8.dp.toPx()
                    drawCircle(
                        color = NothingRed,
                        radius = handleRadius,
                        center = Offset(size.width - 20.dp.toPx(), markerStartY),
                    )
                    drawCircle(
                        color = NothingRed,
                        radius = handleRadius,
                        center = Offset(size.width - 20.dp.toPx(), markerEndY),
                    )

                    // Measurement text between markers
                    val midY = (minY + maxY) / 2f
                    val measureStyle = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NothingRed,
                    )
                    val measureMeasured = textMeasurer.measure(measurement, measureStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = measurement,
                        style = measureStyle,
                        topLeft = Offset(
                            size.width - 20.dp.toPx() - measureMeasured.size.width / 2,
                            midY - measureMeasured.size.height / 2,
                        ),
                    )
                }
            }
        }
    }
}
