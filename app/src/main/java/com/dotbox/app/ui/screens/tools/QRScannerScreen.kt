package com.dotbox.app.ui.screens.tools

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var scannedValue by rememberSaveable { mutableStateOf<String?>(null) }
    var scannedFormat by rememberSaveable { mutableStateOf("") }
    var isTorchOn by rememberSaveable { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

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

    LaunchedEffect(isTorchOn) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }

    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", text))
    }

    fun isUrl(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }

    fun openUrl(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) { }
    }

    ToolScreenScaffold(title = "QR Scanner", onBack = onBack) { paddingValues ->
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
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    val executor = remember { Executors.newSingleThreadExecutor() }
                    val barcodeScanner = remember { BarcodeScanning.getClient() }

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
                                                val mediaImage = imageProxy.image
                                                if (mediaImage != null) {
                                                    val image = InputImage.fromMediaImage(
                                                        mediaImage,
                                                        imageProxy.imageInfo.rotationDegrees,
                                                    )
                                                    barcodeScanner.process(image)
                                                        .addOnSuccessListener { barcodes ->
                                                            barcodes.firstOrNull()?.let { barcode ->
                                                                scannedValue = barcode.rawValue
                                                                scannedFormat = when (barcode.format) {
                                                                    Barcode.FORMAT_QR_CODE -> "QR Code"
                                                                    Barcode.FORMAT_EAN_13 -> "EAN-13"
                                                                    Barcode.FORMAT_EAN_8 -> "EAN-8"
                                                                    Barcode.FORMAT_UPC_A -> "UPC-A"
                                                                    Barcode.FORMAT_UPC_E -> "UPC-E"
                                                                    Barcode.FORMAT_CODE_128 -> "Code 128"
                                                                    Barcode.FORMAT_CODE_39 -> "Code 39"
                                                                    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
                                                                    Barcode.FORMAT_PDF417 -> "PDF417"
                                                                    else -> "Barcode"
                                                                }
                                                            }
                                                        }
                                                        .addOnCompleteListener {
                                                            imageProxy.close()
                                                        }
                                                } else {
                                                    imageProxy.close()
                                                }
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis,
                                        )
                                    } catch (e: Exception) {
                                        // Camera binding failed
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Torch toggle
                    IconButton(
                        onClick = { isTorchOn = !isTorchOn },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                            .size(44.dp),
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Toggle torch",
                            tint = if (isTorchOn) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Result area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    if (scannedValue != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                        ) {
                            Text(
                                text = scannedFormat,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scannedValue!!,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = { copyToClipboard(scannedValue!!) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                if (isUrl(scannedValue!!)) {
                                    Button(
                                        onClick = { openUrl(scannedValue!!) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary,
                                        ),
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open")
                                    }
                                }
                                Button(
                                    onClick = {
                                        scannedValue = null
                                        scannedFormat = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Point camera at a barcode or QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
