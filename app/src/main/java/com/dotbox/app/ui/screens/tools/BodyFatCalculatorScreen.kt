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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale
import kotlin.math.log10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyFatCalculatorScreen(onBack: () -> Unit) {
    var isMale by rememberSaveable { mutableStateOf(true) }
    var height by rememberSaveable { mutableStateOf("") }
    var waist by rememberSaveable { mutableStateOf("") }
    var neck by rememberSaveable { mutableStateOf("") }
    var hip by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
        cursorColor = MaterialTheme.colorScheme.tertiary
    )

    val heightVal = height.toDoubleOrNull()
    val waistVal = waist.toDoubleOrNull()
    val neckVal = neck.toDoubleOrNull()
    val hipVal = hip.toDoubleOrNull()
    val weightVal = weight.toDoubleOrNull()

    val bodyFat: Double? = remember(isMale, heightVal, waistVal, neckVal, hipVal) {
        if (heightVal != null && heightVal > 0 && waistVal != null && neckVal != null) {
            if (isMale) {
                if (waistVal > neckVal) {
                    86.010 * log10(waistVal - neckVal) - 70.041 * log10(heightVal) + 36.76
                } else null
            } else {
                if (hipVal != null && (waistVal + hipVal - neckVal) > 0) {
                    163.205 * log10(waistVal + hipVal - neckVal) - 97.684 * log10(heightVal) - 78.387
                } else null
            }
        } else null
    }

    data class CategoryInfo(val name: String, val color: Color)

    fun getCategory(bf: Double, male: Boolean): CategoryInfo {
        return if (male) {
            when {
                bf < 6.0 -> CategoryInfo("Essential", Color(0xFFFF5252))
                bf < 14.0 -> CategoryInfo("Athletes", Color(0xFF4CAF50))
                bf < 18.0 -> CategoryInfo("Fitness", Color(0xFF66BB6A))
                bf < 25.0 -> CategoryInfo("Average", Color(0xFFFFA726))
                else -> CategoryInfo("Obese", Color(0xFFFF5252))
            }
        } else {
            when {
                bf < 14.0 -> CategoryInfo("Essential", Color(0xFFFF5252))
                bf < 21.0 -> CategoryInfo("Athletes", Color(0xFF4CAF50))
                bf < 25.0 -> CategoryInfo("Fitness", Color(0xFF66BB6A))
                bf < 32.0 -> CategoryInfo("Average", Color(0xFFFFA726))
                else -> CategoryInfo("Obese", Color(0xFFFF5252))
            }
        }
    }

    ToolScreenScaffold(
        title = "Body Fat % Calculator",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Gender toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = isMale,
                    onClick = { isMale = true },
                    label = { Text("Male") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                FilterChip(
                    selected = !isMale,
                    onClick = { isMale = false },
                    label = { Text("Female") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }

            // Height
            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Waist
            OutlinedTextField(
                value = waist,
                onValueChange = { waist = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Waist at navel (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Neck
            OutlinedTextField(
                value = neck,
                onValueChange = { neck = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Neck (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Hip (female only)
            if (!isMale) {
                OutlinedTextField(
                    value = hip,
                    onValueChange = { hip = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Hip (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Weight (optional)
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (kg) — optional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Results
            if (bodyFat != null) {
                val clampedBf = bodyFat.coerceIn(0.0, 100.0)
                val category = getCategory(clampedBf, isMale)
                val leanMass = 100.0 - clampedBf

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Body Fat",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = String.format(Locale.US, "%.1f%%", clampedBf),
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = category.color
                        )

                        // Category chip
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = category.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = category.color
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        // Lean mass %
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lean Mass",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%%", leanMass),
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Fat mass in kg (if weight provided)
                        if (weightVal != null && weightVal > 0) {
                            val fatMassKg = weightVal * clampedBf / 100.0
                            val leanMassKg = weightVal - fatMassKg

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fat Mass",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f kg", fatMassKg),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Lean Mass",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f kg", leanMassKg),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
