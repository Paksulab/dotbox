package com.dotbox.app.ui.screens.tools

import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlin.math.sqrt

@Composable
fun ScreenInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    val metrics = DisplayMetrics()
    @Suppress("DEPRECATION")
    display?.getRealMetrics(metrics)

    val widthPixels = metrics.widthPixels
    val heightPixels = metrics.heightPixels
    val densityDpi = metrics.densityDpi
    val xdpi = metrics.xdpi
    val ydpi = metrics.ydpi
    val density = metrics.density

    val densityBucket = when {
        densityDpi <= 120 -> "ldpi"
        densityDpi <= 160 -> "mdpi"
        densityDpi <= 240 -> "hdpi"
        densityDpi <= 320 -> "xhdpi"
        densityDpi <= 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }

    val widthInches = widthPixels / xdpi.toDouble()
    val heightInches = heightPixels / ydpi.toDouble()
    val diagonalInches = sqrt(widthInches * widthInches + heightInches * heightInches)

    @Suppress("DEPRECATION")
    val refreshRate = display?.refreshRate ?: 0f

    val hdrSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        display?.isHdr
    } else {
        null
    }

    val wideColorGamut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        configuration.isScreenWideColorGamut
    } else {
        null
    }

    val orientation = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> "Portrait"
        Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
        else -> "Undefined"
    }

    val nightMode = when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> "Enabled"
        Configuration.UI_MODE_NIGHT_NO -> "Disabled"
        else -> "Undefined"
    }

    val fontScale = configuration.fontScale
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // Build section data
    val displaySection = listOf(
        "Resolution" to "${widthPixels} × ${heightPixels} px",
        "DPI" to "$densityDpi (x: ${"%.1f".format(xdpi)}, y: ${"%.1f".format(ydpi)})",
        "Density" to "${"%.2f".format(density)}x ($densityBucket)",
        "Screen Size" to "${"%.2f".format(diagonalInches)}\" diagonal",
        "Refresh Rate" to "${"%.1f".format(refreshRate)} Hz"
    )

    val colorSection = buildList {
        if (hdrSupported != null) {
            add("HDR" to if (hdrSupported) "Supported" else "Not supported")
        } else {
            add("HDR" to "Requires API 26+")
        }
        if (wideColorGamut != null) {
            add("Wide Color Gamut" to if (wideColorGamut) "Supported" else "Not supported")
        } else {
            add("Wide Color Gamut" to "Requires API 26+")
        }
        add("Night Mode" to nightMode)
    }

    val layoutSection = listOf(
        "Orientation" to orientation,
        "Screen Size (dp)" to "${screenWidthDp} × ${screenHeightDp} dp",
        "Font Scale" to "${"%.2f".format(fontScale)}x"
    )

    ToolScreenScaffold(
        title = "Screen Info",
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
            item {
                InfoSectionCard(title = "Display", rows = displaySection)
            }
            item {
                InfoSectionCard(title = "Color", rows = colorSection)
            }
            item {
                InfoSectionCard(title = "Layout", rows = layoutSection)
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = JetBrainsMono,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
