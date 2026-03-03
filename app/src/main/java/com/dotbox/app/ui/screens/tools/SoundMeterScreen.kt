package com.dotbox.app.ui.screens.tools

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.log10

@Composable
fun SoundMeterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var currentDb by remember { mutableDoubleStateOf(0.0) }
    var maxDb by remember { mutableDoubleStateOf(0.0) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Check permission on resume
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(isRecording) {
        if (!isRecording || !hasPermission) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val bufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )

            val audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                )
            } catch (e: SecurityException) {
                hasPermission = false
                return@withContext
            }

            try {
                audioRecord.startRecording()
                val buffer = ShortArray(bufferSize)

                while (isActive && isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += abs(buffer[i].toDouble())
                        }
                        val amplitude = sum / read

                        val db = if (amplitude > 0) {
                            20 * log10(amplitude / Short.MAX_VALUE) + 90
                        } else {
                            0.0
                        }

                        val clampedDb = db.coerceIn(0.0, 120.0)
                        currentDb = currentDb + 0.3 * (clampedDb - currentDb)
                        if (currentDb > maxDb) maxDb = currentDb
                    }
                    delay(50)
                }
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { isRecording = false }
    }

    val animatedDb by animateFloatAsState(
        targetValue = currentDb.toFloat(),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "db",
    )

    val dbColor by animateColorAsState(
        targetValue = when {
            currentDb < 40 -> Color(0xFF4CAF50)
            currentDb < 70 -> Color(0xFFFFC107)
            currentDb < 85 -> Color(0xFFFF9800)
            else -> NothingRed
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dbColor",
    )

    val levelLabel = when {
        currentDb < 30 -> "Quiet"
        currentDb < 50 -> "Moderate"
        currentDb < 70 -> "Loud"
        currentDb < 85 -> "Very Loud"
        currentDb < 100 -> "Dangerous"
        else -> "Extreme"
    }

    val outline = MaterialTheme.colorScheme.outline

    ToolScreenScaffold(title = "Sound Meter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!hasPermission) {
                Text(
                    text = "Microphone permission required",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Please grant microphone permission in Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Arc gauge
                Canvas(modifier = Modifier.size(240.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val padding = strokeWidth / 2 + 8.dp.toPx()
                    val arcSize = Size(size.width - padding * 2, size.height - padding * 2)

                    // Background arc
                    drawArc(
                        color = outline.copy(alpha = 0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(padding, padding),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Value arc
                    val sweep = (animatedDb / 120f) * 270f
                    drawArc(
                        color = dbColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(padding, padding),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // dB value
                Text(
                    text = "%.0f".format(currentDb),
                    style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMono),
                    color = dbColor,
                )
                Text(
                    text = "dB",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = levelLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = dbColor,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Max: %.0f dB".format(maxDb),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Start/Stop button
                Button(
                    onClick = {
                        if (isRecording) {
                            isRecording = false
                        } else {
                            maxDb = 0.0
                            currentDb = 0.0
                            isRecording = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) NothingRed else MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                ) {
                    Text(
                        text = if (isRecording) "Stop" else "Start",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
