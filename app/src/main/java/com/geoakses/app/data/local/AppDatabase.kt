package com.geoakses.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.geoakses.app.data.local.dao.RouteDao
import com.geoakses.app.data.local.dao.ScheduleDao
import com.geoakses.app.data.local.dao.StopDao
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop

@Database(
    entities = [Route::class, Stop::class, Schedule::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun stopDao(): StopDao
    abstract fun scheduleDao(): ScheduleDao

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
    }
}
