package com.geoakses.app.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("destination")
    val destination: String,

    @SerializedName("operator")
    val operator: String = "unknown",

    @SerializedName("fare")
    val fare: Int = 0,

    @SerializedName("operating_hours")
    val operatingHours: String = "Belum tersedia",

    @SerializedName("headway_minutes")
    val headwayMinutes: Int = 0,

    @SerializedName("color_hex")
    val colorHex: String = "#757575",

    @SerializedName("stops_count")
    val stopsCount: Int = 0,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("description")
    val description: String = "",

    @Ignore
    @SerializedName("stops")
    val stops: List<Stop> = emptyList(),

    @SerializedName("data_status")
    @ColumnInfo(defaultValue = "'demo'")
    val dataStatus: String = "unknown",

    @SerializedName("source")
    @ColumnInfo(defaultValue = "'GeoAkses demo'")
    val source: String = "",

    @SerializedName("verified_at")
    val verifiedAt: String? = null
) {
    constructor(
        id: String,
        code: String,
        name: String,
        origin: String,
        destination: String,
        operator: String,
        fare: Int,
        operatingHours: String,
        headwayMinutes: Int,
        colorHex: String,
        stopsCount: Int,
        isActive: Boolean,
        description: String,
        dataStatus: String,
        source: String,
        verifiedAt: String?
    ) : this(
        id, code, name, origin, destination, operator, fare, operatingHours,
        headwayMinutes, colorHex, stopsCount, isActive, description, emptyList(), dataStatus, source, verifiedAt
    )
}
