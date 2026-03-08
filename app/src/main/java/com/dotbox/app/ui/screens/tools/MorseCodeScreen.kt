package com.dotbox.app.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val morseMap: Map<Char, String> = mapOf(
    'A' to "·−", 'B' to "−···", 'C' to "−·−·", 'D' to "−··",
    'E' to "·", 'F' to "··−·", 'G' to "−−·", 'H' to "····",
    'I' to "··", 'J' to "·−−−", 'K' to "−·−", 'L' to "·−··",
    'M' to "−−", 'N' to "−·", 'O' to "−−−", 'P' to "·−−·",
    'Q' to "−−·−", 'R' to "·−·", 'S' to "···", 'T' to "−",
    'U' to "··−", 'V' to "···−", 'W' to "·−−", 'X' to "−··−",
    'Y' to "−·−−", 'Z' to "−−··",
    '0' to "−−−−−", '1' to "·−−−−", '2' to "··−−−", '3' to "···−−",
    '4' to "····−", '5' to "·····", '6' to "−····", '7' to "−−···",
    '8' to "−−−··", '9' to "−−−−·",
)

private val reverseMorseMap: Map<String, Char> = morseMap.entries.associate { (k, v) -> v to k }

// Also support plain dot/dash input (. and -)
private val reverseAsciiMorseMap: Map<String, Char> by lazy {
    morseMap.entries.associate { (k, v) ->
        v.replace('·', '.').replace('−', '-') to k
    }
}

private fun textToMorse(text: String): String {
    return text.uppercase().map { ch ->
        when {
            ch == ' ' -> "//"
            morseMap.containsKey(ch) -> morseMap[ch]!!
            else -> ""
        }
    }.filter { it.isNotEmpty() }.joinToString(" / ") { segment ->
        if (segment == "//") "//" else segment
    }.replace("/ // /", " // ")
}

private fun morseToText(morse: String): String {
    if (morse.isBlank()) return ""
    // Normalize: replace Unicode dots/dashes with ASCII, then split
    val normalized = morse.trim()
    // Split by word separator (double slash or 7+ spaces)
    val words = normalized.split(Regex("\\s*//\\s*|\\s{3,}"))
    return words.joinToString(" ") { word ->
        // Split by letter separator (single slash or 3 spaces)
        val letters = word.split(Regex("\\s*/\\s*|\\s+"))
        letters.mapNotNull { symbol ->
            val trimmed = symbol.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            // Try both Unicode and ASCII morse
            reverseMorseMap[trimmed]
                ?: reverseAsciiMorseMap[trimmed]
                ?: '?'
        }.joinToString("")
    }
}

private enum class MorseMode(val label: String) {
    TEXT_TO_MORSE("Text → Morse"),
    MORSE_TO_TEXT("Morse → Text"),
}

@Composable
fun MorseCodeScreen(onBack: () -> Unit) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = MorseMode.entries[modeIndex]
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var textInput by rememberSaveable { mutableStateOf("") }
    var morseInput by rememberSaveable { mutableStateOf("") }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var outputModeIndex by rememberSaveable { mutableIntStateOf(0) } // 0=Sound, 1=Vibrate, 2=Both
    val outputModes = listOf("Sound", "Vibrate", "Both")

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val morseOutput = remember(textInput) {
        if (textInput.isNotBlank()) textToMorse(textInput) else ""
    }

    val textOutput = remember(morseInput) {
        if (morseInput.isNotBlank()) morseToText(morseInput) else ""
    }

    val displayOutput = when (mode) {
        MorseMode.TEXT_TO_MORSE -> morseOutput
        MorseMode.MORSE_TO_TEXT -> textOutput
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Morse Code", text))
    }

    fun vibrateMs(ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    fun playMorse(morse: String) {
        if (morse.isBlank() || isPlaying) return
        isPlaying = true
        val useSound = outputModeIndex == 0 || outputModeIndex == 2
        val useVibrate = outputModeIndex == 1 || outputModeIndex == 2
        scope.launch {
            withContext(Dispatchers.Default) {
                val toneGenerator = if (useSound) {
                    try {
                        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    } catch (_: Exception) { null }
                } else null

                try {
                    val chars = morse.toCharArray()
                    var i = 0
                    while (i < chars.size) {
                        val ch = chars[i]
                        when {
                            ch == '·' || ch == '.' -> {
                                if (useSound) toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 100)
                                if (useVibrate) vibrateMs(100)
                                delay(100)
                                delay(100) // gap between symbols
                            }
                            ch == '−' || ch == '-' -> {
                                if (useSound) toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 300)
                                if (useVibrate) vibrateMs(300)
                                delay(300)
                                delay(100) // gap between symbols
                            }
                            ch == '/' -> {
                                if (i + 1 < chars.size && chars[i + 1] == '/') {
                                    delay(700) // word gap
                                    i++ // skip second slash
                                } else {
                                    delay(300) // letter gap
                                }
                            }
                            ch == ' ' -> {
                                // spaces are part of separators, handled by / logic
                            }
                        }
                        i++
                    }
                } finally {
                    toneGenerator?.release()
                    isPlaying = false
                }
            }
        }
    }

    ToolScreenScaffold(title = "Morse Code", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MorseMode.entries.forEachIndexed { idx, m ->
                    FilterChip(
                        selected = modeIndex == idx,
                        onClick = { modeIndex = idx },
                        label = { Text(m.label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Output mode chips (Sound / Vibrate / Both)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                outputModes.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = outputModeIndex == idx,
                        onClick = { outputModeIndex = idx },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field
            when (mode) {
                MorseMode.TEXT_TO_MORSE -> {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Enter text") },
                        placeholder = { Text("Hello World") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                }
                MorseMode.MORSE_TO_TEXT -> {
                    OutlinedTextField(
                        value = morseInput,
                        onValueChange = { morseInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Enter morse code") },
                        placeholder = {
                            Text(
                                "· · · · / · / · − · · / · − · · / − − −",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use . and - for dots and dashes, / between letters, // between words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Output
            if (displayOutput.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    Text(
                        text = if (mode == MorseMode.TEXT_TO_MORSE) "Morse Code" else "Decoded Text",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = displayOutput,
                        style = if (mode == MorseMode.TEXT_TO_MORSE) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp,
                            )
                        } else {
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { copyToClipboard(displayOutput) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            val morseToPlay = if (mode == MorseMode.TEXT_TO_MORSE) morseOutput else {
                                // Convert text output back to morse for playback
                                textToMorse(textOutput)
                            }
                            playMorse(morseToPlay)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isPlaying,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.tertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "Playing..." else "Play ${outputModes[outputModeIndex]}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Reference card
            Text(
                text = "Morse Code Reference",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
            ) {
                // Letters A-Z in rows of 3 pairs
                val letters = morseMap.entries.filter { it.key.isLetter() }.toList()
                val digits = morseMap.entries.filter { it.key.isDigit() }.toList()
                val allEntries = letters + digits

                // Display in rows of 3 columns
                val rows = allEntries.chunked(3)
                rows.forEachIndexed { rowIdx, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        row.forEach { (char, morse) ->
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(20.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = morse,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        // Fill remaining columns if row is incomplete
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
