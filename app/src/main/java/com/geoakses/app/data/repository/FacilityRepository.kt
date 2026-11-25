package com.geoakses.app.data.repository

import android.content.Context
import com.geoakses.app.data.local.dao.FacilityDao
import com.geoakses.app.data.model.FacilityCategory
import com.geoakses.app.data.model.PublicFacility
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for public facility data. On first launch it seeds the Room
 * database from the bundled JSON asset `facilities_bandung.json`.
 * Subsequent reads come directly from Room.
 */
@Singleton
class FacilityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val facilityDao: FacilityDao
) {

    /**
     * Returns all facilities, seeding from the asset file on first access.
     */
    fun getAllFacilities(): Flow<List<PublicFacility>> = flow {
        ensureSeeded()
        emitAll(facilityDao.getAllFacilities())
    }

    /**
     * Returns facilities filtered by [category].
     * Pass [FacilityCategory.ALL] to get all facilities.
     */
    fun getFacilitiesByCategory(category: String): Flow<List<PublicFacility>> = flow {
        ensureSeeded()
        if (category == FacilityCategory.ALL) {
            emitAll(facilityDao.getAllFacilities())
        } else {
            emitAll(facilityDao.getFacilitiesByCategory(category))
        }
    }

    /**
     * Full-text search across name, address, and description.
     */
    fun searchFacilities(query: String): Flow<List<PublicFacility>> = flow {
        ensureSeeded()
        if (query.isBlank()) {
            emitAll(facilityDao.getAllFacilities())
        } else {
            emitAll(facilityDao.searchFacilities(query))
        }
    }

    /**
     * Returns a single facility by its id.
     */
    suspend fun getFacilityById(id: String): PublicFacility? {
        ensureSeeded()
        return facilityDao.getFacilityById(id)
    }

    /**
     * Returns facilities near the given coordinates, sorted by distance.
     */
    fun getNearbyFacilities(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0,
        category: String = FacilityCategory.ALL
    ): Flow<List<PublicFacility>> {
        val source = if (category == FacilityCategory.ALL) getAllFacilities()
        else getFacilitiesByCategory(category)
        return source.map { list ->
            list.map { facility ->
                facility.copy(distanceMeters = distanceMeters(latitude, longitude, facility.latitude, facility.longitude))
            }
                .filter { it.distanceMeters <= radiusKm * 1000 }
                .sortedBy(PublicFacility::distanceMeters)
        }
    }

    // --- Internal helpers ---

    private suspend fun ensureSeeded() {
        if (facilityDao.count() == 0) {
            val facilities = loadFromAssets()
            if (facilities.isNotEmpty()) {
                facilityDao.insertFacilities(facilities)
            }
        }
    }

    private fun loadFromAssets(): List<PublicFacility> {
        return try {
            val json = context.assets.open("facilities_bandung.json")
                .bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<PublicFacility>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
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

