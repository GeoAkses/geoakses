package com.geoakses.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geoakses.app.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE isActive = 1 ORDER BY departureTime ASC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE routeId = :routeId AND isActive = 1 ORDER BY departureTime ASC")
    fun getByRoute(routeId: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE routeId = :routeId AND isActive = 1 ORDER BY departureTime ASC")
    suspend fun getByRouteOnce(routeId: String): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedules: List<Schedule>)

    @Query("DELETE FROM schedules")
    suspend fun clear()

    @Transaction
    suspend fun replace(schedules: List<Schedule>) {
        clear()
        insert(schedules)
    }
}
