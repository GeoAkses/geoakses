package com.geoakses.app.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

@Entity(tableName = "stops")
data class Stop(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String = "",

    @SerializedName("corridors_served")
    val corridorsServed: String = "",

    @SerializedName("is_shelter")
    val isShelter: Boolean = false,

    @SerializedName("is_active")
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true,

    @Ignore
    @SerializedName("distance_meters")
    var distanceMeters: Double = 0.0,

    @SerializedName("data_status")
    @ColumnInfo(defaultValue = "'demo'")
    val dataStatus: String = "unknown",

    @SerializedName("source")
    @ColumnInfo(defaultValue = "'GeoAkses demo'")
    val source: String = "",

    @SerializedName("verified_at")
    val verifiedAt: String? = null
) {
    // Secondary constructor for Room
    constructor(
        id: String,
        name: String,
        latitude: Double,
        longitude: Double,
        address: String,
        corridorsServed: String,
        isShelter: Boolean,
        dataStatus: String,
        source: String,
        isActive: Boolean,
        verifiedAt: String?
    ) : this(id, name, latitude, longitude, address, corridorsServed, isShelter, isActive, 0.0, dataStatus, source, verifiedAt)
}
