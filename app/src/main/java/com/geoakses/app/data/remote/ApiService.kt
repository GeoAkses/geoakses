package com.geoakses.app.data.remote

import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("routes")
    suspend fun getRoutes(
        @Query("operator") operator: String? = null,
        @Query("q") query: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<Route>

    @GET("routes/{id}")
    suspend fun getRouteById(
        @Path("id") id: String
    ): Route

    @GET("stops")
    suspend fun getStops(
        @Query("route_id") routeId: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<Stop>

    @GET("stops")
    suspend fun getNearbyStops(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("radius_m") radiusMeters: Int = 5000,
        @Query("route_id") routeId: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<Stop>

    @GET("buses/live")
    suspend fun getLiveBuses(
        @Query("route_id") routeId: String? = null
    ): List<Bus>

    @GET("schedules")
    suspend fun getSchedules(
        @Query("route_id") routeId: String,
        @Query("day_type") dayType: String? = null
    ): List<Schedule>
}
