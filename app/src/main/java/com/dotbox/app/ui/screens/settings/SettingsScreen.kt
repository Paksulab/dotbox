package com.dotbox.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono

private const val PREFS_NAME = "dotbox_settings"
private const val KEY_THEME = "theme_mode"
private const val KEY_GRID_COLUMNS = "grid_columns"
private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
private const val KEY_DEFAULT_CATEGORY = "default_category"

// ── Persistence helpers ─────────────────────────────────────────────────────

private fun prefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

private fun loadString(context: Context, key: String, default: String): String =
    prefs(context).getString(key, default) ?: default

private fun saveString(context: Context, key: String, value: String) =
    prefs(context).edit().putString(key, value).apply()

private fun loadBoolean(context: Context, key: String, default: Boolean): Boolean =
    prefs(context).getBoolean(key, default)

private fun saveBoolean(context: Context, key: String, value: Boolean) =
    prefs(context).edit().putBoolean(key, value).apply()

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var themeMode by remember { mutableStateOf(loadString(context, KEY_THEME, "dark")) }
    var gridColumns by remember { mutableStateOf(loadString(context, KEY_GRID_COLUMNS, "auto")) }
    var hapticFeedback by remember { mutableStateOf(loadBoolean(context, KEY_HAPTIC_FEEDBACK, true)) }
    var defaultCategory by remember { mutableStateOf(loadString(context, KEY_DEFAULT_CATEGORY, "all")) }

    ToolScreenScaffold(
        title = "Settings",
        onBack = onBack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── APPEARANCE ──────────────────────────────────────
            item { SectionHeader("APPEARANCE") }

            item {
                SettingsCard {
                    // Theme
                    SettingLabel(
                        title = "Theme",
                        description = "App colour scheme"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChipGroup(
                        options = listOf("dark" to "Dark", "system" to "System", "light" to "Light"),
                        selected = themeMode,
                        onSelect = {
                            themeMode = it
                            saveString(context, KEY_THEME, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    // Grid columns
                    SettingLabel(
                        title = "Grid Columns",
                        description = "Tile layout on the home screen"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChipGroup(
                        options = listOf("auto" to "Auto", "2" to "2", "3" to "3", "4" to "4"),
                        selected = gridColumns,
                        onSelect = {
                            gridColumns = it
                            saveString(context, KEY_GRID_COLUMNS, it)
                        }
                    )
                }
            }

            // ── BEHAVIOUR ───────────────────────────────────────
            item { SectionHeader("BEHAVIOUR") }

            item {
                SettingsCard {
                    // Haptic feedback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SettingLabel(
                                title = "Haptic Feedback",
                                description = "Vibration in tools like Counter and Pomodoro"
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = {
                                hapticFeedback = it
                                saveBoolean(context, KEY_HAPTIC_FEEDBACK, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                checkedThumbColor = MaterialTheme.colorScheme.onTertiary
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    // Default category
                    SettingLabel(
                        title = "Default Category",
                        description = "Category shown when opening the app"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryDropdown(
                        selectedKey = defaultCategory,
                        onSelect = {
                            defaultCategory = it
                            saveString(context, KEY_DEFAULT_CATEGORY, it)
                        }
                    )
                }
            }

            // ── ABOUT ───────────────────────────────────────────
            item { SectionHeader("ABOUT") }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "dotBox",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = JetBrainsMono,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.2.0",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = JetBrainsMono,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All-in-one utility toolkit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "48 tools · 7 categories",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = JetBrainsMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── Components ──────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontFamily = JetBrainsMono,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingLabel(title: String, description: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ChipGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            FilterChip(
                selected = selected == key,
                onClick = { onSelect(key) },
                label = {
                    Text(
                        text = label,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val allOptions = buildList {
        add("all" to "All")
        ToolCategory.entries.forEach { add(it.name to it.displayName) }
    }

    val displayValue = allOptions.firstOrNull { it.first == selectedKey }?.second ?: "All"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMono),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                cursorColor = MaterialTheme.colorScheme.tertiary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(text = label, fontFamily = JetBrainsMono) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
