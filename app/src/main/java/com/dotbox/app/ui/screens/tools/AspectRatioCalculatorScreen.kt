package com.dotbox.app.ui.screens.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.Locale
import kotlin.math.sqrt

private data class Preset(val name: String, val w: Int, val h: Int)

private val presets = listOf(
    Preset("16:9", 16, 9),
    Preset("4:3", 4, 3),
    Preset("21:9", 21, 9),
    Preset("1:1", 1, 1),
    Preset("3:2", 3, 2),
    Preset("9:16", 9, 16),
)

@Composable
fun AspectRatioCalculatorScreen(onBack: () -> Unit) {
    var widthStr by rememberSaveable { mutableStateOf("1920") }
    var heightStr by rememberSaveable { mutableStateOf("1080") }
    var diagonalStr by rememberSaveable { mutableStateOf("") }

    val w = widthStr.toDoubleOrNull() ?: 0.0
    val h = heightStr.toDoubleOrNull() ?: 0.0

    val gcdVal by androidx.compose.runtime.remember(w, h) {
        derivedStateOf {
            if (w > 0 && h > 0) gcd(w.toLong(), h.toLong()) else 1L
        }
    }

    val ratioW by androidx.compose.runtime.remember(w, gcdVal) {
        derivedStateOf { if (gcdVal > 0) (w / gcdVal).toLong() else 0L }
    }
    val ratioH by androidx.compose.runtime.remember(h, gcdVal) {
        derivedStateOf { if (gcdVal > 0) (h / gcdVal).toLong() else 0L }
    }

    val diagonal by androidx.compose.runtime.remember(w, h) {
        derivedStateOf { sqrt(w * w + h * h) }
    }

    val diag = diagonalStr.toDoubleOrNull()
    val ppi by androidx.compose.runtime.remember(diagonal, diag) {
        derivedStateOf {
            if (diag != null && diag > 0 && diagonal > 0) diagonal / diag else null
        }
    }

    val megapixels by androidx.compose.runtime.remember(w, h) {
        derivedStateOf { w * h / 1_000_000.0 }
    }

    ToolScreenScaffold(title = "Aspect Ratio", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Quick presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = ratioW == preset.w.toLong() && ratioH == preset.h.toLong(),
                        onClick = {
                            // Calculate matching resolution
                            val mult = if (preset.w >= preset.h) 1920.0 / preset.w else 1080.0 / preset.h
                            widthStr = (preset.w * mult).toInt().toString()
                            heightStr = (preset.h * mult).toInt().toString()
                        },
                        label = { Text(preset.name, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Width & Height inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = widthStr,
                    onValueChange = { widthStr = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Width") },
                    suffix = { Text("px") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Height") },
                    suffix = { Text("px") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagonal input (for PPI)
            OutlinedTextField(
                value = diagonalStr,
                onValueChange = { diagonalStr = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Screen diagonal (optional)") },
                suffix = { Text("inches") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visual preview
            if (w > 0 && h > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val previewRatio = (w / h).toFloat().coerceIn(0.3f, 3.5f)
                    val accentColor = MaterialTheme.colorScheme.tertiary

                    Canvas(
                        modifier = Modifier
                            .height(100.dp)
                            .aspectRatio(previewRatio),
                    ) {
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.1f),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            size = size,
                        )
                        drawRoundRect(
                            color = accentColor.copy(alpha = 0.5f),
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            size = size,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                        // Diagonal line
                        drawLine(
                            color = accentColor.copy(alpha = 0.3f),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
            ) {
                Text(
                    text = "RESULTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Ratio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Aspect Ratio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (w > 0 && h > 0) "$ratioW:$ratioH" else "—",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                ARRow("Decimal Ratio", if (w > 0 && h > 0) String.format(Locale.US, "%.4f", w / h) else "—")
                ARRow("Resolution", if (w > 0 && h > 0) "${w.toInt()} × ${h.toInt()}" else "—")
                ARRow("Diagonal (px)", if (w > 0 && h > 0) String.format(Locale.US, "%.1f", diagonal) else "—")
                ARRow("Megapixels", if (w > 0 && h > 0) String.format(Locale.US, "%.2f MP", megapixels) else "—")
                ARRow("Total Pixels", if (w > 0 && h > 0) "%,d".format((w * h).toLong()) else "—")

                if (ppi != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    ARRow("PPI (Pixels/inch)", String.format(Locale.US, "%.1f", ppi))
                    ARRow("Dot Pitch", String.format(Locale.US, "%.4f mm", 25.4 / (ppi ?: 1.0)))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ARRow(label: String, value: String) {
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
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
