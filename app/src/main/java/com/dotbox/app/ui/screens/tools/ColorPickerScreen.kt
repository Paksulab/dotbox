package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

@Composable
fun ColorPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var red by rememberSaveable { mutableFloatStateOf(214f) }
    var green by rememberSaveable { mutableFloatStateOf(47f) }
    var blue by rememberSaveable { mutableFloatStateOf(47f) }

    val currentColor = Color(red.toInt(), green.toInt(), blue.toInt())
    val hexString = "#%02X%02X%02X".format(red.toInt(), green.toInt(), blue.toInt())
    val rgbString = "rgb(${red.toInt()}, ${green.toInt()}, ${blue.toInt()})"

    // HSL values
    val r = red / 255f
    val g = green / 255f
    val b = blue / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val lightness = (max + min) / 2f
    val saturation = if (max == min) 0f else if (lightness <= 0.5f) (max - min) / (max + min) else (max - min) / (2f - max - min)
    val hue = when {
        max == min -> 0f
        max == r -> ((g - b) / (max - min) * 60f + 360f) % 360f
        max == g -> (b - r) / (max - min) * 60f + 120f
        else -> (r - g) / (max - min) * 60f + 240f
    }
    val hslString = "hsl(${hue.toInt()}, ${(saturation * 100).toInt()}%, ${(lightness * 100).toInt()}%)"

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

            // Red slider
            ColorSlider(
                label = "R",
                value = red,
                onValueChange = { red = it },
                activeColor = Color.Red,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Green slider
            ColorSlider(
                label = "G",
                value = green,
                onValueChange = { green = it },
                activeColor = Color.Green,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Blue slider
            ColorSlider(
                label = "B",
                value = blue,
                onValueChange = { blue = it },
                activeColor = Color(0xFF4488FF),
            )

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
            valueRange = 0f..255f,
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
            modifier = Modifier.width(36.dp),
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
