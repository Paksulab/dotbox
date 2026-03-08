package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.screens.settings.animationsEnabled
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.delay

private enum class FlashMode(val label: String) {
    STEADY("Steady"),
    STROBE("Strobe"),
    SOS("SOS"),
}

@Composable
fun FlashlightScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val animEnabled = animationsEnabled(context)
    var isOn by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val currentMode = FlashMode.entries[mode]

    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun setTorch(on: Boolean) {
        try {
            if (cameraId.isNotEmpty()) {
                cameraManager.setTorchMode(cameraId, on)
            }
        } catch (_: Exception) {}
    }

    // SOS pattern: ... --- ...
    val sosPattern = remember {
        listOf(
            200L, 200L, 200L, 200L, 200L, 400L, // S: dit dit dit
            600L, 200L, 600L, 200L, 600L, 400L, // O: dah dah dah
            200L, 200L, 200L, 200L, 200L, 800L, // S: dit dit dit + pause
        )
    }

    // Handle flash modes
    LaunchedEffect(isOn, currentMode) {
        if (!isOn) {
            setTorch(false)
            return@LaunchedEffect
        }

        when (currentMode) {
            FlashMode.STEADY -> setTorch(true)
            FlashMode.STROBE -> {
                while (isOn) {
                    setTorch(true)
                    delay(50)
                    setTorch(false)
                    delay(50)
                }
            }
            FlashMode.SOS -> {
                while (isOn) {
                    for (i in sosPattern.indices) {
                        if (!isOn) break
                        setTorch(i % 2 == 0) // even = on, odd = off
                        delay(sosPattern[i])
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { setTorch(false) }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (isOn) NothingRed else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "flashColor",
    )

    val glowTransition = rememberInfiniteTransition(label = "flashGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (currentMode) {
                    FlashMode.STEADY -> 2000
                    FlashMode.STROBE -> 100
                    FlashMode.SOS -> 600
                }
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    ToolScreenScaffold(title = "Flashlight", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mode display
            Text(
                text = currentMode.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isOn) "ON" else "OFF",
                style = MaterialTheme.typography.displaySmall,
                color = if (isOn) NothingRed else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Big toggle button with glow
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Radial glow behind button
                if (isOn && animEnabled) {
                    val glowColor = NothingRed
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = glowAlpha * 0.6f),
                                    glowColor.copy(alpha = glowAlpha * 0.2f),
                                    glowColor.copy(alpha = 0f),
                                ),
                                center = center,
                                radius = size.minDimension / 2,
                            ),
                        )
                    }
                }

                LargeFloatingActionButton(
                    onClick = { isOn = !isOn },
                    shape = CircleShape,
                    containerColor = buttonColor,
                    contentColor = if (isOn) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(120.dp),
                ) {
                    Icon(
                        imageVector = if (isOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = if (isOn) "Turn off" else "Turn on",
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Mode selection
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlashMode.entries.forEachIndexed { index, flashMode ->
                    FilledTonalButton(
                        onClick = { mode = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (mode == index) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Text(
                            text = flashMode.label,
                            color = if (mode == index) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}
