package com.dotbox.app.ui.screens.tools

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private data class TimeZoneInfo(
    val id: String,
    val displayName: String,
)

private val popularZones = listOf(
    TimeZoneInfo("UTC", "UTC"),
    TimeZoneInfo("America/New_York", "New York (EST/EDT)"),
    TimeZoneInfo("America/Chicago", "Chicago (CST/CDT)"),
    TimeZoneInfo("America/Denver", "Denver (MST/MDT)"),
    TimeZoneInfo("America/Los_Angeles", "Los Angeles (PST/PDT)"),
    TimeZoneInfo("Europe/London", "London (GMT/BST)"),
    TimeZoneInfo("Europe/Paris", "Paris (CET/CEST)"),
    TimeZoneInfo("Europe/Berlin", "Berlin (CET/CEST)"),
    TimeZoneInfo("Europe/Istanbul", "Istanbul (TRT)"),
    TimeZoneInfo("Asia/Dubai", "Dubai (GST)"),
    TimeZoneInfo("Asia/Kolkata", "India (IST)"),
    TimeZoneInfo("Asia/Bangkok", "Bangkok (ICT)"),
    TimeZoneInfo("Asia/Shanghai", "Shanghai (CST)"),
    TimeZoneInfo("Asia/Tokyo", "Tokyo (JST)"),
    TimeZoneInfo("Asia/Seoul", "Seoul (KST)"),
    TimeZoneInfo("Australia/Sydney", "Sydney (AEST/AEDT)"),
    TimeZoneInfo("Pacific/Auckland", "Auckland (NZST/NZDT)"),
    TimeZoneInfo("America/Sao_Paulo", "São Paulo (BRT)"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeZoneConverterScreen(onBack: () -> Unit) {
    var fromZoneId by rememberSaveable { mutableStateOf(popularZones[0].id) }
    var toZoneId by rememberSaveable { mutableStateOf(popularZones[1].id) }
    val fromZone = popularZones.find { it.id == fromZoneId } ?: popularZones[0]
    val toZone = popularZones.find { it.id == toZoneId } ?: popularZones[1]
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }

    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = ZonedDateTime.now()
            delay(1000)
        }
    }

    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEE, MMM d yyyy") }
    val fullFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss z") }

    val fromTime = currentTime.withZoneSameInstant(ZoneId.of(fromZone.id))
    val toTime = currentTime.withZoneSameInstant(ZoneId.of(toZone.id))

    val offsetHours = (toTime.offset.totalSeconds - fromTime.offset.totalSeconds) / 3600.0
    val offsetLabel = if (offsetHours >= 0) "+${offsetHours}h" else "${offsetHours}h"

    ToolScreenScaffold(title = "Time Zones", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // From timezone
            TimeZoneCard(
                label = "FROM",
                zone = fromZone,
                time = fromTime.format(timeFormat),
                date = fromTime.format(dateFormat),
                fullTime = fromTime.format(fullFormat),
                zones = popularZones,
                onZoneSelected = { fromZoneId = it.id },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Swap button & offset
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = {
                        val temp = fromZoneId
                        fromZoneId = toZoneId
                        toZoneId = temp
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = offsetLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // To timezone
            TimeZoneCard(
                label = "TO",
                zone = toZone,
                time = toTime.format(timeFormat),
                date = toTime.format(dateFormat),
                fullTime = toTime.format(fullFormat),
                zones = popularZones,
                onZoneSelected = { toZoneId = it.id },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeZoneCard(
    label: String,
    zone: TimeZoneInfo,
    time: String,
    date: String,
    fullTime: String,
    zones: List<TimeZoneInfo>,
    onZoneSelected: (TimeZoneInfo) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Zone selector
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = zone.displayName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                ),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                zones.forEach { tz ->
                    DropdownMenuItem(
                        text = { Text(tz.displayName) },
                        onClick = {
                            onZoneSelected(tz)
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time display
        Text(
            text = time,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
