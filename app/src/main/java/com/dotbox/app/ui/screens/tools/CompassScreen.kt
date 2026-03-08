package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val hapticOn = hapticEnabled(context)
    var azimuth by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    DisposableEffect(Unit) {
        val accelerometerValues = FloatArray(3)
        val magnetometerValues = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelerometerValues, 0, 3)
                    Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, magnetometerValues, 0, 3)
                }

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerValues)) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuth < 0) azimuth += 360f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    sensorAccuracy = accuracy
                }
            }
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "compassRotation",
    )

    val direction = when {
        azimuth >= 337.5f || azimuth < 22.5f -> "N"
        azimuth >= 22.5f && azimuth < 67.5f -> "NE"
        azimuth >= 67.5f && azimuth < 112.5f -> "E"
        azimuth >= 112.5f && azimuth < 157.5f -> "SE"
        azimuth >= 157.5f && azimuth < 202.5f -> "S"
        azimuth >= 202.5f && azimuth < 247.5f -> "SW"
        azimuth >= 247.5f && azimuth < 292.5f -> "W"
        azimuth >= 292.5f && azimuth < 337.5f -> "NW"
        else -> "N"
    }

    // Accuracy indicators
    val accuracyColor = when (sensorAccuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF4CAF50) // Green
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFFFC107) // Amber
        else -> NothingRed // Red for LOW or UNRELIABLE
    }
    val accuracyLabel = when (sensorAccuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High accuracy"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium accuracy"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low accuracy"
        else -> "Unreliable"
    }
    val needsCalibration = sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW

    // Haptic feedback at cardinal directions
    var lastCardinal by remember { mutableStateOf("") }
    LaunchedEffect(direction) {
        if (hapticOn && direction in listOf("N", "E", "S", "W") && direction != lastCardinal) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        lastCardinal = direction
    }

    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    ToolScreenScaffold(title = "Compass", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Calibration banner
            AnimatedVisibility(
                visible = needsCalibration,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                CalibrationBanner()
            }

            // Accuracy indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accuracyColor),
                )
                Text(
                    text = accuracyLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Degree display
            Text(
                text = "${azimuth.toInt()}°",
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = direction,
                    style = MaterialTheme.typography.headlineMedium,
                    color = NothingRed,
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Compass", "Bearing: ${azimuth.toInt()}° $direction"),
                        )
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy bearing",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Compass dial
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 16.dp.toPx()

                    // Outer circle
                    drawCircle(
                        color = outline.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                    )

                    // Rotate the compass rose
                    rotate(animatedAzimuth, center) {
                        // Tick marks
                        for (i in 0 until 360 step 5) {
                            val angle = Math.toRadians(i.toDouble())
                            val isCardinal = i % 90 == 0
                            val isMajor = i % 30 == 0
                            val tickLength = when {
                                isCardinal -> 20.dp.toPx()
                                isMajor -> 12.dp.toPx()
                                else -> 6.dp.toPx()
                            }
                            val tickWidth = if (isCardinal) 3f else if (isMajor) 2f else 1f
                            val tickColor = when {
                                i == 0 -> NothingRed
                                isCardinal -> onSurface
                                else -> outline.copy(alpha = 0.5f)
                            }

                            val outerX = center.x + (radius - 4.dp.toPx()) * sin(angle).toFloat()
                            val outerY = center.y - (radius - 4.dp.toPx()) * cos(angle).toFloat()
                            val innerX = center.x + (radius - 4.dp.toPx() - tickLength) * sin(angle).toFloat()
                            val innerY = center.y - (radius - 4.dp.toPx() - tickLength) * cos(angle).toFloat()

                            drawLine(
                                color = tickColor,
                                start = Offset(innerX, innerY),
                                end = Offset(outerX, outerY),
                                strokeWidth = tickWidth,
                                cap = StrokeCap.Round,
                            )
                        }

                        // Cardinal labels
                        val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                        labels.forEach { (label, angle) ->
                            val rad = Math.toRadians(angle.toDouble())
                            val labelRadius = radius - 40.dp.toPx()
                            val x = center.x + labelRadius * sin(rad).toFloat()
                            val y = center.y - labelRadius * cos(rad).toFloat()

                            val style = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (label == "N") NothingRed else onSurface,
                            )
                            val measured = textMeasurer.measure(label, style)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = label,
                                style = style,
                                topLeft = Offset(
                                    x - measured.size.width / 2,
                                    y - measured.size.height / 2,
                                ),
                            )
                        }

                        // North pointer (red)
                        val pointerLength = radius - 60.dp.toPx()
                        drawLine(
                            color = NothingRed,
                            start = center,
                            end = Offset(center.x, center.y - pointerLength),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                        // South pointer (gray)
                        drawLine(
                            color = outline,
                            start = center,
                            end = Offset(center.x, center.y + pointerLength),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }

                    // Center dot
                    drawCircle(
                        color = onSurface,
                        radius = 4.dp.toPx(),
                        center = center,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalibrationBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "figure8")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "figure8Phase",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NothingRed.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Calibration needed",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = NothingRed,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Move your phone in a figure-8 pattern",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Animated figure-8 guide
        Canvas(
            modifier = Modifier
                .size(width = 120.dp, height = 60.dp),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = size.width / 2f - 8.dp.toPx()
            val ry = size.height / 2f - 8.dp.toPx()

            // Draw figure-8 path
            val points = 100
            for (i in 0 until points) {
                val t = i.toFloat() / points * 2f * Math.PI.toFloat()
                val x = cx + rx * sin(t)
                val y = cy + ry * sin(2f * t) / 2f
                val nextT = (i + 1).toFloat() / points * 2f * Math.PI.toFloat()
                val nx = cx + rx * sin(nextT)
                val ny = cy + ry * sin(2f * nextT) / 2f

                drawLine(
                    color = NothingRed.copy(alpha = 0.3f),
                    start = Offset(x, y),
                    end = Offset(nx, ny),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            // Animated dot on the path
            val t = phase * 2f * Math.PI.toFloat()
            val dotX = cx + rx * sin(t)
            val dotY = cy + ry * sin(2f * t) / 2f
            drawCircle(
                color = NothingRed,
                radius = 6.dp.toPx(),
                center = Offset(dotX, dotY),
            )
        }
    }
}
