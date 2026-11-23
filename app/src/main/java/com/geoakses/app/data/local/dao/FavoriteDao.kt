package com.geoakses.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geoakses.app.data.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId AND kind = :kind)")
    suspend fun contains(itemId: String, kind: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE itemId = :itemId AND kind = :kind")
    suspend fun remove(itemId: String, kind: String)
}
