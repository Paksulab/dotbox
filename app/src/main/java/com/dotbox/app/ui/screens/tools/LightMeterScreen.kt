package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlin.math.log10

private val LuxDark = Color(0xFF5C6BC0)
private val LuxDim = Color(0xFF42A5F5)
private val LuxIndoor = Color(0xFF66BB6A)
private val LuxBright = Color(0xFFFFB74D)
private val LuxSunlight = Color(0xFFEF5350)

private fun luxColor(lux: Float): Color = when {
    lux < 10f -> LuxDark
    lux < 50f -> LuxDim
    lux < 500f -> LuxIndoor
    lux < 10_000f -> LuxBright
    else -> LuxSunlight
}

private fun luxCategory(lux: Float): String = when {
    lux < 10f -> "Dark"
    lux < 50f -> "Dim"
    lux < 500f -> "Indoor"
    lux < 10_000f -> "Bright"
    else -> "Sunlight"
}

private fun luxFraction(lux: Float): Float {
    if (lux <= 0f) return 0f
    return (log10(lux + 1f) / log10(100_001f)).coerceIn(0f, 1f)
}

@Composable
fun LightMeterScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var currentLux by remember { mutableFloatStateOf(0f) }
    var minLux by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxLux by remember { mutableFloatStateOf(0f) }
    var totalLux by remember { mutableFloatStateOf(0f) }
    var sampleCount by remember { mutableIntStateOf(0) }
    var sensorAvailable by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val lux = event.values[0]
                    currentLux = lux
                    if (lux < minLux) minLux = lux
                    if (lux > maxLux) maxLux = lux
                    totalLux += lux
                    sampleCount++
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                lightSensor,
                SensorManager.SENSOR_DELAY_UI,
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val averageLux = if (sampleCount > 0) totalLux / sampleCount else 0f
    val displayMin = if (minLux == Float.MAX_VALUE) 0f else minLux

    ToolScreenScaffold(title = "Light Meter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!sensorAvailable) {
                Text(
                    text = "Light sensor not available on this device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            } else {
                // Arc gauge
                LuxGauge(lux = currentLux)

                Spacer(modifier = Modifier.height(4.dp))

                // Category label
                Text(
                    text = luxCategory(currentLux),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = luxColor(currentLux),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Min / Max / Average card
                LightSection("Readings") {
                    LightRow("Current", "${String.format("%.1f", currentLux)} lux")
                    LightRow("Minimum", "${String.format("%.1f", displayMin)} lux")
                    LightRow("Maximum", "${String.format("%.1f", maxLux)} lux")
                    LightRow("Average", "${String.format("%.1f", averageLux)} lux")
                    LightRow("Samples", "$sampleCount")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reset button
                Button(
                    onClick = {
                        minLux = currentLux
                        maxLux = currentLux
                        totalLux = currentLux
                        sampleCount = 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Reset Min / Max")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reference table
                LightSection("Reference") {
                    LuxReferenceRow(LuxDark, "Dark", "0 – 10 lux", "Moonlight, dark room")
                    LuxReferenceRow(LuxDim, "Dim", "10 – 50 lux", "Hallway, twilight")
                    LuxReferenceRow(LuxIndoor, "Indoor", "50 – 500 lux", "Office, living room")
                    LuxReferenceRow(LuxBright, "Bright", "500 – 10k lux", "Overcast sky, studio")
                    LuxReferenceRow(LuxSunlight, "Sunlight", "10k+ lux", "Direct sunlight")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LuxGauge(lux: Float) {
    val accentColor = luxColor(lux)
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val fraction = luxFraction(lux)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(
                color = bgColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Value arc
            if (fraction > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = 270f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (lux < 100f) String.format("%.1f", lux) else String.format("%.0f", lux),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = textColor,
            )
            Text(
                text = "lux",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LightSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun LightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LuxReferenceRow(
    color: Color,
    category: String,
    range: String,
    example: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
