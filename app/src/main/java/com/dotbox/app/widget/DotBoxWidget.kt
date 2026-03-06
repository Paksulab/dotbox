package com.dotbox.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dotbox.app.MainActivity
import com.dotbox.app.data.model.ToolId

class DotBoxWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("dotbox_settings", Context.MODE_PRIVATE)
        val favoritesJson = prefs.getString("widget_favorites", "") ?: ""
        val favoriteNames = if (favoritesJson.isNotBlank()) {
            favoritesJson.split(",").take(4)
        } else {
            emptyList()
        }

        val favoriteTools = favoriteNames.mapNotNull { name ->
            ToolId.entries.find { it.name == name }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(favoriteTools)
            }
        }
    }
}

@Composable
private fun WidgetContent(favorites: List<ToolId>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(day = android.graphics.Color.parseColor("#1A1A1A"), night = android.graphics.Color.parseColor("#1A1A1A"))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "dot",
                style = TextStyle(
                    color = ColorProvider(
                        day = android.graphics.Color.parseColor("#808080"),
                        night = android.graphics.Color.parseColor("#808080"),
                    ),
                    fontSize = 14.sp,
                ),
            )
            Text(
                text = "Box",
                style = TextStyle(
                    color = ColorProvider(
                        day = android.graphics.Color.WHITE,
                        night = android.graphics.Color.WHITE,
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (favorites.isEmpty()) {
            // No favourites yet
            Text(
                text = "Add favourites to see them here",
                style = TextStyle(
                    color = ColorProvider(
                        day = android.graphics.Color.parseColor("#808080"),
                        night = android.graphics.Color.parseColor("#808080"),
                    ),
                    fontSize = 12.sp,
                ),
            )
        } else {
            // Show up to 4 favourites in a 2x2 grid
            val rows = favorites.chunked(2)
            rows.forEach { rowTools ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    rowTools.forEach { tool ->
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .cornerRadius(12.dp)
                                .background(
                                    day = android.graphics.Color.parseColor("#2A2A2A"),
                                    night = android.graphics.Color.parseColor("#2A2A2A"),
                                )
                                .padding(8.dp)
                                .clickable(actionStartActivity<MainActivity>()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = tool.toolName,
                                style = TextStyle(
                                    color = ColorProvider(
                                        day = android.graphics.Color.WHITE,
                                        night = android.graphics.Color.WHITE,
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                maxLines = 1,
                            )
                        }
                        // Spacer between columns
                        if (rowTools.size > 1 && tool != rowTools.last()) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                        }
                    }
                    // Fill remaining space if only 1 tool in row
                    if (rowTools.size == 1) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
                // Spacer between rows
                if (rows.size > 1 && rowTools != rows.last()) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        }
    }
}
