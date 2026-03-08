package com.dotbox.app.ui.screens.home

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.data.preferences.AppPreferences
import com.dotbox.app.data.repository.ToolsRepository
import com.dotbox.app.ui.components.DotPattern
import com.dotbox.app.ui.components.ToolCard
import com.dotbox.app.ui.theme.JetBrainsMono

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
    val view = LocalView.current

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

    // Grid state for drag-to-reorder
    val gridState = rememberLazyGridState()

    // Drag state from ViewModel
    val dragList by viewModel.dragList.collectAsStateWithLifecycle()
    val draggedIndex by viewModel.draggedIndex.collectAsStateWithLifecycle()
    val isDragging = draggedIndex != null

    // Visual drag offset (pixels from resting position)
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // The list to display: drag override or DB-backed
    val displayFavorites = dragList ?: uiState.favoriteToolsOrdered

    // Read grid columns preference
    val gridCells = remember {
        val pref = AppPreferences.get(context)
            .getString(AppPreferences.KEY_GRID_COLUMNS, "auto") ?: "auto"
        when (pref) {
            "2" -> GridCells.Fixed(2)
            "3" -> GridCells.Fixed(3)
            "4" -> GridCells.Fixed(4)
            else -> GridCells.Adaptive(minSize = 160.dp)
        }
    }

    // Apply default category on first launch
    LaunchedEffect(Unit) {
        val pref = AppPreferences.get(context)
            .getString(AppPreferences.KEY_DEFAULT_CATEGORY, "all") ?: "all"
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
                            // Website: border-radius 14px
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                            // Website: border 1px solid #ff7676 (lighter accent border)
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                            ),
                        ) {
                            Text("Done", fontWeight = FontWeight.Medium)
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
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                // Website: border-radius 999px (fully rounded pill)
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    // Website: unselected bg rgba(245,245,245,0.03)
                                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    // Website: selected bg uses category color at ~15%
                                    selectedContainerColor = category.accentColor.copy(alpha = 0.12f),
                                    selectedLabelColor = category.accentColor,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    // Website: border 1px solid rgba(224,224,224,0.2)
                                    borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                    selectedBorderColor = category.accentColor.copy(alpha = 0.35f),
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
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = !isDragging,
                ) {
                    // Favorites section
                    if (displayFavorites.isNotEmpty() && !uiState.isSearchActive && uiState.selectedCategory == null) {
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
                        itemsIndexed(displayFavorites, key = { _, tool -> "fav_${tool.name}" }) { index, tool ->
                            val isBeingDragged = draggedIndex == index

                            ToolCard(
                                tool = tool,
                                isFavorite = true,
                                onClick = {
                                    if (!uiState.isEditMode) onToolClick(tool)
                                },
                                onFavoriteToggle = { viewModel.toggleFavorite(tool) },
                                modifier = Modifier
                                    .zIndex(if (isBeingDragged) 1f else 0f)
                                    .then(
                                        if (uiState.isEditMode) {
                                            Modifier.pointerInput(Unit) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        viewModel.startDrag(index)
                                                        dragOffset = Offset.Zero
                                                        view.performHapticFeedback(
                                                            HapticFeedbackConstants.LONG_PRESS,
                                                        )
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffset += Offset(dragAmount.x, dragAmount.y)

                                                        // Find which grid cell the dragged item's center is over
                                                        val currentDragIdx = viewModel.draggedIndex.value
                                                            ?: return@detectDragGesturesAfterLongPress

                                                        val draggedItemInfo = gridState.layoutInfo.visibleItemsInfo
                                                            .find { it.key == "fav_${tool.name}" }
                                                            ?: return@detectDragGesturesAfterLongPress

                                                        val centerX = draggedItemInfo.offset.x +
                                                            draggedItemInfo.size.width / 2 + dragOffset.x
                                                        val centerY = draggedItemInfo.offset.y +
                                                            draggedItemInfo.size.height / 2 + dragOffset.y

                                                        // Find target item under the center point
                                                        val targetItem = gridState.layoutInfo.visibleItemsInfo
                                                            .firstOrNull { itemInfo ->
                                                                val key = itemInfo.key as? String
                                                                    ?: return@firstOrNull false
                                                                if (!key.startsWith("fav_")) return@firstOrNull false
                                                                if (key == "fav_${tool.name}") return@firstOrNull false
                                                                centerX >= itemInfo.offset.x &&
                                                                    centerX <= itemInfo.offset.x + itemInfo.size.width &&
                                                                    centerY >= itemInfo.offset.y &&
                                                                    centerY <= itemInfo.offset.y + itemInfo.size.height
                                                            }

                                                        if (targetItem != null) {
                                                            // Calculate the favorite index from grid index
                                                            // The "FAVORITES" header is at grid index 0,
                                                            // so favorite items start at grid index 1
                                                            val headerOffset = 1
                                                            val targetFavIndex = targetItem.index - headerOffset
                                                            val currentList = viewModel.dragList.value
                                                                ?: return@detectDragGesturesAfterLongPress
                                                            if (targetFavIndex in currentList.indices &&
                                                                targetFavIndex != currentDragIdx
                                                            ) {
                                                                viewModel.moveDragItem(currentDragIdx, targetFavIndex)
                                                                // Reset offset: item slot moved to new position
                                                                dragOffset = Offset.Zero
                                                                view.performHapticFeedback(
                                                                    HapticFeedbackConstants.CLOCK_TICK,
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        dragOffset = Offset.Zero
                                                        viewModel.endDrag()
                                                    },
                                                    onDragCancel = {
                                                        dragOffset = Offset.Zero
                                                        viewModel.cancelDrag()
                                                    },
                                                )
                                            }
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .graphicsLayer {
                                        if (uiState.isEditMode) {
                                            if (isBeingDragged) {
                                                // Dragged item: follow finger, scale up, elevate
                                                translationX = dragOffset.x
                                                translationY = dragOffset.y
                                                scaleX = 1.08f
                                                scaleY = 1.08f
                                                shadowElevation = 12f
                                                rotationZ = 0f // stop wiggle
                                            } else {
                                                // Non-dragged: wiggle
                                                rotationZ = wiggleAngle * if (index % 2 == 0) 1f else -1f
                                            }
                                        }
                                    }
                                    .animateItem(),
                                isEditMode = uiState.isEditMode,
                                onLongClick = if (!uiState.isEditMode) {
                                    { viewModel.toggleEditMode() }
                                } else {
                                    null
                                },
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
