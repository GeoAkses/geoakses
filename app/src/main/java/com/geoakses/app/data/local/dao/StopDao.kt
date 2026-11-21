package com.geoakses.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geoakses.app.data.model.Stop
import kotlinx.coroutines.flow.Flow

@Dao
interface StopDao {

    @Query("SELECT * FROM stops ORDER BY name ASC")
    fun getAllStops(): Flow<List<Stop>>

    @Query("SELECT * FROM stops WHERE id = :id")
    suspend fun getStopById(id: String): Stop?

    @Query("SELECT * FROM stops WHERE corridorsServed LIKE '%' || :corridor || '%'")
    fun getStopsByCorridor(corridor: String): Flow<List<Stop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<Stop>)

    @Transaction
    suspend fun replaceStops(stops: List<Stop>) {
        clearStops()
        insertStops(stops)
    }

    @Query("DELETE FROM stops")
    suspend fun clearStops()
}
