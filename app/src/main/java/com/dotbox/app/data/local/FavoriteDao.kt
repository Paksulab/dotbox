package com.dotbox.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val toolId: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Dao
interface FavoriteDao {
    @Query("SELECT toolId FROM favorites ORDER BY addedAt ASC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE toolId = :toolId")
    suspend fun removeFavorite(toolId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE toolId = :toolId)")
    fun isFavorite(toolId: String): Flow<Boolean>
}
