package com.dotbox.app.ui.screens.tools

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Persistence helpers ──────────────────────────────────────────────

private const val PREFS_NAME = "habits"
private const val KEY_HABIT_NAMES = "habit_names"
private const val KEY_CHECKED_DATES = "checked_dates"

private fun loadHabitNames(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getStringSet(KEY_HABIT_NAMES, emptySet()) ?: emptySet()
    return raw.sorted()
}

private fun saveHabitNames(context: Context, names: List<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_HABIT_NAMES, names.toSet())
        .apply()
}

private fun loadCheckedDates(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KEY_CHECKED_DATES, emptySet()) ?: emptySet()
}

private fun saveCheckedDates(context: Context, dates: Set<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_CHECKED_DATES, dates)
        .apply()
}

/** Key format: "habitName:yyyy-MM-dd" */
private fun dateKey(habitName: String, date: LocalDate): String {
    return "$habitName:${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
}

/** Calculate the current streak (consecutive days ending today or yesterday). */
private fun calculateStreak(habitName: String, checkedDates: Set<String>): Int {
    var streak = 0
    var day = LocalDate.now()
    // Allow streak to start from today or yesterday
    if (dateKey(habitName, day) !in checkedDates) {
        day = day.minusDays(1)
    }
    while (dateKey(habitName, day) in checkedDates) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

// ── Main Screen ──────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var habitNames by remember { mutableStateOf(loadHabitNames(context)) }
    var checkedDates by remember { mutableStateOf(loadCheckedDates(context)) }
    var newHabitName by rememberSaveable { mutableStateOf("") }
    var currentMonth by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var habitToDelete by remember { mutableStateOf<String?>(null) }

    val yearMonth = YearMonth.parse(currentMonth)

    // Delete confirmation dialog
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit") },
            text = { Text("Delete \"$habit\" and all its tracked data?") },
            confirmButton = {
                TextButton(onClick = {
                    // Remove habit name
                    habitNames = habitNames.filter { it != habit }
                    saveHabitNames(context, habitNames)
                    // Remove all checked dates for this habit
                    val prefix = "$habit:"
                    checkedDates = checkedDates.filter { !it.startsWith(prefix) }.toSet()
                    saveCheckedDates(context, checkedDates)
                    habitToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ToolScreenScaffold(title = "Habit Tracker", onBack = onBack) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Add habit input ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("New habit") },
                        placeholder = { Text("e.g. Meditate") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )
                    IconButton(
                        onClick = {
                            val trimmed = newHabitName.trim()
                            if (trimmed.isNotEmpty() && trimmed !in habitNames) {
                                habitNames = (habitNames + trimmed).sorted()
                                saveHabitNames(context, habitNames)
                                newHabitName = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiary),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add habit",
                            tint = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            }

            // ── Month navigation (shared for all habits) ──
            item {
                MonthNavigator(
                    yearMonth = yearMonth,
                    onPrevious = {
                        currentMonth = yearMonth.minusMonths(1).toString()
                    },
                    onNext = {
                        currentMonth = yearMonth.plusMonths(1).toString()
                    },
                )
            }

            // ── Empty state ──
            if (habitNames.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No habits yet.\nAdd one above to start tracking!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // ── Habit cards ──
            items(habitNames, key = { it }) { habitName ->
                val streak = calculateStreak(habitName, checkedDates)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { habitToDelete = habitName },
                        )
                        .padding(16.dp)
                        .animateItem(),
                ) {
                    // Header: habit name + streak + delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = habitName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )

                        // Streak badge
                        if (streak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { habitToDelete = habitName },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete $habitName",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar grid
                    CalendarGrid(
                        yearMonth = yearMonth,
                        habitName = habitName,
                        checkedDates = checkedDates,
                        onToggleDate = { date ->
                            val key = dateKey(habitName, date)
                            checkedDates = if (key in checkedDates) {
                                checkedDates - key
                            } else {
                                checkedDates + key
                            }
                            saveCheckedDates(context, checkedDates)
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Completion count for the month
                    val daysInMonth = yearMonth.lengthOfMonth()
                    val completedThisMonth = (1..daysInMonth).count { day ->
                        dateKey(habitName, yearMonth.atDay(day)) in checkedDates
                    }
                    Text(
                        text = "$completedThisMonth / $daysInMonth days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── Month Navigator ──────────────────────────────────────────────────

@Composable
private fun MonthNavigator(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ── Calendar Grid ────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    habitName: String,
    checkedDates: Set<String>,
    onToggleDate: (LocalDate) -> Unit,
) {
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )
    val today = LocalDate.now()
    val firstOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek
    // Offset: how many blank cells before day 1 (Monday = 0, Sunday = 6)
    val offset = (firstDayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = offset + daysInMonth
    val rows = (totalCells + 6) / 7

    // Day-of-week headers
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        daysOfWeek.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Calendar rows
    for (row in 0 until rows) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (col in 0 until 7) {
                val cellIndex = row * 7 + col
                val dayOfMonth = cellIndex - offset + 1

                if (dayOfMonth in 1..daysInMonth) {
                    val date = yearMonth.atDay(dayOfMonth)
                    val key = dateKey(habitName, date)
                    val isChecked = key in checkedDates
                    val isToday = date == today

                    DayCell(
                        day = dayOfMonth,
                        isChecked = isChecked,
                        isToday = isToday,
                        onClick = { onToggleDate(date) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    // Empty cell
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isChecked: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isChecked -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.surface
        },
        label = "dayCellBg",
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isChecked -> MaterialTheme.colorScheme.onTertiary
            isToday -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "dayCellText",
    )

    val borderShape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(borderShape)
            .background(bgColor)
            .then(
                if (isToday && !isChecked) {
                    Modifier
                        .clip(borderShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = if (isChecked || isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
            ),
            color = textColor,
        )
    }
}
