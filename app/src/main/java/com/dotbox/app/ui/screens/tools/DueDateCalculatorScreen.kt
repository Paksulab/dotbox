package com.dotbox.app.ui.screens.tools

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private enum class InputMode(val label: String) {
    LMP("Last Period (LMP)"),
    CONCEPTION("Conception Date"),
}

@Composable
fun DueDateCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = InputMode.entries[modeIndex]

    var selectedDateStr by rememberSaveable {
        mutableStateOf(LocalDate.now().minusWeeks(8).toString())
    }
    val selectedDate = LocalDate.parse(selectedDateStr)

    val dueDateFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val pickerFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")

    // Calculate due date using Naegele's rule
    val dueDate = when (mode) {
        InputMode.LMP -> selectedDate.plusDays(280)
        InputMode.CONCEPTION -> selectedDate.plusDays(266)
    }

    // Derive LMP regardless of mode (for gestational age calculations)
    val lmpDate = when (mode) {
        InputMode.LMP -> selectedDate
        InputMode.CONCEPTION -> selectedDate.minusDays(14) // conception ~ LMP + 14 days
    }

    val today = LocalDate.now()
    val totalGestationDays = 280L
    val daysElapsed = ChronoUnit.DAYS.between(lmpDate, today)
    val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)
    val gestationalWeeks = (daysElapsed / 7).toInt()
    val gestationalDaysRemainder = (daysElapsed % 7).toInt()

    val trimester = when {
        gestationalWeeks < 13 -> 1
        gestationalWeeks < 27 -> 2
        else -> 3
    }

    val percentComplete = ((daysElapsed.toFloat() / totalGestationDays) * 100f)
        .coerceIn(0f, 100f)

    val isValidPregnancy = daysElapsed in 0..totalGestationDays

    fun pickDate() {
        DatePickerDialog(
            context,
            { _, y, m, d -> selectedDateStr = LocalDate.of(y, m + 1, d).toString() },
            selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth,
        ).show()
    }

    ToolScreenScaffold(title = "Due Date", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode selector
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InputMode.entries.forEachIndexed { idx, m ->
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

            Spacer(modifier = Modifier.height(20.dp))

            // Date picker field
            DueDatePickerField(
                label = if (mode == InputMode.LMP) "First Day of Last Period" else "Conception Date",
                value = selectedDate.format(pickerFmt),
                onClick = { pickDate() },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Due Date result
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ESTIMATED DUE DATE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dueDate.format(dueDateFmt),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
            }

            if (isValidPregnancy) {
                Spacer(modifier = Modifier.height(12.dp))

                // Gestational age & progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    Text(
                        text = "CURRENT PROGRESS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DueDateInfoRow(
                        "Gestational age",
                        "$gestationalWeeks weeks, $gestationalDaysRemainder days",
                    )
                    DueDateInfoRow("Trimester", "${trimester}${trimesterSuffix(trimester)} trimester")
                    DueDateInfoRow("Days remaining", "$daysRemaining")
                    DueDateInfoRow("Complete", "%.1f%%".format(percentComplete))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { (percentComplete / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "0w",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "40w",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Trimester timeline
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    Text(
                        text = "TRIMESTER TIMELINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        TrimesterSegment(
                            label = "1st",
                            subtitle = "Wk 1-12",
                            isActive = trimester == 1,
                            modifier = Modifier.weight(1f),
                        )
                        TrimesterSegment(
                            label = "2nd",
                            subtitle = "Wk 13-26",
                            isActive = trimester == 2,
                            modifier = Modifier.weight(1f),
                        )
                        TrimesterSegment(
                            label = "3rd",
                            subtitle = "Wk 27-40",
                            isActive = trimester == 3,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Key milestones
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp),
                ) {
                    Text(
                        text = "KEY MILESTONES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val milestones = listOf(
                        Triple("First heartbeat", "~6 weeks", 6),
                        Triple("First movement", "~16-20 weeks", 16),
                        Triple("Viability", "~24 weeks", 24),
                        Triple("Full term", "~37 weeks", 37),
                        Triple("Due date", "~40 weeks", 40),
                    )

                    milestones.forEachIndexed { index, (name, timing, week) ->
                        val isPast = gestationalWeeks >= week
                        val isCurrent = gestationalWeeks in (week - 2)..(week + 1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Status dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPast -> MaterialTheme.colorScheme.tertiary
                                            isCurrent -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        },
                                    ),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (isPast || isCurrent) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                            Text(
                                text = timing,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                color = if (isPast) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            if (isPast) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "\u2713",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }

                        if (index < milestones.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 22.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DueDatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TrimesterSegment(
    label: String,
    subtitle: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = if (isActive) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
            color = if (isActive) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
        )
    }
}

@Composable
private fun DueDateInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun trimesterSuffix(trimester: Int): String = when (trimester) {
    1 -> "st"
    2 -> "nd"
    3 -> "rd"
    else -> "th"
}
