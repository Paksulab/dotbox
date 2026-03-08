package com.dotbox.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val toolId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = Int.MAX_VALUE,
)

@Dao
interface FavoriteDao {
    @Query("SELECT toolId FROM favorites ORDER BY orderIndex ASC, addedAt ASC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT * FROM favorites ORDER BY orderIndex ASC, addedAt ASC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY orderIndex ASC, addedAt ASC")
    suspend fun getAllFavoritesSnapshot(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE toolId = :toolId")
    suspend fun removeFavorite(toolId: String)

    @Query("UPDATE favorites SET orderIndex = :orderIndex WHERE toolId = :toolId")
    suspend fun updateOrder(toolId: String, orderIndex: Int)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE toolId = :toolId)")
    fun isFavorite(toolId: String): Flow<Boolean>

    @Transaction
    suspend fun updateAllOrders(orders: List<Pair<String, Int>>) {
        orders.forEach { (toolId, orderIndex) ->
            updateOrder(toolId, orderIndex)
        }
    }
}
