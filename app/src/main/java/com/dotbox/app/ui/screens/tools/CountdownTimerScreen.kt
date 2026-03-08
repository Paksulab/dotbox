package com.dotbox.app.ui.screens.tools

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotbox.app.data.preferences.AppPreferences
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private data class SavedCountdown(
    val id: String,
    val name: String,
    val targetDate: String,
    val targetTime: String,
)

private fun loadCountdowns(context: android.content.Context): List<SavedCountdown> {
    val json = AppPreferences.get(context)
        .getString(AppPreferences.KEY_SAVED_COUNTDOWNS, "[]") ?: "[]"
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            SavedCountdown(
                id = obj.getString("id"),
                name = obj.getString("name"),
                targetDate = obj.getString("targetDate"),
                targetTime = obj.getString("targetTime"),
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveCountdowns(context: android.content.Context, list: List<SavedCountdown>) {
    val arr = JSONArray()
    list.forEach { c ->
        arr.put(
            JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("targetDate", c.targetDate)
                put("targetTime", c.targetTime)
            },
        )
    }
    AppPreferences.get(context).edit()
        .putString(AppPreferences.KEY_SAVED_COUNTDOWNS, arr.toString())
        .apply()
}

@Composable
fun CountdownTimerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var eventName by rememberSaveable { mutableStateOf("") }
    var targetDateStr by rememberSaveable {
        mutableStateOf(LocalDate.now().plusDays(7).toString())
    }
    var targetTimeStr by rememberSaveable { mutableStateOf("00:00") }
    var nowMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    // Saved countdowns
    var savedCountdowns by remember { mutableStateOf(loadCountdowns(context)) }

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

    fun saveCurrentCountdown() {
        val name = eventName.ifBlank { "Countdown" }
        val new = SavedCountdown(
            id = UUID.randomUUID().toString(),
            name = name,
            targetDate = targetDateStr,
            targetTime = targetTimeStr,
        )
        savedCountdowns = savedCountdowns + new
        saveCountdowns(context, savedCountdowns)
    }

    fun deleteCountdown(id: String) {
        savedCountdowns = savedCountdowns.filter { it.id != id }
        saveCountdowns(context, savedCountdowns)
    }

    fun loadCountdown(countdown: SavedCountdown) {
        eventName = countdown.name
        targetDateStr = countdown.targetDate
        targetTimeStr = countdown.targetTime
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

            // Event name + save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Event name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                IconButton(
                    onClick = { saveCurrentCountdown() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Save event",
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

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
                Text(
                    ":",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CountdownUnit("%02d".format(hours), "hrs")
                Text(
                    ":",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CountdownUnit("%02d".format(minutes), "min")
                Text(
                    ":",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                listOf(
                    "1 hour" to 1L,
                    "24 hours" to 24L,
                    "7 days" to 168L,
                    "30 days" to 720L,
                ).forEach { (label, hrs) ->
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

            // Saved countdowns section
            AnimatedVisibility(
                visible = savedCountdowns.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "SAVED EVENTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    savedCountdowns.forEach { countdown ->
                        SavedCountdownCard(
                            countdown = countdown,
                            nowMillis = nowMillis,
                            onClick = { loadCountdown(countdown) },
                            onDelete = { deleteCountdown(countdown.id) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SavedCountdownCard(
    countdown: SavedCountdown,
    nowMillis: Long,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val targetDate = LocalDate.parse(countdown.targetDate)
    val targetTime = LocalTime.parse(countdown.targetTime)
    val targetMillis = LocalDateTime.of(targetDate, targetTime)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val diff = targetMillis - nowMillis
    val isPast = diff <= 0
    val absDiff = kotlin.math.abs(diff)
    val totalSec = absDiff / 1000
    val d = totalSec / 86400
    val h = (totalSec % 86400) / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60

    val countdownText = buildString {
        if (d > 0) append("${d}d ")
        append("%02d:%02d:%02d".format(h, m, s))
        if (isPast) append(" ago")
    }

    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm")
    val formattedTarget = LocalDateTime.of(targetDate, targetTime).format(dateFmt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = countdown.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedTarget,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = countdownText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            color = if (isPast) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.tertiary,
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Delete",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
private fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
