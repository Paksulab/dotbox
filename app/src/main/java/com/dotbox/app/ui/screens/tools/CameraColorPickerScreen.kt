package com.dotbox.app.ui.screens.tools

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraColorPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }

    var pickedRed by rememberSaveable { mutableIntStateOf(128) }
    var pickedGreen by rememberSaveable { mutableIntStateOf(128) }
    var pickedBlue by rememberSaveable { mutableIntStateOf(128) }
    var isLocked by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Derived color values
    val currentColor = Color(pickedRed, pickedGreen, pickedBlue)
    val hexString = "#%02X%02X%02X".format(pickedRed, pickedGreen, pickedBlue)
    val rgbString = "rgb($pickedRed, $pickedGreen, $pickedBlue)"

    // HSL computation
    val r = pickedRed / 255f
    val g = pickedGreen / 255f
    val b = pickedBlue / 255f
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

    ToolScreenScaffold(title = "Camera Color Picker", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (!hasPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Camera permission required",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                // Camera preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            isLocked = !isLocked
                        },
                ) {
                    val executor = remember { Executors.newSingleThreadExecutor() }

                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(executor) { imageProxy ->
                                                if (!isLocked) {
                                                    try {
                                                        val yBuffer = imageProxy.planes[0].buffer
                                                        val uBuffer = imageProxy.planes[1].buffer
                                                        val vBuffer = imageProxy.planes[2].buffer

                                                        val centerX = imageProxy.width / 2
                                                        val centerY = imageProxy.height / 2

                                                        val yIndex = centerY * imageProxy.planes[0].rowStride + centerX
                                                        val uvIndex = (centerY / 2) * imageProxy.planes[1].rowStride + (centerX / 2) * imageProxy.planes[1].pixelStride

                                                        val y = (yBuffer.get(yIndex).toInt() and 0xFF).toFloat()
                                                        val u = (uBuffer.get(uvIndex).toInt() and 0xFF).toFloat() - 128f
                                                        val v = (vBuffer.get(uvIndex).toInt() and 0xFF).toFloat() - 128f

                                                        val newR = (y + 1.370705f * v).coerceIn(0f, 255f).toInt()
                                                        val newG = (y - 0.337633f * u - 0.698001f * v).coerceIn(0f, 255f).toInt()
                                                        val newB = (y + 1.732446f * u).coerceIn(0f, 255f).toInt()

                                                        pickedRed = newR
                                                        pickedGreen = newG
                                                        pickedBlue = newB
                                                    } catch (_: Exception) {
                                                        // Buffer access may fail on some frames
                                                    }
                                                }
                                                imageProxy.close()
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis,
                                        )
                                    } catch (_: Exception) {
                                        // Camera binding failed
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Crosshair overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val crosshairSize = 40.dp.toPx()

                        // Shadow lines
                        val shadowColor = Color.Black.copy(alpha = 0.5f)
                        val shadowStroke = Stroke(width = 3.dp.toPx())
                        // Horizontal shadow
                        drawLine(
                            color = shadowColor,
                            start = Offset(cx - crosshairSize, cy),
                            end = Offset(cx + crosshairSize, cy),
                            strokeWidth = shadowStroke.width,
                        )
                        // Vertical shadow
                        drawLine(
                            color = shadowColor,
                            start = Offset(cx, cy - crosshairSize),
                            end = Offset(cx, cy + crosshairSize),
                            strokeWidth = shadowStroke.width,
                        )

                        // White lines
                        val lineStroke = Stroke(width = 1.5.dp.toPx())
                        // Horizontal
                        drawLine(
                            color = Color.White,
                            start = Offset(cx - crosshairSize, cy),
                            end = Offset(cx + crosshairSize, cy),
                            strokeWidth = lineStroke.width,
                        )
                        // Vertical
                        drawLine(
                            color = Color.White,
                            start = Offset(cx, cy - crosshairSize),
                            end = Offset(cx, cy + crosshairSize),
                            strokeWidth = lineStroke.width,
                        )

                        // Center circle
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 6.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.dp.toPx()),
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx()),
                        )
                    }

                    // Locked badge
                    if (isLocked) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onTertiary,
                            )
                            Text(
                                text = "LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom panel with color info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Color circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(currentColor)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                ),
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLocked) "Color locked - tap preview to unlock" else "Tap preview to lock color",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HEX row
                    CameraColorValueRow(label = "HEX", value = hexString) { copyToClipboard(hexString) }
                    // RGB row
                    CameraColorValueRow(label = "RGB", value = rgbString) { copyToClipboard(rgbString) }
                    // HSL row
                    CameraColorValueRow(label = "HSL", value = hslString) { copyToClipboard(hslString) }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CameraColorValueRow(label: String, value: String, onCopy: () -> Unit) {
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
