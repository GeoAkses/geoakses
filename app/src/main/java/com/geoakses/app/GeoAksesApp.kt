package com.geoakses.app

import android.app.Application
import android.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File

@HiltAndroidApp
class GeoAksesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure OSMDroid user agent & tile cache
        @Suppress("DEPRECATION")
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        Configuration.getInstance().apply {
            val tileCache = File(cacheDir, "osmdroid").apply { mkdirs() }
            osmdroidBasePath = tileCache
            osmdroidTileCache = File(tileCache, "tiles").apply { mkdirs() }
            userAgentValue = packageName
        }
    }
}
