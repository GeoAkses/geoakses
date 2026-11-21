package com.geoakses.app.data.model

import com.google.gson.annotations.SerializedName

enum class OccupancyStatus {
    @SerializedName("unknown")
    UNKNOWN,
    @SerializedName("empty")
    EMPTY,
    @SerializedName("seats_available")
    SEATS_AVAILABLE,
    @SerializedName("standing_only")
    STANDING_ONLY,
    @SerializedName("full")
    FULL
}

data class Bus(
    @SerializedName("id")
    val id: String,

    @SerializedName("plate_number")
    val plateNumber: String,

    @SerializedName("route_id")
    val routeId: String,

    @SerializedName("route_name")
    val routeName: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("heading")
    val heading: Float = 0f,

    @SerializedName("speed_kmh")
    val speedKmH: Double = 0.0,

    @SerializedName("current_stop")
    val currentStop: String = "",

    @SerializedName("next_stop")
    val nextStop: String = "",

    @SerializedName("occupancy")
    val occupancy: OccupancyStatus = OccupancyStatus.UNKNOWN,

    @SerializedName("eta_minutes")
    val etaMinutes: Int? = null,

    @SerializedName("last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),

    @SerializedName("observed_at")
    val observedAt: String = "",

    @SerializedName("data_status")
    val dataStatus: String = "unavailable",

    @SerializedName("source")
    val source: String = ""
)
