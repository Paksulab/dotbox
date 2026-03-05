package com.dotbox.app.ui.screens.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class Waveform(val label: String) {
    SINE("Sine"),
    SQUARE("Square"),
    SAWTOOTH("Sawtooth"),
}

private data class MusicalNote(val name: String, val frequency: Double)

private val STANDARD_NOTES: List<MusicalNote> = buildList {
    val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    // Generate notes from C0 to B8
    for (octave in 0..8) {
        for ((semitone, noteName) in noteNames.withIndex()) {
            // A4 = 440Hz is the 49th key (index 48 from A0)
            // MIDI note number for C0 = 12, A4 = 69
            val midiNote = 12 + octave * 12 + semitone
            val freq = 440.0 * 2.0.pow((midiNote - 69).toDouble() / 12.0)
            if (freq in 20.0..20000.0) {
                add(MusicalNote("$noteName$octave", freq))
            }
        }
    }
}

private fun findClosestNote(frequency: Double): MusicalNote? {
    val closest = STANDARD_NOTES.minByOrNull { abs(it.frequency - frequency) } ?: return null
    // Only return if within ~50 cents (about 3% tolerance)
    val ratio = frequency / closest.frequency
    return if (ratio in 0.97..1.03) closest else null
}

private fun getFrequencyCategory(freq: Double): String = when {
    freq < 60 -> "Sub-bass"
    freq < 250 -> "Bass"
    freq < 500 -> "Low-mid"
    freq < 2000 -> "Mid"
    freq < 4000 -> "Upper-mid"
    freq < 6000 -> "Presence"
    else -> "Treble"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FrequencyGeneratorScreen(onBack: () -> Unit) {
    val sampleRate = 44100
    val scope = rememberCoroutineScope()

    // Logarithmic slider: 0..1 maps to 20..20000 Hz
    var sliderPosition by rememberSaveable { mutableFloatStateOf(0.5f) }
    var selectedWaveform by rememberSaveable { mutableStateOf(Waveform.SINE) }
    var volume by rememberSaveable { mutableFloatStateOf(0.5f) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    // Logarithmic mapping: position 0->20Hz, position 1->20000Hz
    val minLog = ln(20.0)
    val maxLog = ln(20000.0)
    val frequency = kotlin.math.exp(minLog + sliderPosition * (maxLog - minLog))
    val freqHz = frequency.roundToInt()

    val closestNote = findClosestNote(frequency)
    val category = getFrequencyCategory(frequency)

    // AudioTrack instance
    val audioTrack = remember {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    var playJob by remember { mutableStateOf<Job?>(null) }

    fun startPlayback() {
        if (isPlaying) return
        isPlaying = true
        audioTrack.play()
        playJob = scope.launch(Dispatchers.Default) {
            val bufferSize = 2048
            val samples = ShortArray(bufferSize)
            var sampleIndex = 0L

            while (isActive) {
                val currentFreq = kotlin.math.exp(minLog + sliderPosition * (maxLog - minLog))
                val currentVolume = volume
                val currentWaveform = selectedWaveform

                for (i in samples.indices) {
                    val t = sampleIndex + i
                    val rawSample = when (currentWaveform) {
                        Waveform.SINE -> {
                            sin(2.0 * Math.PI * currentFreq * t / sampleRate)
                        }
                        Waveform.SQUARE -> {
                            if (sin(2.0 * Math.PI * currentFreq * t / sampleRate) > 0) 1.0 else -1.0
                        }
                        Waveform.SAWTOOTH -> {
                            val phase = t * currentFreq / sampleRate
                            2.0 * (phase - floor(phase + 0.5))
                        }
                    }
                    samples[i] = (rawSample * currentVolume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                sampleIndex += bufferSize
                audioTrack.write(samples, 0, bufferSize)
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack.pause()
            audioTrack.flush()
        } catch (_: Exception) { }
    }

    // Clean up AudioTrack on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) { }
        }
    }

    ToolScreenScaffold(title = "Frequency Generator", onBack = {
        stopPlayback()
        onBack()
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Frequency display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "%,d Hz".format(freqHz),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (closestNote != null) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = closestNote.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "FREQUENCY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "20 Hz",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "20 kHz",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "PRESETS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    data class Preset(val label: String, val freq: Double)
                    val presets = listOf(
                        Preset("100 Hz", 100.0),
                        Preset("C4 · 261 Hz", 261.63),
                        Preset("A4 · 440 Hz", 440.0),
                        Preset("1 kHz", 1000.0),
                        Preset("10 kHz", 10000.0),
                    )
                    presets.forEach { preset ->
                        Button(
                            onClick = {
                                sliderPosition = ((ln(preset.freq) - minLog) / (maxLog - minLog)).toFloat()
                                    .coerceIn(0f, 1f)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waveform selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "WAVEFORM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Waveform.entries.forEach { waveform ->
                        FilterChip(
                            selected = selectedWaveform == waveform,
                            onClick = { selectedWaveform = waveform },
                            label = {
                                Text(
                                    text = waveform.label,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                enabled = true,
                                selected = selectedWaveform == waveform,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "VOLUME",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(volume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Play/Stop button
            LargeFloatingActionButton(
                onClick = {
                    if (isPlaying) stopPlayback() else startPlayback()
                },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = if (isPlaying) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isPlaying) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ) {
                if (isPlaying) {
                    // Stop icon (square-ish representation)
                    Text(
                        text = "■",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPlaying) "Playing · ${selectedWaveform.label} · %,d Hz".format(freqHz) else "Tap to play",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
