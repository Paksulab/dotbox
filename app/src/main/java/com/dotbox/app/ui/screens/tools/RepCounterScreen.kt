package com.dotbox.app.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.launch

private data class SetRecord(val setNumber: Int, val reps: Int)

@Composable
fun RepCounterScreen(onBack: () -> Unit) {
    var currentReps by rememberSaveable { mutableIntStateOf(0) }
    val sets = rememberSaveable(
        saver = listSaver<SnapshotStateList<SetRecord>, String>(
            save = { list -> list.map { "${it.setNumber},${it.reps}" } },
            restore = { saved ->
                saved.map { s ->
                    val (num, reps) = s.split(",")
                    SetRecord(num.toInt(), reps.toInt())
                }.toMutableStateList()
            },
        ),
    ) { mutableStateListOf() }
    val bounceScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    ToolScreenScaffold(title = "Rep Counter", onBack = onBack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Current set info
            Text(
                text = "SET ${sets.size + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Big rep counter
            Text(
                text = "$currentReps",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.graphicsLayer {
                    scaleX = bounceScale.value
                    scaleY = bounceScale.value
                },
            )

            Text(
                text = "reps",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // +/- buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Minus
                FilledTonalIconButton(
                    onClick = { if (currentReps > 0) currentReps-- },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Minus", modifier = Modifier.size(32.dp))
                }

                // Big plus
                FilledIconButton(
                    onClick = {
                        currentReps++
                        scope.launch {
                            bounceScale.animateTo(1.15f, spring(stiffness = 600f))
                            bounceScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f))
                        }
                    },
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = NothingRed,
                    ),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add rep", modifier = Modifier.size(48.dp))
                }

                // Reset current
                FilledTonalIconButton(
                    onClick = { currentReps = 0 },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Log set button
            TextButton(
                onClick = {
                    if (currentReps > 0) {
                        sets.add(SetRecord(sets.size + 1, currentReps))
                        currentReps = 0
                    }
                },
                enabled = currentReps > 0,
            ) {
                Text(
                    text = "LOG SET →",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (currentReps > 0) NothingRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Set history
            if (sets.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SETS LOG",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val totalReps = sets.sumOf { it.reps }
                        Text(
                            text = "Total: $totalReps reps",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    sets.forEachIndexed { index, set ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Set ${set.setNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${set.reps} reps",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { sets.clear() },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Clear all",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
