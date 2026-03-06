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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class NoiseType(val label: String) {
    WHITE("White"),
    BROWN("Brown"),
    PINK("Pink"),
}

private data class SleepTimerOption(val label: String, val minutes: Int)

private val SLEEP_TIMER_OPTIONS = listOf(
    SleepTimerOption("Off", 0),
    SleepTimerOption("15 min", 15),
    SleepTimerOption("30 min", 30),
    SleepTimerOption("60 min", 60),
    SleepTimerOption("90 min", 90),
)

/**
 * Generates pink noise samples using the Voss-McCartney algorithm with 8 octaves.
 */
private class PinkNoiseGenerator(private val octaves: Int = 8) {
    private val values = FloatArray(octaves)
    private var counter = 0

    init {
        for (i in 0 until octaves) {
            values[i] = Random.nextFloat() * 2f - 1f
        }
    }

    fun nextSample(): Float {
        val lastCounter = counter
        counter++

        var sum = 0f
        for (i in 0 until octaves) {
            // Update octave i when bit i of the counter changes
            if ((lastCounter xor counter) and (1 shl i) != 0) {
                values[i] = Random.nextFloat() * 2f - 1f
            }
            sum += values[i]
        }
        return sum / octaves
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhiteNoiseScreen(onBack: () -> Unit) {
    val sampleRate = 44100
    val scope = rememberCoroutineScope()

    var selectedNoiseType by rememberSaveable { mutableStateOf(NoiseType.WHITE) }
    var volume by rememberSaveable { mutableFloatStateOf(0.7f) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var selectedTimerMinutes by rememberSaveable { mutableIntStateOf(0) }
    var timerSecondsRemaining by rememberSaveable { mutableIntStateOf(0) }

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

    fun stopPlayback() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        timerSecondsRemaining = 0
        try {
            audioTrack.pause()
            audioTrack.flush()
        } catch (_: Exception) { }
    }

    fun startPlayback() {
        if (isPlaying) return
        isPlaying = true

        // Start the sleep timer if a duration is selected
        if (selectedTimerMinutes > 0) {
            timerSecondsRemaining = selectedTimerMinutes * 60
        }

        audioTrack.play()
        playJob = scope.launch(Dispatchers.Default) {
            val bufferSize = 2048
            val samples = ShortArray(bufferSize)

            // Brown noise state
            var brownValue = 0f

            // Pink noise generator
            val pinkGenerator = PinkNoiseGenerator(octaves = 8)

            while (isActive) {
                val currentVolume = volume
                val currentNoiseType = selectedNoiseType

                for (i in samples.indices) {
                    val rawSample: Float = when (currentNoiseType) {
                        NoiseType.WHITE -> {
                            Random.nextFloat() * 2f - 1f
                        }
                        NoiseType.BROWN -> {
                            // Random walk with normalization and clamping
                            brownValue += (Random.nextFloat() * 2f - 1f) * 0.02f
                            // Low-pass: clamp to prevent drift
                            brownValue = brownValue.coerceIn(-1f, 1f)
                            // Slight leak toward zero to keep it centered
                            brownValue *= 0.998f
                            brownValue
                        }
                        NoiseType.PINK -> {
                            pinkGenerator.nextSample()
                        }
                    }

                    val scaled = (rawSample * currentVolume * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                    samples[i] = scaled
                }
                audioTrack.write(samples, 0, bufferSize)
            }
        }
    }

    // Sleep timer countdown
    LaunchedEffect(isPlaying, timerSecondsRemaining) {
        if (isPlaying && timerSecondsRemaining > 0) {
            delay(1000L)
            timerSecondsRemaining--
            if (timerSecondsRemaining <= 0) {
                stopPlayback()
            }
        }
    }

    // Clean up AudioTrack on dispose
    DisposableEffect(Unit) {
        onDispose {
            playJob?.cancel()
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) { }
        }
    }

    ToolScreenScaffold(title = "White Noise", onBack = {
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

            // Status display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isPlaying) selectedNoiseType.label + " Noise" else "Ready",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isPlaying) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isPlaying && timerSecondsRemaining > 0) {
                    val minutes = timerSecondsRemaining / 60
                    val seconds = timerSecondsRemaining % 60
                    Text(
                        text = "Sleep in %02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                        ),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                    )
                } else {
                    Text(
                        text = if (isPlaying) "Playing" else "Tap play to start",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Noise type selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "NOISE TYPE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NoiseType.entries.forEach { noiseType ->
                        FilterChip(
                            selected = selectedNoiseType == noiseType,
                            onClick = { selectedNoiseType = noiseType },
                            label = {
                                Text(
                                    text = noiseType.label,
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
                                selected = selectedNoiseType == noiseType,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Sleep timer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "SLEEP TIMER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SLEEP_TIMER_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = selectedTimerMinutes == option.minutes,
                            onClick = {
                                selectedTimerMinutes = option.minutes
                                // Update running timer if playback is active
                                if (isPlaying) {
                                    timerSecondsRemaining = if (option.minutes > 0) {
                                        option.minutes * 60
                                    } else {
                                        0
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = option.label,
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
                                selected = selectedTimerMinutes == option.minutes,
                            ),
                        )
                    }
                }
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
                    Text(
                        text = "\u25A0",
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
                text = if (isPlaying) {
                    "Playing \u00B7 ${selectedNoiseType.label} Noise"
                } else {
                    "Tap to play"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isPlaying) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
