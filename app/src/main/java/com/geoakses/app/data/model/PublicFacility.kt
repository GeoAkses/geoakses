package com.geoakses.app.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Represents a public facility in Bandung such as hospitals, schools,
 * libraries, parks, and sports venues.
 *
 * Data sourced from Portal Geodata Kota Bandung, OpenStreetMap, and
 * publicly available municipal information. Each record carries
 * [dataStatus] and [source] for provenance.
 */
@Entity(tableName = "public_facilities")
data class PublicFacility(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("address")
    val address: String = "",

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("operating_hours")
    val operatingHours: String = "Belum tersedia",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("accessibility_info")
    val accessibilityInfo: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("data_status")
    val dataStatus: String = "community",

    @SerializedName("source")
    val source: String = "",

    @SerializedName("verified_at")
    val verifiedAt: String? = null,

    @Ignore
    @SerializedName("distance_meters")
    var distanceMeters: Double = 0.0
) {
    // Secondary constructor for Room (excludes @Ignore field)
    constructor(
        id: String,
        name: String,
        category: String,
        address: String,
        latitude: Double,
        longitude: Double,
        operatingHours: String,
        description: String,
        accessibilityInfo: String,
        phone: String,
        dataStatus: String,
        source: String,
        verifiedAt: String?
    ) : this(
        id, name, category, address, latitude, longitude,
        operatingHours, description, accessibilityInfo, phone,
        dataStatus, source, verifiedAt, 0.0
    )
}

/**
 * Supported facility categories used for filtering in the UI.
 */
object FacilityCategory {
    const val ALL = "Semua"
    const val HOSPITAL = "Rumah Sakit"
    const val PUSKESMAS = "Puskesmas"
    const val LIBRARY = "Perpustakaan"
    const val SCHOOL = "Sekolah"
    const val UNIVERSITY = "Universitas"
    const val PARK = "Taman"
    const val SPORTS = "Olahraga"

    val ALL_CATEGORIES = listOf(ALL, HOSPITAL, PUSKESMAS, LIBRARY, SCHOOL, UNIVERSITY, PARK, SPORTS)
}

