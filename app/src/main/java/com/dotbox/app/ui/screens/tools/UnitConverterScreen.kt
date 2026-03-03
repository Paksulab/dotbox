package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono

private data class UnitDef(val name: String, val symbol: String, val toBase: (Double) -> Double, val fromBase: (Double) -> Double)

private data class UnitCategoryDef(val name: String, val units: List<UnitDef>)

private val unitCategories = listOf(
    UnitCategoryDef("Length", listOf(
        UnitDef("Millimeter", "mm", { it / 1000 }, { it * 1000 }),
        UnitDef("Centimeter", "cm", { it / 100 }, { it * 100 }),
        UnitDef("Meter", "m", { it }, { it }),
        UnitDef("Kilometer", "km", { it * 1000 }, { it / 1000 }),
        UnitDef("Inch", "in", { it * 0.0254 }, { it / 0.0254 }),
        UnitDef("Foot", "ft", { it * 0.3048 }, { it / 0.3048 }),
        UnitDef("Yard", "yd", { it * 0.9144 }, { it / 0.9144 }),
        UnitDef("Mile", "mi", { it * 1609.344 }, { it / 1609.344 }),
    )),
    UnitCategoryDef("Weight", listOf(
        UnitDef("Milligram", "mg", { it / 1_000_000 }, { it * 1_000_000 }),
        UnitDef("Gram", "g", { it / 1000 }, { it * 1000 }),
        UnitDef("Kilogram", "kg", { it }, { it }),
        UnitDef("Tonne", "t", { it * 1000 }, { it / 1000 }),
        UnitDef("Ounce", "oz", { it * 0.0283495 }, { it / 0.0283495 }),
        UnitDef("Pound", "lb", { it * 0.453592 }, { it / 0.453592 }),
    )),
    UnitCategoryDef("Temperature", listOf(
        UnitDef("Celsius", "°C", { it }, { it }),
        UnitDef("Fahrenheit", "°F", { (it - 32) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32 }),
        UnitDef("Kelvin", "K", { it - 273.15 }, { it + 273.15 }),
    )),
    UnitCategoryDef("Volume", listOf(
        UnitDef("Milliliter", "mL", { it / 1000 }, { it * 1000 }),
        UnitDef("Liter", "L", { it }, { it }),
        UnitDef("Gallon (US)", "gal", { it * 3.78541 }, { it / 3.78541 }),
        UnitDef("Quart (US)", "qt", { it * 0.946353 }, { it / 0.946353 }),
        UnitDef("Cup (US)", "cup", { it * 0.236588 }, { it / 0.236588 }),
        UnitDef("Fl Oz (US)", "fl oz", { it * 0.0295735 }, { it / 0.0295735 }),
    )),
    UnitCategoryDef("Speed", listOf(
        UnitDef("m/s", "m/s", { it }, { it }),
        UnitDef("km/h", "km/h", { it / 3.6 }, { it * 3.6 }),
        UnitDef("mph", "mph", { it * 0.44704 }, { it / 0.44704 }),
        UnitDef("Knot", "kn", { it * 0.514444 }, { it / 0.514444 }),
    )),
    UnitCategoryDef("Data", listOf(
        UnitDef("Byte", "B", { it }, { it }),
        UnitDef("Kilobyte", "KB", { it * 1024 }, { it / 1024 }),
        UnitDef("Megabyte", "MB", { it * 1024 * 1024 }, { it / (1024 * 1024) }),
        UnitDef("Gigabyte", "GB", { it * 1024 * 1024 * 1024 }, { it / (1024.0 * 1024 * 1024) }),
        UnitDef("Terabyte", "TB", { it * 1024.0 * 1024 * 1024 * 1024 }, { it / (1024.0 * 1024 * 1024 * 1024) }),
    )),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var fromIndex by rememberSaveable { mutableIntStateOf(0) }
    var toIndex by rememberSaveable { mutableIntStateOf(1) }
    var inputValue by rememberSaveable { mutableStateOf("1") }

    val currentCategory = unitCategories[selectedCategoryIndex]
    val units = currentCategory.units

    // Ensure indices are valid after category change
    val safeFromIndex = fromIndex.coerceIn(0, units.lastIndex)
    val safeToIndex = toIndex.coerceIn(0, units.lastIndex)

    val fromUnit = units[safeFromIndex]
    val toUnit = units[safeToIndex]

    val result = inputValue.toDoubleOrNull()?.let { value ->
        val baseValue = fromUnit.toBase(value)
        toUnit.fromBase(baseValue)
    }

    val formattedResult = result?.let {
        if (it == it.toLong().toDouble() && !it.isInfinite() && !it.isNaN() && kotlin.math.abs(it) < 1e15) {
            it.toLong().toString()
        } else {
            String.format("%.10g", it)
        }
    } ?: ""

    ToolScreenScaffold(title = "Unit Converter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                items(unitCategories) { category ->
                    val index = unitCategories.indexOf(category)
                    FilterChip(
                        selected = selectedCategoryIndex == index,
                        onClick = {
                            selectedCategoryIndex = index
                            fromIndex = 0
                            toIndex = if (unitCategories[index].units.size > 1) 1 else 0
                        },
                        label = { Text(category.name, style = MaterialTheme.typography.labelLarge) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // From unit
            Text("FROM", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            UnitDropdown(
                units = units,
                selectedIndex = safeFromIndex,
                onSelect = { fromIndex = it },
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
                suffix = { Text(fromUnit.symbol, style = MaterialTheme.typography.titleMedium) },
            )

            // Swap button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = {
                        val temp = fromIndex
                        fromIndex = toIndex
                        toIndex = temp
                        inputValue = formattedResult
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            // To unit
            Text("TO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            UnitDropdown(
                units = units,
                selectedIndex = safeToIndex,
                onSelect = { toIndex = it },
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formattedResult,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                readOnly = true,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                suffix = { Text(toUnit.symbol, style = MaterialTheme.typography.titleMedium) },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    units: List<UnitDef>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = "${units[selectedIndex].name} (${units[selectedIndex].symbol})",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { index, unit ->
                DropdownMenuItem(
                    text = { Text("${unit.name} (${unit.symbol})") },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                )
            }
        }
    }
}
