package com.geoakses.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geoakses.app.data.model.Route
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes ORDER BY code ASC")
    fun getAllRoutes(): Flow<List<Route>>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: String): Route?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<Route>)

    @Transaction
    suspend fun replaceRoutes(routes: List<Route>) {
        clearRoutes()
        insertRoutes(routes)
    }

    @Query("DELETE FROM routes")
    suspend fun clearRoutes()
}
