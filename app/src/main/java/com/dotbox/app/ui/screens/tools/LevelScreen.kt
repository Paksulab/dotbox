package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlin.math.abs

@Composable
fun LevelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Low-pass filter for smooth values
                val alpha = 0.15f
                pitch = pitch + alpha * (event.values[1] - pitch)
                roll = roll + alpha * (event.values[0] - roll)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Convert to degrees (approximate)
    val pitchDegrees = (pitch / SensorManager.GRAVITY_EARTH * 90f).coerceIn(-90f, 90f)
    val rollDegrees = (roll / SensorManager.GRAVITY_EARTH * 90f).coerceIn(-90f, 90f)

    val isLevel = abs(pitchDegrees) < 1.5f && abs(rollDegrees) < 1.5f

    val animatedPitch by animateFloatAsState(
        targetValue = pitchDegrees,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "pitch",
    )
    val animatedRoll by animateFloatAsState(
        targetValue = rollDegrees,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "roll",
    )

    val bubbleColor by animateColorAsState(
        targetValue = if (isLevel) Color(0xFF4CAF50) else NothingRed,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bubbleColor",
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    ToolScreenScaffold(title = "Level", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Angle display
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PITCH",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.1f°".format(pitchDegrees),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROLL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.1f°".format(rollDegrees),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLevel) "LEVEL" else "NOT LEVEL",
                style = MaterialTheme.typography.titleMedium,
                color = bubbleColor,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2D bubble level
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.minDimension / 2 - 16.dp.toPx()

                    // Outer ring
                    drawCircle(
                        color = outline.copy(alpha = 0.3f),
                        radius = maxRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                    )

                    // Middle ring
                    drawCircle(
                        color = outline.copy(alpha = 0.2f),
                        radius = maxRadius * 0.66f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                    )

                    // Inner ring (level zone)
                    drawCircle(
                        color = outline.copy(alpha = 0.15f),
                        radius = maxRadius * 0.33f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                    )

                    // Crosshair
                    drawLine(
                        color = outline.copy(alpha = 0.2f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawLine(
                        color = outline.copy(alpha = 0.2f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx(),
                    )

                    // Center target dot
                    drawCircle(
                        color = outline.copy(alpha = 0.5f),
                        radius = 3.dp.toPx(),
                        center = center,
                    )

                    // Bubble position
                    val bubbleX = center.x + (animatedRoll / 45f) * maxRadius * 0.8f
                    val bubbleY = center.y + (animatedPitch / 45f) * maxRadius * 0.8f
                    val bubblePos = Offset(
                        bubbleX.coerceIn(center.x - maxRadius + 20.dp.toPx(), center.x + maxRadius - 20.dp.toPx()),
                        bubbleY.coerceIn(center.y - maxRadius + 20.dp.toPx(), center.y + maxRadius - 20.dp.toPx()),
                    )

                    // Bubble shadow
                    drawCircle(
                        color = bubbleColor.copy(alpha = 0.15f),
                        radius = 24.dp.toPx(),
                        center = bubblePos,
                    )

                    // Bubble
                    drawCircle(
                        color = bubbleColor,
                        radius = 16.dp.toPx(),
                        center = bubblePos,
                    )

                    // Bubble highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = 6.dp.toPx(),
                        center = Offset(bubblePos.x - 4.dp.toPx(), bubblePos.y - 4.dp.toPx()),
                    )
                }
            }
        }
    }
}
