package com.dotbox.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.ui.theme.NothingRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolCard(
    tool: ToolId,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val favoriteColor by animateColorAsState(
        targetValue = if (isFavorite) NothingRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "favoriteColor",
    )

    // Press micro-interaction: scale down on tap, spring back
    val pressScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    // Website-matching gradient: 160° from cardGray(0.95) to darkGray(0.95)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
                // Website-matching shadow: 0 12px 34px rgba(0,0,0,0.35)
                shadowElevation = 12f
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = {
                    scope.launch {
                        pressScale.animateTo(0.94f, tween(60))
                        pressScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f))
                    }
                    onClick()
                },
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.01f),
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isEditMode) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
            } else {
                tool.category.accentColor.copy(alpha = 0.2f)
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                // Category tint layer on top of gradient
                .background(tool.category.accentColor.copy(alpha = 0.06f)),
        ) {
            if (isEditMode) {
                // Edit mode: remove button (top-right)
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .padding(4.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NothingRed.copy(alpha = 0.15f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove from favorites",
                        tint = NothingRed,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                // Normal mode: favourite heart button (top-right)
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(44.dp)
                        .padding(6.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = favoriteColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Icon + Name — centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.toolName,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tool.toolName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
