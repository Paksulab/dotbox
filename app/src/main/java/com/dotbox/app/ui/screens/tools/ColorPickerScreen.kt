package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlin.math.roundToInt

private fun rgbToHsl(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    val max = maxOf(rf, gf, bf)
    val min = minOf(rf, gf, bf)
    val l = (max + min) / 2f
    if (max == min) return Triple(0f, 0f, l * 100f)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when {
        max == rf -> ((gf - bf) / d + (if (gf < bf) 6f else 0f)) * 60f
        max == gf -> ((bf - rf) / d + 2f) * 60f
        else -> ((rf - gf) / d + 4f) * 60f
    }
    return Triple(h, s * 100f, l * 100f)
}

private fun hslToRgb(h: Float, s: Float, l: Float): Triple<Float, Float, Float> {
    val sf = s / 100f
    val lf = l / 100f
    if (sf == 0f) {
        val v = (lf * 255f).roundToInt().toFloat()
        return Triple(v, v, v)
    }
    val hue = h / 360f
    val q = if (lf < 0.5f) lf * (1f + sf) else lf + sf - lf * sf
    val p = 2f * lf - q
    fun hueToRgb(p: Float, q: Float, t: Float): Float {
        var tt = t
        if (tt < 0f) tt += 1f
        if (tt > 1f) tt -= 1f
        return when {
            tt < 1f / 6f -> p + (q - p) * 6f * tt
            tt < 1f / 2f -> q
            tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
            else -> p
        }
    }
    val r = (hueToRgb(p, q, hue + 1f / 3f) * 255f).roundToInt().coerceIn(0, 255).toFloat()
    val g = (hueToRgb(p, q, hue) * 255f).roundToInt().coerceIn(0, 255).toFloat()
    val b = (hueToRgb(p, q, hue - 1f / 3f) * 255f).roundToInt().coerceIn(0, 255).toFloat()
    return Triple(r, g, b)
}

@Composable
fun ColorPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var red by rememberSaveable { mutableFloatStateOf(214f) }
    var green by rememberSaveable { mutableFloatStateOf(47f) }
    var blue by rememberSaveable { mutableFloatStateOf(47f) }
    var inputMode by rememberSaveable { mutableStateOf("RGB") }

    val currentColor = Color(red.toInt(), green.toInt(), blue.toInt())
    val hexString = "#%02X%02X%02X".format(red.toInt(), green.toInt(), blue.toInt())
    val rgbString = "rgb(${red.toInt()}, ${green.toInt()}, ${blue.toInt()})"

    val (hue, saturation, lightness) = rgbToHsl(red, green, blue)
    val hslString = "hsl(${hue.toInt()}, ${saturation.toInt()}%, ${lightness.toInt()}%)"

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Color", text))
    }

    ToolScreenScaffold(title = "Color Picker", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Color preview
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(currentColor)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp),
                    ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color values
            ColorValueRow(label = "HEX", value = hexString) { copyToClipboard(hexString) }
            ColorValueRow(label = "RGB", value = rgbString) { copyToClipboard(rgbString) }
            ColorValueRow(label = "HSL", value = hslString) { copyToClipboard(hslString) }

            Spacer(modifier = Modifier.height(24.dp))

            // Input mode selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilterChip(
                    selected = inputMode == "RGB",
                    onClick = { inputMode = "RGB" },
                    label = { Text("RGB") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                FilterChip(
                    selected = inputMode == "HSL",
                    onClick = { inputMode = "HSL" },
                    label = { Text("HSL") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (inputMode == "RGB") {
                ColorSlider(label = "R", value = red, onValueChange = { red = it }, activeColor = Color.Red, valueRange = 0f..255f)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(label = "G", value = green, onValueChange = { green = it }, activeColor = Color.Green, valueRange = 0f..255f)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(label = "B", value = blue, onValueChange = { blue = it }, activeColor = Color(0xFF4488FF), valueRange = 0f..255f)
            } else {
                ColorSlider(
                    label = "H", value = hue, activeColor = NothingRed, valueRange = 0f..360f,
                    onValueChange = { h ->
                        val (r, g, b) = hslToRgb(h, saturation, lightness)
                        red = r; green = g; blue = b
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(
                    label = "S", value = saturation, activeColor = MaterialTheme.colorScheme.tertiary, valueRange = 0f..100f,
                    onValueChange = { s ->
                        val (r, g, b) = hslToRgb(hue, s, lightness)
                        red = r; green = g; blue = b
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(
                    label = "L", value = lightness, activeColor = MaterialTheme.colorScheme.onSurfaceVariant, valueRange = 0f..100f,
                    onValueChange = { l ->
                        val (r, g, b) = hslToRgb(hue, saturation, l)
                        red = r; green = g; blue = b
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preset colors
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            val presets = listOf(
                Triple(255f, 255f, 255f),   // White
                Triple(0f, 0f, 0f),         // Black
                Triple(214f, 47f, 47f),     // Nothing Red
                Triple(255f, 193f, 7f),     // Amber
                Triple(76f, 175f, 80f),     // Green
                Triple(33f, 150f, 243f),    // Blue
                Triple(156f, 39f, 176f),    // Purple
                Triple(255f, 87f, 34f),     // Deep Orange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                presets.forEach { (pr, pg, pb) ->
                    IconButton(
                        onClick = {
                            red = pr
                            green = pg
                            blue = pb
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(pr.toInt(), pg.toInt(), pb.toInt()))
                                .border(
                                    width = if (red == pr && green == pg && blue == pb) 2.dp else 1.dp,
                                    color = if (red == pr && green == pg && blue == pb) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Color Harmonies
            Text(
                text = "HARMONIES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            val harmonies = listOf(
                "Complementary" to listOf((hue + 180f) % 360f),
                "Analogous" to listOf((hue - 30f + 360f) % 360f, (hue + 30f) % 360f),
                "Triadic" to listOf((hue + 120f) % 360f, (hue + 240f) % 360f),
                "Split-comp." to listOf((hue + 150f) % 360f, (hue + 210f) % 360f),
            )

            harmonies.forEach { (name, hues) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(90.dp),
                    )
                    // Current color swatch
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Harmony colors
                    hues.forEach { harmonyHue ->
                        val (hr, hg, hb) = hslToRgb(harmonyHue, saturation, lightness)
                        val harmonyColor = Color(
                            hr.toInt().coerceIn(0, 255),
                            hg.toInt().coerceIn(0, 255),
                            hb.toInt().coerceIn(0, 255),
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(harmonyColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    red = hr.coerceIn(0f, 255f)
                                    green = hg.coerceIn(0f, 255f)
                                    blue = hb.coerceIn(0f, 255f)
                                },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    valueRange: ClosedFloatingPointRange<Float> = 0f..255f,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
            color = activeColor,
            modifier = Modifier.width(24.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = activeColor.copy(alpha = 0.2f),
            ),
        )
        Text(
            text = value.toInt().toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
    }
}

@Composable
private fun ColorValueRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
