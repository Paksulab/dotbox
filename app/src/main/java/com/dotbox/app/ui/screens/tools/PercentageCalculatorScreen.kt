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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale

private enum class PercentMode(val label: String, val desc: String) {
    PERCENT_OF("% of X", "What is P% of X?"),
    X_IS_WHAT("X is ?% of Y", "X is what % of Y?"),
    CHANGE("% Change", "From X to Y"),
    INCREASE("Increase by %", "X + P%"),
    DECREASE("Decrease by %", "X − P%"),
}

@Composable
fun PercentageCalculatorScreen(onBack: () -> Unit) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = PercentMode.entries[modeIndex]

    var input1 by rememberSaveable { mutableStateOf("") }
    var input2 by rememberSaveable { mutableStateOf("") }

    val v1 = input1.toDoubleOrNull()
    val v2 = input2.toDoubleOrNull()

    val result: String = when (mode) {
        PercentMode.PERCENT_OF -> {
            if (v1 != null && v2 != null) fmt(v1 / 100.0 * v2)
            else "—"
        }
        PercentMode.X_IS_WHAT -> {
            if (v1 != null && v2 != null && v2 != 0.0) "${fmt(v1 / v2 * 100.0)}%"
            else "—"
        }
        PercentMode.CHANGE -> {
            if (v1 != null && v2 != null && v1 != 0.0) {
                val change = (v2 - v1) / v1 * 100.0
                val sign = if (change >= 0) "+" else ""
                "$sign${fmt(change)}%"
            } else "—"
        }
        PercentMode.INCREASE -> {
            if (v1 != null && v2 != null) fmt(v1 * (1 + v2 / 100.0))
            else "—"
        }
        PercentMode.DECREASE -> {
            if (v1 != null && v2 != null) fmt(v1 * (1 - v2 / 100.0))
            else "—"
        }
    }

    fun label1() = when (mode) {
        PercentMode.PERCENT_OF -> "Percentage (%)"
        PercentMode.X_IS_WHAT -> "Value (X)"
        PercentMode.CHANGE -> "From (X)"
        PercentMode.INCREASE -> "Value (X)"
        PercentMode.DECREASE -> "Value (X)"
    }

    fun label2() = when (mode) {
        PercentMode.PERCENT_OF -> "Number (X)"
        PercentMode.X_IS_WHAT -> "Total (Y)"
        PercentMode.CHANGE -> "To (Y)"
        PercentMode.INCREASE -> "Increase by (%)"
        PercentMode.DECREASE -> "Decrease by (%)"
    }

    ToolScreenScaffold(title = "Percentage", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode chips — two rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PercentMode.entries.take(3).forEachIndexed { idx, m ->
                        FilterChip(
                            selected = modeIndex == idx,
                            onClick = {
                                modeIndex = idx
                                input1 = ""; input2 = ""
                            },
                            label = { Text(m.label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PercentMode.entries.drop(3).forEachIndexed { idx, m ->
                        FilterChip(
                            selected = modeIndex == idx + 3,
                            onClick = {
                                modeIndex = idx + 3
                                input1 = ""; input2 = ""
                            },
                            label = { Text(m.label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mode.desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = input1,
                onValueChange = { input1 = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label1()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = input2,
                onValueChange = { input2 = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label2()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Result
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (result != "—") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun fmt(v: Double): String {
    return if (v == v.toLong().toDouble()) {
        String.format(Locale.US, "%,d", v.toLong())
    } else {
        String.format(Locale.US, "%,.4f", v).trimEnd('0').trimEnd('.')
    }
}
