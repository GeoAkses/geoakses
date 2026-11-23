package com.geoakses.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

@Entity(tableName = "schedules", indices = [Index("routeId"), Index("departureTime")])
data class Schedule(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("route_id")
    val routeId: String,

    @SerializedName("stop_name")
    val stopName: String,

    @SerializedName("departure_time")
    val departureTime: String,

    @SerializedName("estimated_arrival")
    val estimatedArrival: String? = null,

    @SerializedName("day_type")
    val dayType: String = "unknown",

    @SerializedName("is_realtime")
    val isRealtime: Boolean = false,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("data_status")
    @ColumnInfo(defaultValue = "'demo'")
    val dataStatus: String = "unknown",

    @SerializedName("source")
    @ColumnInfo(defaultValue = "'GeoAkses demo'")
    val source: String = "",

    @SerializedName("stop_id")
    val stopId: String? = null,

    @SerializedName("verified_at")
    val verifiedAt: String? = null
)
