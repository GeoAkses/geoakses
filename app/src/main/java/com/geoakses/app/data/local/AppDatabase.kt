package com.geoakses.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.geoakses.app.data.local.dao.FacilityDao
import com.geoakses.app.data.local.dao.RouteDao
import com.geoakses.app.data.local.dao.FavoriteDao
import com.geoakses.app.data.local.dao.ScheduleDao
import com.geoakses.app.data.local.dao.StopDao
import com.geoakses.app.data.model.PublicFacility
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Favorite
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop

@Database(
    entities = [Route::class, Stop::class, Schedule::class, Favorite::class, PublicFacility::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun stopDao(): StopDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun facilityDao(): FacilityDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routes ADD COLUMN dataStatus TEXT NOT NULL DEFAULT 'demo'")
                db.execSQL("ALTER TABLE routes ADD COLUMN source TEXT NOT NULL DEFAULT 'GeoAkses demo'")
                db.execSQL("ALTER TABLE stops ADD COLUMN dataStatus TEXT NOT NULL DEFAULT 'demo'")
                db.execSQL("ALTER TABLE stops ADD COLUMN source TEXT NOT NULL DEFAULT 'GeoAkses demo'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS schedules (
                        id TEXT NOT NULL,
                        routeId TEXT NOT NULL,
                        stopName TEXT NOT NULL,
                        departureTime TEXT NOT NULL,
                        estimatedArrival TEXT,
                        dayType TEXT NOT NULL,
                        isRealtime INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        dataStatus TEXT NOT NULL DEFAULT 'demo',
                        source TEXT NOT NULL DEFAULT 'GeoAkses demo',
                        stopId TEXT,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_schedules_routeId ON schedules(routeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_schedules_departureTime ON schedules(departureTime)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM routes WHERE dataStatus = 'demo'")
                db.execSQL("DELETE FROM stops WHERE dataStatus = 'demo'")
                db.execSQL("DELETE FROM schedules WHERE dataStatus = 'demo'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routes ADD COLUMN verifiedAt TEXT")
                db.execSQL("ALTER TABLE stops ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE stops ADD COLUMN verifiedAt TEXT")
                db.execSQL("ALTER TABLE schedules ADD COLUMN verifiedAt TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorites (
                        itemId TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        subtitle TEXT NOT NULL,
                        savedAt INTEGER NOT NULL,
                        PRIMARY KEY(itemId, kind)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS public_facilities (
                        id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        address TEXT NOT NULL DEFAULT '',
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        operatingHours TEXT NOT NULL DEFAULT 'Belum tersedia',
                        description TEXT NOT NULL DEFAULT '',
                        accessibilityInfo TEXT NOT NULL DEFAULT '',
                        phone TEXT NOT NULL DEFAULT '',
                        dataStatus TEXT NOT NULL DEFAULT 'community',
                        source TEXT NOT NULL DEFAULT '',
                        verifiedAt TEXT,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
            }
        }
    }
}
