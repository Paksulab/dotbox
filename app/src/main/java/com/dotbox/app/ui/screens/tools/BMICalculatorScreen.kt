package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale
import kotlin.math.pow

private enum class UnitSystem(val label: String) {
    Metric("Metric"),
    Imperial("Imperial"),
}

private enum class BmiCategory(val label: String, val color: Color) {
    Underweight("Underweight", Color(0xFF42A5F5)),
    Normal("Normal", Color(0xFF66BB6A)),
    Overweight("Overweight", Color(0xFFFFB74D)),
    Obese("Obese", Color(0xFFEF5350)),
}

private fun classifyBmi(bmi: Double): BmiCategory = when {
    bmi < 18.5 -> BmiCategory.Underweight
    bmi < 25.0 -> BmiCategory.Normal
    bmi < 30.0 -> BmiCategory.Overweight
    else -> BmiCategory.Obese
}

@Composable
fun BMICalculatorScreen(onBack: () -> Unit) {
    var unitSystem by rememberSaveable { mutableStateOf(UnitSystem.Metric.name) }
    val isMetric = unitSystem == UnitSystem.Metric.name

    // Metric inputs
    var weightKg by rememberSaveable { mutableStateOf("") }
    var heightCm by rememberSaveable { mutableStateOf("") }

    // Imperial inputs
    var weightLbs by rememberSaveable { mutableStateOf("") }
    var heightFt by rememberSaveable { mutableStateOf("") }
    var heightIn by rememberSaveable { mutableStateOf("") }

    // Compute metric values regardless of unit system
    val weightMetric by androidx.compose.runtime.remember(isMetric, weightKg, weightLbs) {
        derivedStateOf {
            if (isMetric) {
                weightKg.toDoubleOrNull() ?: 0.0
            } else {
                (weightLbs.toDoubleOrNull() ?: 0.0) * 0.453592
            }
        }
    }

    val heightMetric by androidx.compose.runtime.remember(isMetric, heightCm, heightFt, heightIn) {
        derivedStateOf {
            if (isMetric) {
                (heightCm.toDoubleOrNull() ?: 0.0) / 100.0 // meters
            } else {
                val feet = heightFt.toDoubleOrNull() ?: 0.0
                val inches = heightIn.toDoubleOrNull() ?: 0.0
                (feet * 12.0 + inches) * 0.0254 // meters
            }
        }
    }

    val bmi by androidx.compose.runtime.remember(weightMetric, heightMetric) {
        derivedStateOf {
            if (weightMetric > 0 && heightMetric > 0) {
                weightMetric / heightMetric.pow(2)
            } else 0.0
        }
    }

    val bmiPrime by androidx.compose.runtime.remember(bmi) {
        derivedStateOf { if (bmi > 0) bmi / 25.0 else 0.0 }
    }

    val ponderalIndex by androidx.compose.runtime.remember(weightMetric, heightMetric) {
        derivedStateOf {
            if (weightMetric > 0 && heightMetric > 0) {
                weightMetric / heightMetric.pow(3)
            } else 0.0
        }
    }

    val hasResult = bmi > 0
    val category = if (hasResult) classifyBmi(bmi) else null

    ToolScreenScaffold(title = "BMI Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Unit toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UnitSystem.entries.forEach { system ->
                    FilterChip(
                        selected = unitSystem == system.name,
                        onClick = { unitSystem = system.name },
                        label = { Text(system.label) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input fields
            if (isMetric) {
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight") },
                    suffix = { Text("kg") },
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
                    value = heightCm,
                    onValueChange = { heightCm = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Height") },
                    suffix = { Text("cm") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            } else {
                OutlinedTextField(
                    value = weightLbs,
                    onValueChange = { weightLbs = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight") },
                    suffix = { Text("lbs") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = heightFt,
                        onValueChange = { heightFt = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Feet") },
                        suffix = { Text("ft") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    OutlinedTextField(
                        value = heightIn,
                        onValueChange = { heightIn = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Inches") },
                        suffix = { Text("in") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Arc gauge
                BmiGauge(
                    bmi = bmi,
                    category = category,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category label with color indicator
                if (category != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(category.color),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = category.color,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    BmiRow("BMI", fmtBmi(bmi))
                    BmiRow("BMI Prime", fmtBmi(bmiPrime))
                    BmiRow(
                        "Ponderal Index",
                        "${fmtBmi(ponderalIndex)} kg/m\u00B3",
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Range reference
                    Text(
                        text = "BMI Ranges",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BmiRangeRow("Underweight", "< 18.5", BmiCategory.Underweight.color)
                    BmiRangeRow("Normal", "18.5 – 24.9", BmiCategory.Normal.color)
                    BmiRangeRow("Overweight", "25.0 – 29.9", BmiCategory.Overweight.color)
                    BmiRangeRow("Obese", "\u2265 30.0", BmiCategory.Obese.color)
                } else {
                    Text(
                        text = "Enter weight and height\nto calculate BMI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BmiGauge(bmi: Double, category: BmiCategory?) {
    val bgColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface
    val accentColor = category?.color ?: MaterialTheme.colorScheme.onSurfaceVariant

    // Map BMI to gauge: 10–50 range across 270 degrees
    val clampedBmi = bmi.coerceIn(10.0, 50.0)
    val fraction = if (bmi > 0) ((clampedBmi - 10.0) / 40.0).toFloat() else 0f

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(
                color = bgColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // Value arc
            if (bmi > 0) {
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = 270f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (bmi > 0) fmtBmi(bmi) else "—",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = if (bmi > 0) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "BMI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BmiRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BmiRangeRow(label: String, range: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun fmtBmi(v: Double): String = String.format(Locale.US, "%.1f", v)
