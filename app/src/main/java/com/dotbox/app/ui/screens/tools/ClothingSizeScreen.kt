package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono

// ── Data model ──────────────────────────────────────────────────────────────

private enum class Region(val label: String) {
    US("US"),
    EU("EU"),
    UK("UK"),
    JP("JP")
}

private data class SizeEntry(
    val us: String,
    val eu: String,
    val uk: String,
    val jp: String = ""
)

private data class SizeCategory(
    val name: String,
    val regions: List<Region>,
    val sizes: List<SizeEntry>
)

// ── Size data ───────────────────────────────────────────────────────────────

private val shoesMen = SizeCategory(
    name = "Shoes (Men)",
    regions = listOf(Region.US, Region.EU, Region.UK, Region.JP),
    sizes = listOf(
        SizeEntry("6", "39", "5.5", "24"),
        SizeEntry("6.5", "39.5", "6", "24.5"),
        SizeEntry("7", "40", "6.5", "25"),
        SizeEntry("7.5", "40.5", "7", "25.5"),
        SizeEntry("8", "41", "7.5", "26"),
        SizeEntry("8.5", "42", "8", "26.5"),
        SizeEntry("9", "42.5", "8.5", "27"),
        SizeEntry("9.5", "43", "9", "27.5"),
        SizeEntry("10", "44", "9.5", "28"),
        SizeEntry("10.5", "44.5", "10", "28.5"),
        SizeEntry("11", "45", "10.5", "29"),
        SizeEntry("12", "46", "11.5", "30"),
        SizeEntry("13", "47", "12.5", "31")
    )
)

private val shoesWomen = SizeCategory(
    name = "Shoes (Women)",
    regions = listOf(Region.US, Region.EU, Region.UK, Region.JP),
    sizes = listOf(
        SizeEntry("5", "35.5", "3", "22"),
        SizeEntry("5.5", "36", "3.5", "22.5"),
        SizeEntry("6", "37", "4", "23"),
        SizeEntry("6.5", "37.5", "4.5", "23.5"),
        SizeEntry("7", "38", "5", "24"),
        SizeEntry("7.5", "38.5", "5.5", "24.5"),
        SizeEntry("8", "39", "6", "25"),
        SizeEntry("8.5", "40", "6.5", "25.5"),
        SizeEntry("9", "40.5", "7", "26"),
        SizeEntry("9.5", "41", "7.5", "26.5"),
        SizeEntry("10", "42", "8", "27"),
        SizeEntry("11", "43", "9", "28"),
        SizeEntry("12", "44", "10", "29")
    )
)

private val topsUnisex = SizeCategory(
    name = "Tops",
    regions = listOf(Region.US, Region.EU, Region.UK),
    sizes = listOf(
        SizeEntry("XS (32-34)", "42-44", "32-34"),
        SizeEntry("S (34-36)", "44-46", "34-36"),
        SizeEntry("M (38-40)", "46-48", "38-40"),
        SizeEntry("L (42-44)", "48-50", "42-44"),
        SizeEntry("XL (46-48)", "50-52", "46-48"),
        SizeEntry("XXL (50-52)", "52-54", "50-52"),
        SizeEntry("3XL (54-56)", "54-56", "54-56")
    )
)

private val pantsMen = SizeCategory(
    name = "Pants (Men)",
    regions = listOf(Region.US, Region.EU, Region.UK),
    sizes = listOf(
        SizeEntry("28", "44", "28"),
        SizeEntry("29", "44-46", "29"),
        SizeEntry("30", "46", "30"),
        SizeEntry("31", "46-48", "31"),
        SizeEntry("32", "48", "32"),
        SizeEntry("33", "48-50", "33"),
        SizeEntry("34", "50", "34"),
        SizeEntry("36", "52", "36"),
        SizeEntry("38", "54", "38"),
        SizeEntry("40", "56", "40")
    )
)

private val dressesWomen = SizeCategory(
    name = "Dresses",
    regions = listOf(Region.US, Region.EU, Region.UK),
    sizes = listOf(
        SizeEntry("0 (XS)", "30-32", "4"),
        SizeEntry("2 (XS)", "32-34", "6"),
        SizeEntry("4 (S)", "34-36", "8"),
        SizeEntry("6 (S)", "36-38", "10"),
        SizeEntry("8 (M)", "38-40", "12"),
        SizeEntry("10 (M)", "40-42", "14"),
        SizeEntry("12 (L)", "42-44", "16"),
        SizeEntry("14 (L)", "44-46", "18"),
        SizeEntry("16 (XL)", "46-48", "20")
    )
)

private val categories = listOf(shoesMen, shoesWomen, topsUnisex, pantsMen, dressesWomen)

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun SizeEntry.getByRegion(region: Region): String = when (region) {
    Region.US -> us
    Region.EU -> eu
    Region.UK -> uk
    Region.JP -> jp
}

private fun findMatchingRow(
    category: SizeCategory,
    fromRegion: Region,
    inputValue: String
): SizeEntry? {
    val normalised = inputValue.trim().lowercase()
    return category.sizes.firstOrNull { entry ->
        entry.getByRegion(fromRegion).lowercase() == normalised
    }
}

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingSizeScreen(onBack: () -> Unit) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentCategory = categories[selectedCategoryIndex]

    var fromRegionIndex by remember { mutableIntStateOf(0) }
    var toRegionIndex by remember { mutableIntStateOf(1) }
    var inputSize by remember { mutableStateOf("") }

    // Reset when category changes
    val availableRegions = currentCategory.regions

    // Clamp indices
    if (fromRegionIndex >= availableRegions.size) fromRegionIndex = 0
    if (toRegionIndex >= availableRegions.size) toRegionIndex = if (availableRegions.size > 1) 1 else 0

    val fromRegion = availableRegions[fromRegionIndex]
    val toRegion = availableRegions[toRegionIndex]

    // Conversion
    val matchedEntry = if (inputSize.isNotBlank()) {
        findMatchingRow(currentCategory, fromRegion, inputSize)
    } else null

    val convertedValue = matchedEntry?.getByRegion(toRegion) ?: ""

    // Available sizes for the "from" dropdown
    val availableSizes = currentCategory.sizes.map { it.getByRegion(fromRegion) }

    ToolScreenScaffold(
        title = "Clothing Sizes",
        onBack = onBack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Category chips ──────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEachIndexed { index, category ->
                        FilterChip(
                            selected = selectedCategoryIndex == index,
                            onClick = {
                                selectedCategoryIndex = index
                                inputSize = ""
                                fromRegionIndex = 0
                                toRegionIndex = if (categories[index].regions.size > 1) 1 else 0
                            },
                            label = {
                                Text(
                                    text = category.name,
                                    fontFamily = JetBrainsMono,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }

            // ── Converter card ──────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From / To region selectors with swap
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RegionDropdown(
                                label = "From",
                                regions = availableRegions,
                                selectedIndex = fromRegionIndex,
                                onSelect = { fromRegionIndex = it },
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    val tmp = fromRegionIndex
                                    fromRegionIndex = toRegionIndex
                                    toRegionIndex = tmp
                                    // Swap input to the converted value if available
                                    if (convertedValue.isNotBlank()) {
                                        inputSize = convertedValue
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapHoriz,
                                    contentDescription = "Swap",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            RegionDropdown(
                                label = "To",
                                regions = availableRegions,
                                selectedIndex = toRegionIndex,
                                onSelect = { toRegionIndex = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Size input dropdown
                        SizeInputDropdown(
                            label = "Size (${fromRegion.label})",
                            value = inputSize,
                            onValueChange = { inputSize = it },
                            suggestions = availableSizes
                        )

                        // Result
                        if (inputSize.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (convertedValue.isNotBlank()) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (convertedValue.isNotBlank()) {
                                        Text(
                                            text = "${toRegion.label} Size",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = convertedValue,
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        // Show full conversion row
                                        if (matchedEntry != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val allConversions = availableRegions
                                                .filter { it != fromRegion }
                                                .joinToString("  •  ") { region ->
                                                    "${region.label}: ${matchedEntry.getByRegion(region)}"
                                                }
                                            Text(
                                                text = allConversions,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = JetBrainsMono,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Size not found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = JetBrainsMono,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Pick a valid ${fromRegion.label} size from the list below",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Full reference table ────────────────────────────────
            item {
                Text(
                    text = "Reference Table",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 14.dp, horizontal = 8.dp)
                        ) {
                            availableRegions.forEach { region ->
                                Text(
                                    text = region.label,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Data rows
                        currentCategory.sizes.forEachIndexed { index, entry ->
                            val isMatched = matchedEntry == entry && inputSize.isNotBlank()
                            val bgColor by animateColorAsState(
                                targetValue = when {
                                    isMatched -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    index % 2 == 0 -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                label = "rowBg"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor)
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                availableRegions.forEach { region ->
                                    val textColor = if (isMatched) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                    Text(
                                        text = entry.getByRegion(region),
                                        modifier = Modifier.weight(1f),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 14.sp,
                                        fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        color = textColor
                                    )
                                }
                            }

                            if (index < currentCategory.sizes.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Sizes are approximate. Check retailer-specific charts for exact fit.",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Region dropdown ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(
    label: String,
    regions: List<Region>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = regions[selectedIndex].label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                cursorColor = MaterialTheme.colorScheme.tertiary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            regions.forEachIndexed { index, region ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = region.label,
                            fontFamily = JetBrainsMono
                        )
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── Size input with dropdown suggestions ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeInputDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = JetBrainsMono
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                cursorColor = MaterialTheme.colorScheme.tertiary
            )
        )

        val filtered = suggestions.filter {
            it.lowercase().contains(value.lowercase())
        }

        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filtered.forEach { size ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = size,
                                fontFamily = JetBrainsMono
                            )
                        },
                        onClick = {
                            onValueChange(size)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
