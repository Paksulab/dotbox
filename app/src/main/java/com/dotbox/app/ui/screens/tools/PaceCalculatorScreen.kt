package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono

private enum class PaceUnit(val label: String) {
    KM("km"),
    MI("mi"),
}

private data class RaceSplit(
    val name: String,
    val distanceKm: Double,
    val color: Color,
)

private val raceSplits = listOf(
    RaceSplit("1K", 1.0, Color(0xFF66BB6A)),
    RaceSplit("5K", 5.0, Color(0xFF4DD0E1)),
    RaceSplit("10K", 10.0, Color(0xFFFFB74D)),
    RaceSplit("Half Marathon", 21.0975, Color(0xFFEF5350)),
    RaceSplit("Marathon", 42.195, Color(0xFFD32F2F)),
)

@Composable
fun PaceCalculatorScreen(onBack: () -> Unit) {
    var distanceInput by rememberSaveable { mutableStateOf("") }
    var hoursInput by rememberSaveable { mutableStateOf("") }
    var minutesInput by rememberSaveable { mutableStateOf("") }
    var secondsInput by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(PaceUnit.KM.name) }

    val selectedUnit = PaceUnit.valueOf(unit)
    val distanceKm = distanceInput.toDoubleOrNull()?.let {
        if (selectedUnit == PaceUnit.MI) it * 1.60934 else it
    }
    val totalSeconds = (hoursInput.toIntOrNull() ?: 0) * 3600 +
            (minutesInput.toIntOrNull() ?: 0) * 60 +
            (secondsInput.toIntOrNull() ?: 0)

    ToolScreenScaffold(title = "Pace Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Unit toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaceUnit.entries.forEach { u ->
                    FilterChip(
                        selected = selectedUnit == u,
                        onClick = { unit = u.name },
                        label = { Text(u.label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Distance input
            OutlinedTextField(
                value = distanceInput,
                onValueChange = { distanceInput = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Distance") },
                placeholder = { Text("e.g. 10") },
                suffix = { Text(selectedUnit.label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time inputs
            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = hoursInput,
                    onValueChange = { hoursInput = it.filter { c -> c.isDigit() }.take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Hours") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                OutlinedTextField(
                    value = minutesInput,
                    onValueChange = { minutesInput = it.filter { c -> c.isDigit() }.take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                OutlinedTextField(
                    value = secondsInput,
                    onValueChange = { secondsInput = it.filter { c -> c.isDigit() }.take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Sec") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (distanceKm != null && distanceKm > 0 && totalSeconds > 0) {
                // Pace result
                val paceSecPerKm = totalSeconds / distanceKm
                val paceSecPerMi = paceSecPerKm * 1.60934
                val speedKmh = distanceKm / totalSeconds * 3600
                val speedMph = speedKmh / 1.60934

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp)
                        .animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Pace",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val displayPace = if (selectedUnit == PaceUnit.KM) paceSecPerKm else paceSecPerMi
                    Text(
                        text = formatPace(displayPace.toInt()),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "per ${selectedUnit.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f".format(speedKmh),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "km/h",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f".format(speedMph),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "mph",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Race predictions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "RACE PREDICTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    raceSplits.forEach { race ->
                        val predictedSeconds = (paceSecPerKm * race.distanceKm).toInt()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .height(12.dp)
                                        .width(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(race.color),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = race.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = formatDuration(predictedSeconds),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatPace(totalSeconds: Int): String {
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    return "%d:%02d".format(min, sec)
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val min = (totalSeconds % 3600) / 60
    val sec = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, min, sec)
    else "%d:%02d".format(min, sec)
}
