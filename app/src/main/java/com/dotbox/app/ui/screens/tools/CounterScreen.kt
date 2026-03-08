package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.screens.settings.animationsEnabled
import com.dotbox.app.ui.theme.JetBrainsMono
import org.json.JSONArray
import org.json.JSONObject

private data class CounterItem(
    val id: Long,
    val name: String,
    var count: Int,
    var stepSize: Int = 1,
)

private const val PREFS_NAME = "counter_prefs"
private const val KEY_COUNTERS = "counters_json"

private fun saveCounters(context: Context, counters: List<CounterItem>) {
    val jsonArray = JSONArray()
    counters.forEach { counter ->
        val obj = JSONObject().apply {
            put("id", counter.id)
            put("name", counter.name)
            put("count", counter.count)
            put("stepSize", counter.stepSize)
        }
        jsonArray.put(obj)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_COUNTERS, jsonArray.toString())
        .apply()
}

private fun loadCounters(context: Context): List<CounterItem> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(KEY_COUNTERS, null) ?: return emptyList()
    return try {
        val jsonArray = JSONArray(json)
        (0 until jsonArray.length()).map { i ->
            val obj = jsonArray.getJSONObject(i)
            CounterItem(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                count = obj.getInt("count"),
                stepSize = obj.optInt("stepSize", 1),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val animEnabled = animationsEnabled(context)
    val counters = remember { mutableStateListOf<CounterItem>() }
    var newCounterName by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }

    // Load counters from SharedPreferences on first composition
    LaunchedEffect(Unit) {
        if (!isLoaded) {
            counters.addAll(loadCounters(context))
            isLoaded = true
        }
    }

    // Persist whenever counters change
    fun persist() {
        saveCounters(context, counters.toList())
    }

    ToolScreenScaffold(title = "Counter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Add counter input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newCounterName,
                    onValueChange = { newCounterName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Counter name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        cursorColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                Button(
                    onClick = {
                        val name = newCounterName.trim()
                        if (name.isNotEmpty()) {
                            counters.add(
                                CounterItem(
                                    id = System.currentTimeMillis(),
                                    name = name,
                                    count = 0,
                                    stepSize = 1,
                                )
                            )
                            newCounterName = ""
                            persist()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            if (counters.isEmpty() && isLoaded) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a counter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // Counter list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    items(
                        items = counters,
                        key = { it.id },
                    ) { counter ->
                        CounterCard(
                            counter = counter,
                            animEnabled = animEnabled,
                            onIncrement = {
                                val index = counters.indexOfFirst { it.id == counter.id }
                                if (index >= 0) {
                                    val updated = counters[index].copy(
                                        count = counters[index].count + counters[index].stepSize
                                    )
                                    counters[index] = updated
                                    persist()
                                }
                            },
                            onDecrement = {
                                val index = counters.indexOfFirst { it.id == counter.id }
                                if (index >= 0) {
                                    val updated = counters[index].copy(
                                        count = counters[index].count - counters[index].stepSize
                                    )
                                    counters[index] = updated
                                    persist()
                                }
                            },
                            onReset = {
                                val index = counters.indexOfFirst { it.id == counter.id }
                                if (index >= 0) {
                                    counters[index] = counters[index].copy(count = 0)
                                    persist()
                                }
                            },
                            onDelete = {
                                counters.removeAll { it.id == counter.id }
                                persist()
                            },
                            onStepChange = { newStep ->
                                val index = counters.indexOfFirst { it.id == counter.id }
                                if (index >= 0) {
                                    counters[index] = counters[index].copy(stepSize = newStep)
                                    persist()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CounterCard(
    counter: CounterItem,
    animEnabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onStepChange: (Int) -> Unit,
) {
    val view = LocalView.current
    val stepOptions = listOf(1, 5, 10)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        // Header row: name + action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = counter.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Reset button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onReset()
                    },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(18.dp),
                    )
                }
                // Delete button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onDelete()
                    },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Counter value with +/- buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Decrement button (outlined)
            OutlinedButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onDecrement()
                },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease by ${counter.stepSize}",
                    modifier = Modifier.size(28.dp),
                )
            }

            // Count display
            AnimatedContent(
                targetState = counter.count,
                transitionSpec = {
                    if (animEnabled) {
                        if (targetState > initialState) {
                            (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                        } else {
                            (slideInVertically { it } + fadeIn()) togetherWith
                                (slideOutVertically { -it } + fadeOut())
                        }
                    } else {
                        ContentTransform(
                            targetContentEnter = fadeIn(snap()),
                            initialContentExit = fadeOut(snap()),
                        )
                    }
                },
                label = "counterValue",
                modifier = Modifier.weight(1f),
            ) { count ->
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = JetBrainsMono,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
            }

            // Increment button (filled)
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onIncrement()
                },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase by ${counter.stepSize}",
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step size selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Step:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            stepOptions.forEach { step ->
                FilterChip(
                    selected = counter.stepSize == step,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onStepChange(step)
                    },
                    label = {
                        Text(
                            text = "+$step",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
            }
        }
    }
}
