package com.geoakses.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geoakses.app.data.model.PublicFacility
import kotlinx.coroutines.flow.Flow

@Dao
interface FacilityDao {

    @Query("SELECT * FROM public_facilities ORDER BY name ASC")
    fun getAllFacilities(): Flow<List<PublicFacility>>

    @Query("SELECT * FROM public_facilities WHERE category = :category ORDER BY name ASC")
    fun getFacilitiesByCategory(category: String): Flow<List<PublicFacility>>

    @Query("SELECT * FROM public_facilities WHERE id = :id")
    suspend fun getFacilityById(id: String): PublicFacility?

    @Query("""
        SELECT * FROM public_facilities 
        WHERE name LIKE '%' || :query || '%' 
           OR address LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchFacilities(query: String): Flow<List<PublicFacility>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<PublicFacility>)

    @Transaction
    suspend fun replaceFacilities(facilities: List<PublicFacility>) {
        clearFacilities()
        insertFacilities(facilities)
    }

    @Query("DELETE FROM public_facilities")
    suspend fun clearFacilities()

    @Query("SELECT COUNT(*) FROM public_facilities")
    suspend fun count(): Int
}

