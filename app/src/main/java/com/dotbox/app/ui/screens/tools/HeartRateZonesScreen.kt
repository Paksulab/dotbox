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

private enum class HrMethod(val label: String) {
    STANDARD("Standard %"),
    KARVONEN("Karvonen"),
}

private data class HeartRateZone(
    val name: String,
    val lowPct: Double,
    val highPct: Double,
    val color: Color,
    val benefit: String,
)

private val zones = listOf(
    HeartRateZone("Zone 1 · Recovery", 0.50, 0.60, Color(0xFF66BB6A), "Warm-up & active recovery"),
    HeartRateZone("Zone 2 · Fat Burn", 0.60, 0.70, Color(0xFF4DD0E1), "Endurance & fat oxidation"),
    HeartRateZone("Zone 3 · Aerobic", 0.70, 0.80, Color(0xFFFFB74D), "Cardio fitness & stamina"),
    HeartRateZone("Zone 4 · Anaerobic", 0.80, 0.90, Color(0xFFEF5350), "Speed & lactate threshold"),
    HeartRateZone("Zone 5 · VO2 Max", 0.90, 1.00, Color(0xFFD32F2F), "Peak power & max effort"),
)

@Composable
fun HeartRateZonesScreen(onBack: () -> Unit) {
    var ageInput by rememberSaveable { mutableStateOf("") }
    var restingHrInput by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf(HrMethod.STANDARD.name) }

    val selectedMethod = HrMethod.valueOf(method)
    val age = ageInput.toIntOrNull()
    val restingHr = restingHrInput.toIntOrNull()
    val maxHr = if (age != null && age in 1..120) 220 - age else null

    ToolScreenScaffold(title = "Heart Rate Zones", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Age input
            OutlinedTextField(
                value = ageInput,
                onValueChange = { ageInput = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Age") },
                placeholder = { Text("e.g. 30") },
                suffix = { Text("years") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Resting HR input
            OutlinedTextField(
                value = restingHrInput,
                onValueChange = { restingHrInput = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Resting Heart Rate (optional)") },
                placeholder = { Text("e.g. 60") },
                suffix = { Text("bpm") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Method toggle
            Text(
                text = "Calculation Method",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HrMethod.entries.forEach { m ->
                    FilterChip(
                        selected = selectedMethod == m,
                        onClick = { method = m.name },
                        label = { Text(m.label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            if (selectedMethod == HrMethod.KARVONEN && restingHr == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your resting heart rate for Karvonen method",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Max HR display
            if (maxHr != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Max Heart Rate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$maxHr bpm",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )
                    if (selectedMethod == HrMethod.KARVONEN && restingHr != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Heart Rate Reserve: ${maxHr - restingHr} bpm",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Zones
                val canShowZones = when (selectedMethod) {
                    HrMethod.STANDARD -> true
                    HrMethod.KARVONEN -> restingHr != null
                }

                if (canShowZones) {
                    zones.forEach { zone ->
                        val lowHr: Int
                        val highHr: Int
                        when (selectedMethod) {
                            HrMethod.STANDARD -> {
                                lowHr = (maxHr * zone.lowPct).toInt()
                                highHr = (maxHr * zone.highPct).toInt()
                            }
                            HrMethod.KARVONEN -> {
                                val hrr = maxHr - (restingHr ?: 0)
                                lowHr = (hrr * zone.lowPct + (restingHr ?: 0)).toInt()
                                highHr = (hrr * zone.highPct + (restingHr ?: 0)).toInt()
                            }
                        }

                        ZoneCard(
                            zone = zone,
                            lowHr = lowHr,
                            highHr = highHr,
                            maxHr = maxHr,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else if (ageInput.isNotEmpty()) {
                Text(
                    text = "Please enter a valid age (1–120)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ZoneCard(
    zone: HeartRateZone,
    lowHr: Int,
    highHr: Int,
    maxHr: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = zone.name,
                style = MaterialTheme.typography.titleSmall,
                color = zone.color,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$lowHr – $highHr bpm",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = zone.benefit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal bar
        val barFraction = (highHr.toFloat() / maxHr.toFloat()).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = barFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(zone.color),
            )
        }
    }
}
