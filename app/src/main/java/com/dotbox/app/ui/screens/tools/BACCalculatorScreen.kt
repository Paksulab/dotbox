package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.Locale

@Composable
fun BACCalculatorScreen(onBack: () -> Unit) {
    var drinks by rememberSaveable { mutableStateOf("") }
    var gramsPerDrink by rememberSaveable { mutableStateOf("14") }
    var bodyWeight by rememberSaveable { mutableStateOf("") }
    var isMale by rememberSaveable { mutableStateOf(true) }
    var hours by rememberSaveable { mutableStateOf("") }

    val drinksVal = drinks.toDoubleOrNull() ?: 0.0
    val gramsVal = gramsPerDrink.toDoubleOrNull() ?: 14.0
    val weightVal = bodyWeight.toDoubleOrNull() ?: 0.0
    val hoursVal = hours.toDoubleOrNull() ?: 0.0

    val bac by remember(drinksVal, gramsVal, weightVal, isMale, hoursVal) {
        derivedStateOf {
            if (weightVal > 0.0) {
                val r = if (isMale) 0.68 else 0.55
                val alcoholGrams = drinksVal * gramsVal
                val rawBac = (alcoholGrams / (weightVal * r)) - (0.015 * hoursVal)
                maxOf(0.0, rawBac)
            } else {
                0.0
            }
        }
    }

    val bacFormatted by remember(bac) {
        derivedStateOf { String.format(Locale.US, "%.4f", bac) }
    }

    val status by remember(bac) {
        derivedStateOf {
            when {
                bac == 0.0 -> BacStatus("Sober", Color(0xFF4CAF50))
                bac <= 0.03 -> BacStatus("Minimal", Color(0xFF8BC34A))
                bac <= 0.06 -> BacStatus("Buzzed", Color(0xFFFFEB3B))
                bac <= 0.09 -> BacStatus("Impaired", Color(0xFFFF9800))
                else -> BacStatus("Dangerous", Color(0xFFF44336))
            }
        }
    }

    val timeToSober by remember(bac) {
        derivedStateOf {
            if (bac > 0.0) bac / 0.015 else 0.0
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
        cursorColor = MaterialTheme.colorScheme.tertiary,
    )

    ToolScreenScaffold(title = "BAC Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Number of drinks
            OutlinedTextField(
                value = drinks,
                onValueChange = { drinks = it },
                label = { Text("Number of Drinks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alcohol per drink
            OutlinedTextField(
                value = gramsPerDrink,
                onValueChange = { gramsPerDrink = it },
                label = { Text("Alcohol per Drink (grams)") },
                supportingText = { Text("US standard drink = 14g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body weight
            OutlinedTextField(
                value = bodyWeight,
                onValueChange = { bodyWeight = it },
                label = { Text("Body Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hours since first drink
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("Hours Since First Drink") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender toggle
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = isMale,
                    onClick = { isMale = true },
                    label = { Text("Male") },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                FilterChip(
                    selected = !isMale,
                    onClick = { isMale = false },
                    label = { Text("Female") },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Blood Alcohol Content",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Large BAC value
                Text(
                    text = bacFormatted,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = status.color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status label
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = status.color,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time to sober
                BacResultRow(
                    label = "Estimated BAC",
                    value = bacFormatted,
                )
                BacResultRow(
                    label = "Time to Sober",
                    value = if (bac > 0.0) formatDuration(timeToSober) else "—",
                )
                BacResultRow(
                    label = "Metabolism Rate",
                    value = "0.015 / hr",
                )
                BacResultRow(
                    label = "Widmark Factor (r)",
                    value = if (isMale) "0.68" else "0.55",
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Disclaimer
                Text(
                    text = "\u26A0\uFE0F For educational purposes only. Never drink and drive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class BacStatus(
    val label: String,
    val color: Color,
)

@Composable
private fun BacResultRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatDuration(hours: Double): String {
    val totalMinutes = (hours * 60).toInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        m > 0 -> "${m}m"
        else -> "0m"
    }
}
