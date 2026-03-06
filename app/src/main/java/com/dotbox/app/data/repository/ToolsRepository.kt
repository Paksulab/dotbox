package com.dotbox.app.data.repository

import android.content.Context
import com.dotbox.app.data.local.FavoriteDao
import com.dotbox.app.data.local.FavoriteEntity
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.data.model.ToolId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ToolsRepository(
    private val favoriteDao: FavoriteDao,
    private val context: Context? = null,
) {

    val allTools: List<ToolId> = ToolId.entries

    val categories: List<ToolCategory> = ToolCategory.entries

    fun getToolsByCategory(category: ToolCategory): List<ToolId> =
        allTools.filter { it.category == category }

    fun searchTools(query: String): List<ToolId> {
        if (query.isBlank()) return allTools
        val lower = query.lowercase()
        return allTools.filter {
            it.toolName.lowercase().contains(lower) ||
                it.description.lowercase().contains(lower) ||
                it.category.displayName.lowercase().contains(lower)
        }
    }

    val favoriteIds: Flow<List<String>> = favoriteDao.getAllFavoriteIds()

    val favoriteTools: Flow<List<ToolId>> = favoriteDao.getAllFavoriteIds().map { ids ->
        ids.mapNotNull { id -> ToolId.entries.find { it.name == id } }
    }

    val favoriteToolsOrdered: Flow<List<ToolId>> = favoriteDao.getAllFavorites().map { entities ->
        entities.mapNotNull { entity -> ToolId.entries.find { it.name == entity.toolId } }
    }

    fun isFavorite(toolId: ToolId): Flow<Boolean> = favoriteDao.isFavorite(toolId.name)

    suspend fun addFavorite(toolId: ToolId) {
        val count = favoriteDao.getFavoriteCount()
        favoriteDao.addFavorite(FavoriteEntity(toolId = toolId.name, orderIndex = count))
        syncWidgetFavorites()
    }

    suspend fun removeFavorite(toolId: ToolId) {
        favoriteDao.removeFavorite(toolId.name)
        syncWidgetFavorites()
    }

    suspend fun reorderFavorites(fromIndex: Int, toIndex: Int) {
        val currentFavorites = favoriteDao.getAllFavoritesSnapshot().toMutableList()
        if (fromIndex < 0 || fromIndex >= currentFavorites.size) return
        if (toIndex < 0 || toIndex >= currentFavorites.size) return

        val item = currentFavorites.removeAt(fromIndex)
        currentFavorites.add(toIndex, item)

        currentFavorites.forEachIndexed { index, entity ->
            favoriteDao.updateOrder(entity.toolId, index)
        }
        syncWidgetFavorites()
    }

    /** Sync top 4 favourites to SharedPreferences for the widget */
    private suspend fun syncWidgetFavorites() {
        val ctx = context ?: return
        val topFavs = favoriteDao.getAllFavoritesSnapshot()
            .take(4)
            .joinToString(",") { it.toolId }
        ctx.getSharedPreferences("dotbox_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("widget_favorites", topFavs)
            .apply()
    }
}
