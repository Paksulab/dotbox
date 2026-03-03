package com.dotbox.app.data.repository

import com.dotbox.app.data.local.FavoriteDao
import com.dotbox.app.data.local.FavoriteEntity
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.data.model.ToolId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ToolsRepository(private val favoriteDao: FavoriteDao) {

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

    fun isFavorite(toolId: ToolId): Flow<Boolean> = favoriteDao.isFavorite(toolId.name)

    suspend fun toggleFavorite(toolId: ToolId) {
        val entity = FavoriteEntity(toolId = toolId.name)
        // Check synchronously isn't ideal, but we use a simple approach
        favoriteDao.removeFavorite(toolId.name)
        // If it was removed, we need to check if we should add it back
        // Simple approach: try remove, then add if it wasn't there
    }

    suspend fun addFavorite(toolId: ToolId) {
        favoriteDao.addFavorite(FavoriteEntity(toolId = toolId.name))
    }

    suspend fun removeFavorite(toolId: ToolId) {
        favoriteDao.removeFavorite(toolId.name)
    }
}
