package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlin.math.roundToInt

private enum class WeightUnit(val label: String, val suffix: String) {
    KG("kg", "kg"),
    LB("lb", "lb"),
}

// Standard 1RM formulas
private fun epley1RM(weight: Double, reps: Int): Double = weight * (1 + reps / 30.0)
private fun brzycki1RM(weight: Double, reps: Int): Double = weight * (36.0 / (37.0 - reps))
private fun lander1RM(weight: Double, reps: Int): Double = weight * 100.0 / (101.3 - 2.67123 * reps)
private fun lombardi1RM(weight: Double, reps: Int): Double = weight * Math.pow(reps.toDouble(), 0.1)
private fun oconner1RM(weight: Double, reps: Int): Double = weight * (1 + reps / 40.0)

@Composable
fun OneRepMaxScreen(onBack: () -> Unit) {
    var weightInput by rememberSaveable { mutableStateOf("") }
    var repsInput by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(WeightUnit.KG.name) }

    val selectedUnit = WeightUnit.valueOf(unit)
    val weight = weightInput.toDoubleOrNull()
    val reps = repsInput.toIntOrNull()

    ToolScreenScaffold(title = "1RM Calculator", onBack = onBack) { paddingValues ->
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
                WeightUnit.entries.forEach { u ->
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

            // Weight input
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Weight Lifted") },
                placeholder = { Text("e.g. 100") },
                suffix = { Text(selectedUnit.suffix) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reps input
            OutlinedTextField(
                value = repsInput,
                onValueChange = { repsInput = it.filter { c -> c.isDigit() }.take(2) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reps Performed") },
                placeholder = { Text("e.g. 5") },
                suffix = { Text("reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (weight != null && weight > 0 && reps != null && reps in 1..30) {
                // 1RM result
                val epley = epley1RM(weight, reps)
                val brzycki = brzycki1RM(weight, reps)
                val lander = lander1RM(weight, reps)
                val lombardi = lombardi1RM(weight, reps)
                val oconner = oconner1RM(weight, reps)
                val average = (epley + brzycki + lander + lombardi + oconner) / 5.0

                // Main result
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
                        text = "Estimated 1RM",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${average.roundToInt()} ${selectedUnit.suffix}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Average of 5 formulas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Formula breakdown
                val formulas = listOf(
                    "Epley" to epley,
                    "Brzycki" to brzycki,
                    "Lander" to lander,
                    "Lombardi" to lombardi,
                    "O'Conner" to oconner,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "FORMULA BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    formulas.forEach { (name, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${value.roundToInt()} ${selectedUnit.suffix}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Percentage chart
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "TRAINING LOADS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "95%" to 0.95, "90%" to 0.90, "85%" to 0.85,
                        "80%" to 0.80, "75%" to 0.75, "70%" to 0.70,
                        "65%" to 0.65, "60%" to 0.60, "50%" to 0.50,
                    ).forEach { (label, pct) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${(average * pct).roundToInt()} ${selectedUnit.suffix}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else if (weightInput.isNotEmpty() && repsInput.isNotEmpty()) {
                Text(
                    text = if (reps != null && reps !in 1..30) "Reps must be between 1–30" else "Enter valid weight and reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
