package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WaterIntakeScreen(onBack: () -> Unit) {
    ToolScreenScaffold(
        title = "Water Intake Calculator",
        onBack = onBack
    ) {
        val scrollState = rememberScrollState()

        var weightInput by rememberSaveable { mutableStateOf("") }
        var useKg by rememberSaveable { mutableStateOf(true) }
        var activityLevel by rememberSaveable { mutableStateOf("Moderate") }
        var climate by rememberSaveable { mutableStateOf("Normal") }

        val activityLevels = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")
        val climateOptions = listOf("Normal", "Hot", "Cold")

        val activityMultipliers = mapOf(
            "Sedentary" to 1.0,
            "Light" to 1.1,
            "Moderate" to 1.2,
            "Active" to 1.3,
            "Very Active" to 1.4
        )

        val climateMultipliers = mapOf(
            "Normal" to 1.0,
            "Hot" to 1.2,
            "Cold" to 0.9
        )

        val weightKg = weightInput.toDoubleOrNull()?.let { w ->
            if (useKg) w else w * 0.453592
        }

        val baseIntake = weightKg?.let { it * 0.033 }
        val activityMultiplier = activityMultipliers[activityLevel] ?: 1.0
        val climateMultiplier = climateMultipliers[climate] ?: 1.0

        val afterActivity = baseIntake?.let { it * activityMultiplier }
        val totalLiters = afterActivity?.let { it * climateMultiplier }

        val tertiaryColor = MaterialTheme.colorScheme.tertiary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Weight input
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your Weight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    weightInput = newValue
                                }
                            },
                            label = { Text(if (useKg) "Weight (kg)" else "Weight (lbs)") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tertiaryColor,
                                cursorColor = tertiaryColor
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = useKg,
                                onClick = {
                                    if (!useKg) {
                                        useKg = true
                                        weightInput.toDoubleOrNull()?.let { lbs ->
                                            weightInput = String.format(Locale.US, "%.1f", lbs * 0.453592)
                                        }
                                    }
                                },
                                label = { Text("kg") },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tertiaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = tertiaryColor
                                )
                            )
                            FilterChip(
                                selected = !useKg,
                                onClick = {
                                    if (useKg) {
                                        useKg = false
                                        weightInput.toDoubleOrNull()?.let { kg ->
                                            weightInput = String.format(Locale.US, "%.1f", kg / 0.453592)
                                        }
                                    }
                                },
                                label = { Text("lbs") },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tertiaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = tertiaryColor
                                )
                            )
                        }
                    }
                }
            }

            // Activity level
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Activity Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activityLevels.forEach { level ->
                            FilterChip(
                                selected = activityLevel == level,
                                onClick = { activityLevel = level },
                                label = { Text(level) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tertiaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = tertiaryColor
                                )
                            )
                        }
                    }
                }
            }

            // Climate
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Climate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        climateOptions.forEach { option ->
                            FilterChip(
                                selected = climate == option,
                                onClick = { climate = option },
                                label = { Text(option) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tertiaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = tertiaryColor
                                )
                            )
                        }
                    }
                }
            }

            // Results
            if (totalLiters != null && totalLiters > 0) {
                val totalMl = totalLiters * 1000.0
                val totalFlOz = totalLiters * 33.814
                val totalCups = totalFlOz / 8.0
                val totalGlasses = totalMl / 250.0
                val glassesRounded = kotlin.math.ceil(totalGlasses).toInt()

                // Main result
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tertiaryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Water Target",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Text(
                            text = String.format(Locale.US, "%.2f L", totalLiters),
                            fontFamily = JetBrainsMono,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = tertiaryColor
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )

                        // Equivalents
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            EquivalentItem(
                                value = String.format(Locale.US, "%.0f", totalMl),
                                label = "mL"
                            )
                            EquivalentItem(
                                value = String.format(Locale.US, "%.1f", totalFlOz),
                                label = "fl oz"
                            )
                            EquivalentItem(
                                value = String.format(Locale.US, "%.1f", totalCups),
                                label = "cups (8oz)"
                            )
                            EquivalentItem(
                                value = String.format(Locale.US, "%.1f", totalGlasses),
                                label = "glasses"
                            )
                        }
                    }
                }

                // Visual glasses representation
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Glasses of Water (250 mL each)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val fullGlasses = totalGlasses.toInt()
                        val partialFill = totalGlasses - fullGlasses

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (i in 0 until glassesRounded) {
                                val fillRatio = when {
                                    i < fullGlasses -> 1.0f
                                    i == fullGlasses -> partialFill.toFloat()
                                    else -> 0.0f
                                }
                                WaterGlass(fillRatio = fillRatio)
                            }
                        }
                    }
                }

                // Breakdown
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        BreakdownRow(
                            label = "Base intake (weight × 0.033)",
                            value = String.format(Locale.US, "%.2f L", baseIntake ?: 0.0)
                        )

                        val activityAdjustment = (afterActivity ?: 0.0) - (baseIntake ?: 0.0)
                        BreakdownRow(
                            label = "Activity adjustment ($activityLevel ×$activityMultiplier)",
                            value = String.format(
                                Locale.US,
                                "%+.2f L",
                                activityAdjustment
                            )
                        )

                        val climateAdjustment = totalLiters - (afterActivity ?: 0.0)
                        BreakdownRow(
                            label = "Climate adjustment ($climate ×$climateMultiplier)",
                            value = String.format(
                                Locale.US,
                                "%+.2f L",
                                climateAdjustment
                            )
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f L", totalLiters),
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = tertiaryColor
                            )
                        }
                    }
                }

                // Tip
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tip",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Drink more when exercising, in hot weather, or feeling thirsty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EquivalentItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = JetBrainsMono,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WaterGlass(fillRatio: Float) {
    val waterBlue = Color(0xFF42A5F5)
    val glassShape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp, topStart = 2.dp, topEnd = 2.dp)

    Box(
        modifier = Modifier
            .width(28.dp)
            .height(40.dp)
            .clip(glassShape)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                shape = glassShape
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (fillRatio > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = fillRatio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                    .background(waterBlue.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontFamily = JetBrainsMono,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
