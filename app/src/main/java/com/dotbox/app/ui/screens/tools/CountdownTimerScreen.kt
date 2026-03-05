package com.dotbox.app.ui.screens.tools

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CountdownTimerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var eventName by rememberSaveable { mutableStateOf("") }
    var targetDateStr by rememberSaveable {
        mutableStateOf(LocalDate.now().plusDays(7).toString())
    }
    var targetTimeStr by rememberSaveable { mutableStateOf("00:00") }
    var nowMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    val targetDate = LocalDate.parse(targetDateStr)
    val targetTime = LocalTime.parse(targetTimeStr)
    val targetDateTime = LocalDateTime.of(targetDate, targetTime)
    val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    // Tick every second
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val diff = targetMillis - nowMillis
    val isPast = diff <= 0
    val absDiff = kotlin.math.abs(diff)

    val totalSeconds = absDiff / 1000
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    fun pickDate() {
        DatePickerDialog(
            context,
            { _, y, m, d -> targetDateStr = LocalDate.of(y, m + 1, d).toString() },
            targetDate.year, targetDate.monthValue - 1, targetDate.dayOfMonth,
        ).show()
    }

    fun pickTime() {
        TimePickerDialog(
            context,
            { _, h, m -> targetTimeStr = "%02d:%02d".format(h, m) },
            targetTime.hour, targetTime.minute, true,
        ).show()
    }

    ToolScreenScaffold(title = "Countdown", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Event name
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Event name (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date & time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PickerField(
                    label = "Date",
                    value = targetDate.format(dateFmt),
                    onClick = { pickDate() },
                    modifier = Modifier.weight(1f),
                )
                PickerField(
                    label = "Time",
                    value = targetTime.format(timeFmt),
                    onClick = { pickTime() },
                    modifier = Modifier.width(100.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Event name display
            if (eventName.isNotBlank()) {
                Text(
                    text = if (isPast) "$eventName was" else "$eventName in",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Big countdown display
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountdownUnit(days.toString(), "days")
                Text(":", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono), color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountdownUnit("%02d".format(hours), "hrs")
                Text(":", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono), color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountdownUnit("%02d".format(minutes), "min")
                Text(":", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono), color = MaterialTheme.colorScheme.onSurfaceVariant)
                CountdownUnit("%02d".format(seconds), "sec")
            }

            if (isPast) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ago",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
            ) {
                Text(
                    text = "BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                BreakdownRow("Total seconds", "%,d".format(totalSeconds))
                BreakdownRow("Total minutes", "%,d".format(totalSeconds / 60))
                BreakdownRow("Total hours", "%,d".format(totalSeconds / 3600))
                BreakdownRow("Total days", "%,d".format(days))
                BreakdownRow("Total weeks", "%.1f".format(days / 7.0))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick presets
            Text(
                text = "QUICK SET",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("1 hour" to 1L, "24 hours" to 24L, "7 days" to 168L, "30 days" to 720L).forEach { (label, hrs) ->
                    Button(
                        onClick = {
                            val target = LocalDateTime.now().plusHours(hrs)
                            targetDateStr = target.toLocalDate().toString()
                            targetTimeStr = "%02d:%02d".format(target.hour, target.minute)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PickerField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
