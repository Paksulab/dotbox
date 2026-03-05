package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoseCalculatorScreen(onBack: () -> Unit) {
    var weightInput by rememberSaveable { mutableStateOf("") }
    var isKg by rememberSaveable { mutableStateOf(true) }
    var dosePerKg by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("1") }
    var concentration by rememberSaveable { mutableStateOf("") }

    val presetDoses = listOf(5, 10, 15, 20, 25)

    val weightKg = remember(weightInput, isKg) {
        val raw = weightInput.toDoubleOrNull()
        if (raw != null && raw > 0) {
            if (isKg) raw else raw / 2.20462
        } else null
    }

    val dosePerKgValue = remember(dosePerKg) {
        dosePerKg.toDoubleOrNull()?.takeIf { it > 0 }
    }

    val frequencyValue = remember(frequency) {
        frequency.toIntOrNull()?.takeIf { it > 0 }
    }

    val concentrationValue = remember(concentration) {
        concentration.toDoubleOrNull()?.takeIf { it > 0 }
    }

    val totalDose = remember(weightKg, dosePerKgValue) {
        if (weightKg != null && dosePerKgValue != null) weightKg * dosePerKgValue else null
    }

    val dailyDose = remember(totalDose, frequencyValue) {
        if (totalDose != null && frequencyValue != null) totalDose * frequencyValue else null
    }

    val volumePerDose = remember(totalDose, concentrationValue) {
        if (totalDose != null && concentrationValue != null) totalDose / concentrationValue else null
    }

    val dailyVolume = remember(volumePerDose, frequencyValue) {
        if (volumePerDose != null && frequencyValue != null) volumePerDose * frequencyValue else null
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
        cursorColor = MaterialTheme.colorScheme.tertiary
    )

    ToolScreenScaffold(
        title = "Dose Calculator",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weight unit toggle
            Text(
                text = "Weight Unit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isKg,
                    onClick = {
                        if (!isKg) {
                            val current = weightInput.toDoubleOrNull()
                            if (current != null) {
                                weightInput = String.format(Locale.US, "%.1f", current / 2.20462)
                            }
                            isKg = true
                        }
                    },
                    label = { Text("kg") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    selected = !isKg,
                    onClick = {
                        if (isKg) {
                            val current = weightInput.toDoubleOrNull()
                            if (current != null) {
                                weightInput = String.format(Locale.US, "%.1f", current * 2.20462)
                            }
                            isKg = false
                        }
                    },
                    label = { Text("lbs") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Patient weight input
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Patient Weight (${if (isKg) "kg" else "lbs"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            // Dose per kg input
            OutlinedTextField(
                value = dosePerKg,
                onValueChange = { dosePerKg = it },
                label = { Text("Dose per kg (mg/kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            // Quick preset doses
            Text(
                text = "Quick Dose Presets",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presetDoses.forEach { preset ->
                    FilterChip(
                        selected = dosePerKg == preset.toString(),
                        onClick = { dosePerKg = preset.toString() },
                        label = { Text("$preset mg/kg") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Frequency input
            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Frequency (times per day)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            // Concentration input (optional)
            OutlinedTextField(
                value = concentration,
                onValueChange = { concentration = it },
                label = { Text("Concentration (mg/mL) — optional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            // Results card
            if (totalDose != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Total dose per administration
                        Column {
                            Text(
                                text = "Total Dose per Administration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", totalDose)} mg",
                                fontFamily = JetBrainsMono,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                        // Daily total dose
                        if (dailyDose != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daily Total Dose",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", dailyDose)} mg",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Volume per dose (only if concentration provided)
                        if (volumePerDose != null) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Volume per Dose",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", volumePerDose)} mL",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Daily volume
                            if (dailyVolume != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daily Volume",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", dailyVolume)} mL",
                                        fontFamily = JetBrainsMono,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Disclaimer
            Text(
                text = "For reference only. Always verify with prescribing information.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
