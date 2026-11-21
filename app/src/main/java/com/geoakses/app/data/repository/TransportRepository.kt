package com.geoakses.app.data.repository

import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop
import kotlinx.coroutines.flow.Flow

interface TransportRepository {
    fun getRoutes(): Flow<List<Route>>
    fun getRouteById(id: String): Flow<Route?>
    fun getStops(): Flow<List<Stop>>
    fun getNearbyStops(latitude: Double, longitude: Double, radiusKm: Double = 5.0): Flow<List<Stop>>
    fun getLiveBuses(routeId: String? = null): Flow<List<Bus>>
    fun getSchedules(routeId: String): Flow<List<Schedule>>
    suspend fun refreshRoutes()
    suspend fun refreshStops()
}
