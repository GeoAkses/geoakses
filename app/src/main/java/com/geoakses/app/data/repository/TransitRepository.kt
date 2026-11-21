package com.geoakses.app.data.repository

import com.geoakses.app.data.local.dao.RouteDao
import com.geoakses.app.data.local.dao.ScheduleDao
import com.geoakses.app.data.local.dao.StopDao
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.remote.ApiService
import com.geoakses.app.util.Constants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitRepository @Inject constructor(
    private val apiService: ApiService,
    private val routeDao: RouteDao,
    private val stopDao: StopDao,
    private val scheduleDao: ScheduleDao
) {

    fun getRoutes(): Flow<Result<List<Route>>> = flow {
        val cached = routeDao.getAllRoutes().first()
        if (cached.isEmpty()) {
            routeDao.insertRoutes(Constants.SOURCED_ROUTES)
            emit(Result.success(Constants.SOURCED_ROUTES))
        } else {
            emit(Result.success(cached))
        }
        try {
            val remote = apiService.getRoutes()
            if (remote.isNotEmpty()) {
                routeDao.replaceRoutes(remote)
                emit(Result.success(remote))
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
        }
    }

    fun getAllRoutesLocal(): Flow<List<Route>> = routeDao.getAllRoutes()

    suspend fun getRouteById(id: String): Route? {
        val cached = routeDao.getRouteById(id)
            ?: Constants.SOURCED_ROUTES.find { it.id == id }
        if (cached != null) return cached
        return try {
            val remote = apiService.getRouteById(id)
            routeDao.insertRoutes(listOf(remote.copy(stops = emptyList())))
            if (remote.stops.isNotEmpty()) stopDao.insertStops(remote.stops)
            remote
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            cached
        }
    }

    fun getStops(): Flow<Result<List<Stop>>> = flow {
        val cached = stopDao.getAllStops().first()
        emit(Result.success(cached))
        try {
            val remote = apiService.getStops()
            if (remote.isNotEmpty()) {
                stopDao.replaceStops(remote)
                emit(Result.success(remote))
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
        }
    }

    fun getAllStopsLocal(): Flow<List<Stop>> = stopDao.getAllStops()

    suspend fun getStopById(id: String): Stop? = stopDao.getStopById(id)

    fun getStopsByRoute(routeId: String): Flow<List<Stop>> = flow {
        val route = getRouteById(routeId)
        if (route?.stops?.isNotEmpty() == true) stopDao.insertStops(route.stops)
        emitAll(stopDao.getStopsByCorridor(route?.code ?: routeId))
    }

    fun getNearbyStops(
        latitude: Double = Constants.BANDUNG_LATITUDE,
        longitude: Double = Constants.BANDUNG_LONGITUDE,
        radiusKm: Double = 5.0
    ): Flow<List<Stop>> = getStops().map { result ->
        result.getOrDefault(emptyList())
            .map { stop -> stop.copy(distanceMeters = distanceMeters(latitude, longitude, stop.latitude, stop.longitude)) }
            .filter { it.distanceMeters <= radiusKm * 1000 }
            .sortedBy(Stop::distanceMeters)
    }

    fun getLiveBuses(routeId: String? = null): Flow<Result<List<Bus>>> = flow {
        emit(Result.success(emptyList()))
        while (true) {
            try {
                emit(Result.success(apiService.getLiveBuses(routeId)))
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                emit(Result.success(emptyList()))
            }
            delay(10_000L)
        }
    }

    fun getSchedules(routeId: String): Flow<Result<List<Schedule>>> = flow {
        val cached = scheduleDao.getByRouteOnce(routeId)
        emit(Result.success(cached))
        try {
            val remote = apiService.getSchedules(routeId)
            if (remote.isNotEmpty()) {
                scheduleDao.insert(remote)
                emit(Result.success(remote))
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
        }
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0
        val radians = Math.PI / 180.0
        val dLat = (lat2 - lat1) * radians
        val dLon = (lon2 - lon1) * radians
        val a = kotlin.math.sin(dLat / 2).let { it * it } +
            kotlin.math.cos(lat1 * radians) * kotlin.math.cos(lat2 * radians) *
            kotlin.math.sin(dLon / 2).let { it * it }
        return earthRadius * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}
