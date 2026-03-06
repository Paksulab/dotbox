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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun TipCalculatorScreen(onBack: () -> Unit) {
    var billAmount by rememberSaveable { mutableStateOf("") }
    var tipPercent by rememberSaveable { mutableFloatStateOf(15f) }
    var splitCount by rememberSaveable { mutableIntStateOf(1) }
    var isCustomSplit by rememberSaveable { mutableStateOf(false) }
    var roundUp by rememberSaveable { mutableStateOf(false) }

    // Custom split percentages per person (must sum to 100)
    val customShares = remember { mutableStateListOf<Float>() }

    // Sync custom shares when split count changes
    LaunchedEffect(splitCount) {
        if (customShares.size != splitCount) {
            customShares.clear()
            val equalShare = 100f / splitCount
            repeat(splitCount) { customShares.add(equalShare) }
        }
    }

    val bill = billAmount.toDoubleOrNull() ?: 0.0
    val tipAmount by remember(bill, tipPercent) {
        derivedStateOf { bill * tipPercent / 100.0 }
    }
    val rawTotal by remember(bill, tipAmount) {
        derivedStateOf { bill + tipAmount }
    }
    val total by remember(rawTotal, roundUp) {
        derivedStateOf { if (roundUp && rawTotal > 0) ceil(rawTotal) else rawTotal }
    }
    val effectiveTip by remember(total, bill) {
        derivedStateOf { total - bill }
    }
    val perPerson by remember(total, splitCount) {
        derivedStateOf { if (splitCount > 0) total / splitCount else total }
    }
    val tipPerPerson by remember(effectiveTip, splitCount) {
        derivedStateOf { if (splitCount > 0) effectiveTip / splitCount else effectiveTip }
    }

    val quickTips = listOf(10, 15, 18, 20, 25)

    ToolScreenScaffold(title = "Tip Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Bill amount
            OutlinedTextField(
                value = billAmount,
                onValueChange = { billAmount = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bill Amount") },
                placeholder = { Text("0.00") },
                prefix = { Text("$") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tip percentage
            Text(
                text = "Tip: ${tipPercent.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                quickTips.forEach { pct ->
                    FilterChip(
                        selected = tipPercent.toInt() == pct,
                        onClick = { tipPercent = pct.toFloat() },
                        label = { Text("$pct%", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Slider(
                value = tipPercent,
                onValueChange = { tipPercent = it },
                valueRange = 0f..50f,
                steps = 49,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.tertiary,
                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Round up switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Round up total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Round to nearest dollar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = roundUp,
                    onCheckedChange = { roundUp = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                        checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split
            Text(
                text = "Split: $splitCount ${if (splitCount == 1) "person" else "people"}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = splitCount.toFloat(),
                onValueChange = { splitCount = it.toInt() },
                valueRange = 1f..20f,
                steps = 18,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.tertiary,
                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            // Equal / Custom split toggle (only when split > 1)
            if (splitCount > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !isCustomSplit,
                        onClick = { isCustomSplit = false },
                        label = { Text("Equal Split") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    FilterChip(
                        selected = isCustomSplit,
                        onClick = { isCustomSplit = true },
                        label = { Text("Custom Split") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }

                // Custom split sliders
                if (isCustomSplit && customShares.size == splitCount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "CUSTOM SHARES",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        customShares.forEachIndexed { index, share ->
                            val personAmount = total * share / 100.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "P${index + 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.width(28.dp),
                                )
                                Slider(
                                    value = share,
                                    onValueChange = { newVal ->
                                        // Redistribute remaining % equally among others
                                        val clamped = newVal.coerceIn(0f, 100f)
                                        val remaining = 100f - clamped
                                        val otherCount = splitCount - 1
                                        if (otherCount > 0) {
                                            val otherShare = remaining / otherCount
                                            for (i in customShares.indices) {
                                                customShares[i] = if (i == index) clamped else otherShare
                                            }
                                        }
                                    },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.tertiary,
                                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                    ),
                                )
                                Text(
                                    text = "${share.roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(36.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
            ) {
                TipResultRow("Bill", formatTipCurrency(bill))
                TipResultRow("Tip (${tipPercent.toInt()}%)", formatTipCurrency(tipAmount))
                if (roundUp && rawTotal != total) {
                    TipResultRow("Round up", formatTipCurrency(total - rawTotal))
                    TipResultRow("Effective Tip", formatTipCurrency(effectiveTip))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
                TipResultRow("Total", formatTipCurrency(total), bold = true)

                if (splitCount > 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )

                    if (isCustomSplit && customShares.size == splitCount) {
                        // Show per-person custom breakdown
                        customShares.forEachIndexed { index, share ->
                            val personTotal = total * share / 100.0
                            TipResultRow(
                                "Person ${index + 1} (${share.roundToInt()}%)",
                                formatTipCurrency(personTotal),
                                accent = index == 0,
                            )
                        }
                    } else {
                        TipResultRow("Per Person", formatTipCurrency(perPerson), bold = true, accent = true)
                        TipResultRow("Tip Each", formatTipCurrency(tipPerPerson))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TipResultRow(
    label: String,
    value: String,
    bold: Boolean = false,
    accent: Boolean = false,
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
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (accent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatTipCurrency(amount: Double): String {
    return String.format(Locale.US, "$%.2f", amount)
}
