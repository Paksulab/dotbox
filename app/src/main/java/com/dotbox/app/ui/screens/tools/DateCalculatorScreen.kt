package com.dotbox.app.ui.screens.tools

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private enum class DateMode(val label: String) {
    AGE("Age"),
    BETWEEN("Days Between"),
    ADD("Add/Subtract"),
}

@Composable
fun DateCalculatorScreen(onBack: () -> Unit) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = DateMode.entries[modeIndex]
    val context = LocalContext.current

    var date1Str by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var date2Str by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var daysToAdd by rememberSaveable { mutableStateOf("0") }

    val date1 = LocalDate.parse(date1Str)
    val date2 = LocalDate.parse(date2Str)
    val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy")

    fun pickDate(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth,
        ).show()
    }

    ToolScreenScaffold(title = "Date Calculator", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DateMode.entries.forEachIndexed { idx, m ->
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

            // Date pickers
            when (mode) {
                DateMode.AGE -> {
                    DatePickerField("Birth Date", date1.format(fmt)) {
                        pickDate(date1) { date1Str = it.toString() }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val period = Period.between(date1, LocalDate.now())
                    val totalDays = ChronoUnit.DAYS.between(date1, LocalDate.now())

                    ResultCard {
                        Text(
                            text = "Age",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${period.years} years, ${period.months} months, ${period.days} days",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Total days", "%,d".format(totalDays))
                        InfoRow("Total weeks", "%,d".format(totalDays / 7))
                        InfoRow("Total months", "%,d".format(period.toTotalMonths()))
                        InfoRow("Next birthday", run {
                            val nextBd = date1.withYear(LocalDate.now().year).let {
                                if (it.isBefore(LocalDate.now()) || it == LocalDate.now()) it.plusYears(1) else it
                            }
                            "${ChronoUnit.DAYS.between(LocalDate.now(), nextBd)} days away"
                        })
                    }
                }

                DateMode.BETWEEN -> {
                    DatePickerField("Start Date", date1.format(fmt)) {
                        pickDate(date1) { date1Str = it.toString() }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DatePickerField("End Date", date2.format(fmt)) {
                        pickDate(date2) { date2Str = it.toString() }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val daysBetween = ChronoUnit.DAYS.between(date1, date2)
                    val period = Period.between(date1, date2)

                    ResultCard {
                        InfoRow("Days", "%,d".format(daysBetween))
                        InfoRow("Weeks", "%,.1f".format(daysBetween / 7.0))
                        InfoRow("Months", "${period.toTotalMonths()}")
                        InfoRow("Years", "%,.2f".format(daysBetween / 365.25))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline,
                        )
                        InfoRow("Business days (≈)", "%,d".format((daysBetween * 5 / 7)))
                        InfoRow("Weekends (≈)", "%,d".format((daysBetween * 2 / 7)))
                    }
                }

                DateMode.ADD -> {
                    DatePickerField("Start Date", date1.format(fmt)) {
                        pickDate(date1) { date1Str = it.toString() }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = daysToAdd,
                        onValueChange = { daysToAdd = it.filter { c -> c.isDigit() || c == '-' } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Days to add (use − for subtract)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            cursorColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val days = daysToAdd.toLongOrNull() ?: 0L
                    val resultDate = date1.plusDays(days)

                    ResultCard {
                        Text(
                            text = "Result",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resultDate.format(fmt),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resultDate.dayOfWeek.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DatePickerField(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
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
private fun ResultCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
