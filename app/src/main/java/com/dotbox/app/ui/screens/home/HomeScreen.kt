package com.dotbox.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.data.repository.ToolsRepository
import com.dotbox.app.ui.components.DotPattern
import com.dotbox.app.ui.components.ToolCard
import com.dotbox.app.ui.theme.JetBrainsMono

private const val PREFS_NAME = "dotbox_settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ToolsRepository,
    onToolClick: (ToolId) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = remember { ToolCategory.entries }
    val context = LocalContext.current

    // Wiggle animation for edit mode
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle")
    val wiggleAngle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggleRotation",
    )

    // Read grid columns preference
    val gridCells = remember {
        val pref = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString("grid_columns", "auto") ?: "auto"
        when (pref) {
            "2" -> GridCells.Fixed(2)
            "3" -> GridCells.Fixed(3)
            "4" -> GridCells.Fixed(4)
            else -> GridCells.Adaptive(minSize = 160.dp)
        }
    }

    // Apply default category on first launch
    LaunchedEffect(Unit) {
        val pref = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString("default_category", "all") ?: "all"
        if (pref != "all") {
            val category = ToolCategory.entries.find { it.name == pref }
            if (category != null) {
                viewModel.onCategorySelected(category)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearchActive) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    "Search tools...",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.tertiary,
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        )
                    } else if (uiState.isEditMode) {
                        Text(
                            text = "Edit Favourites",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "dot",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Normal,
                                ),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            )
                            Text(
                                text = "Box",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        Button(
                            onClick = { viewModel.exitEditMode() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                        ) {
                            Text("Done")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.onSearchActiveChanged(!uiState.isSearchActive)
                            },
                        ) {
                            Icon(
                                imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (uiState.isSearchActive) "Close search" else "Search",
                            )
                        }
                        if (!uiState.isSearchActive) {
                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            DotPattern()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ── Pinned category chips ────────────────────────────
                AnimatedVisibility(
                    visible = !uiState.isSearchActive && !uiState.isEditMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = uiState.selectedCategory == category,
                                onClick = { viewModel.onCategorySelected(category) },
                                label = {
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = category.accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = category.accentColor,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outline,
                                    selectedBorderColor = category.accentColor.copy(alpha = 0.5f),
                                    enabled = true,
                                    selected = uiState.selectedCategory == category,
                                ),
                            )
                        }
                    }
                }

                // ── Scrollable tool grid ─────────────────────────────
                LazyVerticalGrid(
                    columns = gridCells,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Favorites section
                    val favoriteTools = uiState.favoriteToolsOrdered
                    if (favoriteTools.isNotEmpty() && !uiState.isSearchActive && uiState.selectedCategory == null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "FAVORITES",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                        itemsIndexed(favoriteTools, key = { _, tool -> "fav_${tool.name}" }) { index, tool ->
                            ToolCard(
                                tool = tool,
                                isFavorite = true,
                                onClick = {
                                    if (!uiState.isEditMode) onToolClick(tool)
                                },
                                onFavoriteToggle = { viewModel.toggleFavorite(tool) },
                                modifier = Modifier.graphicsLayer {
                                    if (uiState.isEditMode) {
                                        rotationZ = wiggleAngle * if (index % 2 == 0) 1f else -1f
                                    }
                                },
                                isEditMode = uiState.isEditMode,
                                onLongClick = {
                                    if (!uiState.isEditMode) viewModel.toggleEditMode()
                                },
                                onMoveUp = if (uiState.isEditMode && index > 0) {
                                    { viewModel.reorderFavorites(index, index - 1) }
                                } else null,
                                onMoveDown = if (uiState.isEditMode && index < favoriteTools.size - 1) {
                                    { viewModel.reorderFavorites(index, index + 1) }
                                } else null,
                            )
                        }
                    }

                    // Section header
                    if (!uiState.isEditMode) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    uiState.isSearchActive && uiState.searchQuery.isNotBlank() ->
                                        "RESULTS (${uiState.tools.size})"
                                    uiState.selectedCategory != null ->
                                        uiState.selectedCategory!!.displayName.uppercase()
                                    else -> "ALL TOOLS"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Tool tiles
                        items(uiState.tools, key = { it.name }) { tool ->
                            ToolCard(
                                tool = tool,
                                isFavorite = tool.name in uiState.favoriteIds,
                                onClick = { onToolClick(tool) },
                                onFavoriteToggle = { viewModel.toggleFavorite(tool) },
                            )
                        }
                    }

                    // Bottom spacing
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
