package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.delay

private enum class PomodoroMode(val label: String, val durationMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15),
}

@Suppress("DEPRECATION")
private fun vibrateCompletion(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 400), -1)
        )
    } else {
        vibrator.vibrate(longArrayOf(0, 200, 100, 200, 100, 400), -1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var selectedMode by rememberSaveable { mutableStateOf(PomodoroMode.FOCUS) }
    var timeRemainingSeconds by rememberSaveable {
        mutableIntStateOf(PomodoroMode.FOCUS.durationMinutes * 60)
    }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var completedSessions by rememberSaveable { mutableIntStateOf(0) }
    var currentSession by rememberSaveable { mutableIntStateOf(1) }

    val totalSeconds = selectedMode.durationMinutes * 60
    val progress = if (totalSeconds > 0) timeRemainingSeconds.toFloat() / totalSeconds else 0f

    val breakGreen = Color(0xFF66BB6A)
    val arcColor = when (selectedMode) {
        PomodoroMode.FOCUS -> MaterialTheme.colorScheme.tertiary
        PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> breakGreen
    }

    // Countdown logic
    LaunchedEffect(isRunning, timeRemainingSeconds) {
        if (isRunning && timeRemainingSeconds > 0) {
            delay(1000L)
            timeRemainingSeconds -= 1
        } else if (isRunning && timeRemainingSeconds == 0) {
            // Timer completed
            isRunning = false
            vibrateCompletion(context)

            // Auto-advance logic
            when (selectedMode) {
                PomodoroMode.FOCUS -> {
                    completedSessions += 1
                    if (completedSessions % 4 == 0) {
                        // Every 4th focus session -> long break
                        selectedMode = PomodoroMode.LONG_BREAK
                        timeRemainingSeconds = PomodoroMode.LONG_BREAK.durationMinutes * 60
                    } else {
                        selectedMode = PomodoroMode.SHORT_BREAK
                        timeRemainingSeconds = PomodoroMode.SHORT_BREAK.durationMinutes * 60
                    }
                }
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                    currentSession = completedSessions + 1
                    selectedMode = PomodoroMode.FOCUS
                    timeRemainingSeconds = PomodoroMode.FOCUS.durationMinutes * 60
                }
            }
        }
    }

    fun switchMode(mode: PomodoroMode) {
        isRunning = false
        selectedMode = mode
        timeRemainingSeconds = mode.durationMinutes * 60
    }

    fun resetTimer() {
        isRunning = false
        timeRemainingSeconds = selectedMode.durationMinutes * 60
    }

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timerText = "%02d:%02d".format(minutes, seconds)

    val sessionDisplay = if (selectedMode == PomodoroMode.FOCUS) {
        "Session ${completedSessions + 1} of 4"
    } else {
        "Session $currentSession of 4 — Break"
    }

    ToolScreenScaffold(title = "Pomodoro Timer", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Mode selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                PomodoroMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { switchMode(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selectedMode == mode,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Circular timer gauge
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                val bgArcColor = MaterialTheme.colorScheme.outline

                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc
                    drawArc(
                        color = bgArcColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    // Progress arc
                    drawArc(
                        color = arcColor,
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = selectedMode.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = arcColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session counter
            Text(
                text = sessionDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Completed count
            if (completedSessions > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedSessions focus session${if (completedSessions != 1) "s" else ""} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                )
            }

            // Auto-advance hint when timer just completed
            if (!isRunning && timeRemainingSeconds == selectedMode.durationMinutes * 60 && completedSessions > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val hint = when (selectedMode) {
                    PomodoroMode.LONG_BREAK -> "Great work! Time for a long break."
                    PomodoroMode.SHORT_BREAK -> "Nice focus! Take a short break."
                    PomodoroMode.FOCUS -> "Break over — ready to focus!"
                }
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = arcColor,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Start / Pause button (filled)
                Button(
                    onClick = {
                        if (timeRemainingSeconds == 0) {
                            // Reset if at zero before starting
                            timeRemainingSeconds = selectedMode.durationMinutes * 60
                        }
                        isRunning = !isRunning
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = arcColor,
                        contentColor = if (selectedMode == PomodoroMode.FOCUS) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            Color.Black
                        },
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = if (isRunning) "Pause" else "Start",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Reset button (outlined)
                OutlinedButton(
                    onClick = { resetTimer() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
            ) {
                Text(
                    text = "How It Works",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                PomodoroInfoRow("1.", "Focus for 25 minutes")
                PomodoroInfoRow("2.", "Take a 5-min short break")
                PomodoroInfoRow("3.", "Repeat 4 times")
                PomodoroInfoRow("4.", "Take a 15-min long break")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PomodoroInfoRow(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
