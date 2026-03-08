package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.delay

private data class Split(
    val lapNumber: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long,
)

@Composable
fun SplitTimerScreen(onBack: () -> Unit) {
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var elapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    var lastSplitMs by rememberSaveable { mutableLongStateOf(0L) }
    val splits = rememberSaveable(
        saver = listSaver<SnapshotStateList<Split>, String>(
            save = { list -> list.map { "${it.lapNumber},${it.lapTimeMs},${it.totalTimeMs}" } },
            restore = { saved ->
                saved.map { s ->
                    val parts = s.split(",")
                    Split(parts[0].toInt(), parts[1].toLong(), parts[2].toLong())
                }.toMutableStateList()
            },
        ),
    ) { mutableStateListOf() }

    // Timer tick
    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (true) {
                delay(10L)
                elapsedMs += 10L
            }
        }
    }

    // Find best/worst laps
    val bestLapIdx = if (splits.size > 1) splits.indices.minByOrNull { splits[it].lapTimeMs } else null
    val worstLapIdx = if (splits.size > 1) splits.indices.maxByOrNull { splits[it].lapTimeMs } else null

    ToolScreenScaffold(title = "Split Timer", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Main elapsed time
            Text(
                text = formatSplitTime(elapsedMs),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // Current lap time
            val currentLapMs = elapsedMs - lastSplitMs
            Text(
                text = "Lap ${splits.size + 1}  ${formatSplitTime(currentLapMs)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRunning) {
                    // Stop / Reset
                    FilledTonalIconButton(
                        onClick = {
                            isRunning = false
                            isPaused = false
                            elapsedMs = 0L
                            lastSplitMs = 0L
                            splits.clear()
                        },
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop", modifier = Modifier.size(28.dp))
                    }

                    // Pause / Resume
                    FilledIconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = NothingRed,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    // Split / Lap
                    FilledTonalIconButton(
                        onClick = {
                            if (!isPaused) {
                                val lapTime = elapsedMs - lastSplitMs
                                splits.add(
                                    Split(
                                        lapNumber = splits.size + 1,
                                        lapTimeMs = lapTime,
                                        totalTimeMs = elapsedMs,
                                    )
                                )
                                lastSplitMs = elapsedMs
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        enabled = !isPaused,
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = "Split", modifier = Modifier.size(28.dp))
                    }
                } else {
                    // Start
                    FilledIconButton(
                        onClick = {
                            if (elapsedMs == 0L) {
                                splits.clear()
                                lastSplitMs = 0L
                            }
                            isPaused = false
                            isRunning = true
                        },
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

            // Splits list
            if (splits.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Lap",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.8f),
                        )
                        Text(
                            text = "Lap Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        itemsIndexed(splits.reversed()) { reverseIndex, split ->
                            val actualIndex = splits.size - 1 - reverseIndex

                            if (reverseIndex > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }

                            val lapColor = when (actualIndex) {
                                bestLapIdx -> Color(0xFF66BB6A)
                                worstLapIdx -> NothingRed
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Lap ${split.lapNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = lapColor,
                                    modifier = Modifier.weight(0.8f),
                                )
                                Text(
                                    text = formatSplitTime(split.lapTimeMs),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = lapColor,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                )
                                Text(
                                    text = formatSplitTime(split.totalTimeMs),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatSplitTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    val ms = (millis % 1000) / 10
    return if (min > 0) "%d:%02d.%02d".format(min, sec, ms)
    else "%d.%02d".format(sec, ms)
}
