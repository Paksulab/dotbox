package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono

private enum class NumberBase(val label: String, val radix: Int) {
    BINARY("BIN", 2),
    OCTAL("OCT", 8),
    DECIMAL("DEC", 10),
    HEXADECIMAL("HEX", 16),
}

@Composable
fun NumberBaseConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var inputText by rememberSaveable { mutableStateOf("") }
    var selectedBase by rememberSaveable { mutableStateOf(NumberBase.DECIMAL) }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Number", text))
    }

    // Parse the input value
    val parsedValue: Long? = try {
        if (inputText.isBlank()) null
        else java.lang.Long.parseLong(inputText.uppercase(), selectedBase.radix)
    } catch (e: NumberFormatException) {
        null
    }

    ToolScreenScaffold(title = "Number Base", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Base selector
            Text(
                text = "INPUT BASE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NumberBase.entries.forEach { base ->
                    FilterChip(
                        selected = selectedBase == base,
                        onClick = {
                            // Convert current value to new base for input
                            if (parsedValue != null) {
                                inputText = parsedValue.toString(base.radix).uppercase()
                            }
                            selectedBase = base
                        },
                        label = { Text(base.label) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { newValue ->
                    // Validate input for selected base
                    val validChars = when (selectedBase) {
                        NumberBase.BINARY -> "01"
                        NumberBase.OCTAL -> "01234567"
                        NumberBase.DECIMAL -> "0123456789"
                        NumberBase.HEXADECIMAL -> "0123456789abcdefABCDEF"
                    }
                    if (newValue.all { it in validChars || it == '-' }) {
                        inputText = newValue
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter ${selectedBase.label} number") },
                placeholder = { Text("0") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
                isError = inputText.isNotBlank() && parsedValue == null,
            )

            if (inputText.isNotBlank() && parsedValue == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Invalid ${selectedBase.label} number",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results
            Text(
                text = "CONVERSIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            NumberBase.entries.forEach { base ->
                val converted = parsedValue?.toString(base.radix)?.uppercase() ?: "—"
                val isSource = base == selectedBase

                BaseResultRow(
                    label = base.label,
                    value = converted,
                    isSource = isSource,
                    onCopy = { if (parsedValue != null) copyToClipboard(converted) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BaseResultRow(
    label: String,
    value: String,
    isSource: Boolean,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSource) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSource) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
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
