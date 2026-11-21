package com.geoakses.app.di

import android.content.Context
import androidx.room.Room
import com.geoakses.app.data.local.AppDatabase
import com.geoakses.app.data.local.dao.RouteDao
import com.geoakses.app.data.local.dao.ScheduleDao
import com.geoakses.app.data.local.dao.StopDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "geoakses_database"
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build()
    }

    @Provides
    @Singleton
    fun provideRouteDao(db: AppDatabase): RouteDao = db.routeDao()

    @Provides
    @Singleton
    fun provideStopDao(db: AppDatabase): StopDao = db.stopDao()

    @Provides
    @Singleton
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()
}
