package com.dotbox.app.ui.screens.tools

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.delay

private enum class TimerPreset(
    val label: String,
    val workSec: Int,
    val restSec: Int,
    val rounds: Int,
) {
    TABATA("Tabata", 20, 10, 8),
    HIIT("HIIT", 40, 20, 10),
    EMOM("EMOM", 60, 0, 10),
    CUSTOM("Custom", 30, 15, 8),
}

private enum class IntervalPhase { WORK, REST }

@Composable
fun WorkoutTimerScreen(onBack: () -> Unit) {
    var selectedPreset by rememberSaveable { mutableStateOf(TimerPreset.TABATA.name) }
    var customWorkSec by rememberSaveable { mutableStateOf("30") }
    var customRestSec by rememberSaveable { mutableStateOf("15") }
    var customRounds by rememberSaveable { mutableStateOf("8") }

    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var isComplete by rememberSaveable { mutableStateOf(false) }
    var currentRound by rememberSaveable { mutableIntStateOf(1) }
    var phase by rememberSaveable { mutableStateOf(IntervalPhase.WORK) }
    var remainingMs by rememberSaveable { mutableLongStateOf(0L) }

    val view = LocalView.current

    val preset = TimerPreset.valueOf(selectedPreset)
    val workSec = if (preset == TimerPreset.CUSTOM) customWorkSec.toIntOrNull() ?: 30 else preset.workSec
    val restSec = if (preset == TimerPreset.CUSTOM) customRestSec.toIntOrNull() ?: 15 else preset.restSec
    val totalRounds = if (preset == TimerPreset.CUSTOM) customRounds.toIntOrNull() ?: 8 else preset.rounds

    val currentPhaseTotalMs = when (phase) {
        IntervalPhase.WORK -> workSec * 1000L
        IntervalPhase.REST -> restSec * 1000L
    }

    val progress by animateFloatAsState(
        targetValue = when {
            isComplete -> 1f
            currentPhaseTotalMs > 0 -> remainingMs.toFloat() / currentPhaseTotalMs
            else -> 0f
        },
        animationSpec = tween(100),
        label = "progress",
    )

    val completeColor = Color(0xFF66BB6A)

    val phaseColor by animateColorAsState(
        targetValue = when {
            isComplete -> completeColor
            phase == IntervalPhase.WORK -> NothingRed
            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
        },
        label = "phaseColor",
    )

    // Shared phase-advance logic (used by timer tick AND skip button)
    fun advancePhase() {
        when (phase) {
            IntervalPhase.WORK -> {
                if (restSec > 0) {
                    phase = IntervalPhase.REST
                    remainingMs = restSec * 1000L
                } else if (currentRound < totalRounds) {
                    currentRound++
                    remainingMs = workSec * 1000L
                } else {
                    isRunning = false
                    isComplete = true
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
            IntervalPhase.REST -> {
                if (currentRound < totalRounds) {
                    currentRound++
                    phase = IntervalPhase.WORK
                    remainingMs = workSec * 1000L
                } else {
                    isRunning = false
                    isComplete = true
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
        }
    }

    // Timer tick
    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (remainingMs > 0) {
                delay(50L)
                remainingMs -= 50L
            }
            if (isRunning) {
                advancePhase()
            }
        }
    }

    fun startTimer() {
        currentRound = 1
        phase = IntervalPhase.WORK
        remainingMs = workSec * 1000L
        isPaused = false
        isComplete = false
        isRunning = true
    }

    fun resetTimer() {
        isRunning = false
        isPaused = false
        isComplete = false
        remainingMs = 0L
    }

    ToolScreenScaffold(title = "Workout Timer", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Preset chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TimerPreset.entries.forEach { p ->
                    FilterChip(
                        selected = preset == p,
                        onClick = {
                            selectedPreset = p.name
                            resetTimer()
                        },
                        label = { Text(p.label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        enabled = !isRunning,
                    )
                }
            }

            // Custom inputs
            if (preset == TimerPreset.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = customWorkSec,
                        onValueChange = { customWorkSec = it.filter { c -> c.isDigit() }.take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Work") },
                        suffix = { Text("s") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        enabled = !isRunning,
                    )
                    OutlinedTextField(
                        value = customRestSec,
                        onValueChange = { customRestSec = it.filter { c -> c.isDigit() }.take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Rest") },
                        suffix = { Text("s") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        enabled = !isRunning,
                    )
                    OutlinedTextField(
                        value = customRounds,
                        onValueChange = { customRounds = it.filter { c -> c.isDigit() }.take(2) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Rounds") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        enabled = !isRunning,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Background ring
                    drawArc(
                        color = phaseColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Progress arc
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = arcOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when {
                            isComplete -> "COMPLETE!"
                            isRunning || isPaused -> when (phase) {
                                IntervalPhase.WORK -> "WORK"
                                IntervalPhase.REST -> "REST"
                            }
                            else -> preset.label.uppercase()
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = phaseColor,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isComplete) {
                            val totalSec = totalRounds * (workSec + restSec)
                            formatTimerSeconds(totalSec * 1000L)
                        } else {
                            formatTimerSeconds(remainingMs)
                        },
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isComplete) completeColor else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isComplete -> "$totalRounds rounds done"
                            isRunning || isPaused -> "Round $currentRound / $totalRounds"
                            else -> "${workSec}s / ${restSec}s × $totalRounds"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRunning) {
                    // Stop
                    FilledTonalIconButton(
                        onClick = { resetTimer() },
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop", modifier = Modifier.size(28.dp))
                    }

                    // Pause / Resume
                    FilledIconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = phaseColor,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    // Skip
                    FilledTonalIconButton(
                        onClick = { advancePhase() },
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Skip", modifier = Modifier.size(28.dp))
                    }
                } else if (isComplete) {
                    // Reset after completion
                    FilledTonalIconButton(
                        onClick = { resetTimer() },
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", modifier = Modifier.size(28.dp))
                    }

                    // Restart
                    FilledIconButton(
                        onClick = { startTimer() },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = NothingRed,
                        ),
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Restart",
                            modifier = Modifier.size(36.dp),
                        )
                    }
                } else {
                    // Start
                    FilledIconButton(
                        onClick = { startTimer() },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = NothingRed,
                        ),
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            if (!isRunning && !isComplete) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Total Workout",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalSec = totalRounds * (workSec + restSec)
                    Text(
                        text = "${totalSec / 60}m ${totalSec % 60}s  ·  $totalRounds rounds",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatTimerSeconds(millis: Long): String {
    val totalSec = (millis.coerceAtLeast(0) + 999) / 1000 // ceiling
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
