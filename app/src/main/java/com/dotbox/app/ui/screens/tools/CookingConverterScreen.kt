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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale

private enum class ConversionMode(val label: String) {
    Volume("Volume"),
    ByIngredient("By Ingredient"),
}

private enum class VolumeUnit(val label: String, val toMl: Double) {
    Cup("Cup", 236.588),
    Tablespoon("Tablespoon (tbsp)", 14.787),
    Teaspoon("Teaspoon (tsp)", 4.929),
    FluidOz("Fluid oz", 29.574),
    Milliliter("mL", 1.0),
    Liter("Liter", 1000.0),
}

private enum class Ingredient(val label: String, val gramsPerCup: Double) {
    Flour("Flour (All-purpose)", 120.0),
    Sugar("Sugar (Granulated)", 200.0),
    Butter("Butter", 227.0),
    Milk("Milk", 244.0),
    Rice("Rice", 185.0),
    Oats("Oats", 90.0),
}

private const val GRAMS_PER_OUNCE = 28.3495

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingConverterScreen(onBack: () -> Unit) {
    var mode by rememberSaveable { mutableStateOf(ConversionMode.Volume.name) }
    val isVolume = mode == ConversionMode.Volume.name

    // Volume mode state
    var volumeInput by rememberSaveable { mutableStateOf("") }
    var selectedUnit by rememberSaveable { mutableStateOf(VolumeUnit.Cup.name) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    // Ingredient mode state
    var ingredientInput by rememberSaveable { mutableStateOf("") }
    var selectedIngredient by rememberSaveable { mutableStateOf(Ingredient.Flour.name) }
    var ingredientDropdownExpanded by remember { mutableStateOf(false) }

    val currentUnit = VolumeUnit.valueOf(selectedUnit)
    val currentIngredient = Ingredient.valueOf(selectedIngredient)

    // Volume conversions
    val volumeValue = volumeInput.toDoubleOrNull() ?: 0.0
    val mlValue by remember(volumeValue, currentUnit) {
        derivedStateOf { volumeValue * currentUnit.toMl }
    }

    // Ingredient conversions
    val cupsValue = ingredientInput.toDoubleOrNull() ?: 0.0
    val gramsResult by remember(cupsValue, currentIngredient) {
        derivedStateOf { cupsValue * currentIngredient.gramsPerCup }
    }
    val ouncesResult by remember(gramsResult) {
        derivedStateOf { gramsResult / GRAMS_PER_OUNCE }
    }

    ToolScreenScaffold(title = "Cooking Converter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ConversionMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m.name,
                        onClick = { mode = m.name },
                        label = { Text(m.label) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isVolume) {
                // Volume mode: input + unit selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = volumeInput,
                        onValueChange = { volumeInput = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Value") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = it },
                        modifier = Modifier.weight(1.2f),
                    ) {
                        OutlinedTextField(
                            value = currentUnit.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("Unit") },
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false },
                        ) {
                            VolumeUnit.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.label) },
                                    onClick = {
                                        selectedUnit = unit.name
                                        unitDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Volume results card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    if (volumeValue > 0) {
                        Text(
                            text = "Conversions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        VolumeUnit.entries.forEachIndexed { index, unit ->
                            val converted = mlValue / unit.toMl
                            ConversionRow(
                                label = unit.label,
                                value = formatVolume(converted),
                                isCurrent = unit == currentUnit,
                            )
                            if (index < VolumeUnit.entries.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Enter a value to see\nall conversions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                // By Ingredient mode: ingredient selector
                ExposedDropdownMenuBox(
                    expanded = ingredientDropdownExpanded,
                    onExpandedChange = { ingredientDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = currentIngredient.label,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Ingredient") },
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientDropdownExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = ingredientDropdownExpanded,
                        onDismissRequest = { ingredientDropdownExpanded = false },
                    ) {
                        Ingredient.entries.forEach { ingredient ->
                            DropdownMenuItem(
                                text = { Text(ingredient.label) },
                                onClick = {
                                    selectedIngredient = ingredient.name
                                    ingredientDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ingredientInput,
                    onValueChange = { ingredientInput = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cups") },
                    placeholder = { Text("0") },
                    suffix = { Text("cups") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredient results card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    if (cupsValue > 0) {
                        Text(
                            text = currentIngredient.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatVolume(cupsValue)} ${if (cupsValue == 1.0) "cup" else "cups"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ConversionRow(
                            label = "Grams",
                            value = formatWeight(gramsResult, "g"),
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.outline,
                        )
                        ConversionRow(
                            label = "Ounces",
                            value = formatWeight(ouncesResult, "oz"),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Density: ${currentIngredient.gramsPerCup.toInt()} g/cup",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "Enter cups to convert\nto grams and ounces",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConversionRow(
    label: String,
    value: String,
    isCurrent: Boolean = false,
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
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatVolume(value: Double): String {
    return if (value >= 1000) {
        String.format(Locale.US, "%,.1f", value)
    } else if (value >= 1) {
        String.format(Locale.US, "%.2f", value)
    } else {
        String.format(Locale.US, "%.3f", value)
    }
}

private fun formatWeight(value: Double, suffix: String): String {
    return if (value >= 1000) {
        String.format(Locale.US, "%,.1f %s", value, suffix)
    } else {
        String.format(Locale.US, "%.1f %s", value, suffix)
    }
}
