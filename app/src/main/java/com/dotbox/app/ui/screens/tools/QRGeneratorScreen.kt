package com.dotbox.app.ui.screens.tools

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.FileProvider
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.utils.generateQRBitmap
import java.io.File
import java.io.FileOutputStream

@Composable
fun QRGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var inputText by rememberSaveable { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val focusManager = LocalFocusManager.current

    val fgColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val bgColor = MaterialTheme.colorScheme.background.toArgb()

    fun generate() {
        if (inputText.isNotBlank()) {
            qrBitmap = generateQRBitmap(inputText, foregroundColor = fgColor, backgroundColor = bgColor)
            focusManager.clearFocus()
        }
    }

    ToolScreenScaffold(title = "QR Generator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Text, URL, or data") },
                placeholder = { Text("Enter text to encode...") },
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { generate() }),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { generate() },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Generate QR Code", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // QR Code display
            qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share button
                Button(
                    onClick = {
                        try {
                            val cachePath = File(context.cacheDir, "shared_qr.png")
                            FileOutputStream(cachePath).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                cachePath,
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                        } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share QR Code", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Encoded: ${inputText.take(50)}${if (inputText.length > 50) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (qrBitmap == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Enter text and tap Generate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
