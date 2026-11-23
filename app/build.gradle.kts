plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.geoakses.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.geoakses.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseKeystorePath = System.getenv("KEYSTORE_FILE")
        ?: (project.findProperty("KEYSTORE_FILE") as? String)
        ?: "release.keystore"
    val releaseStorePassword = System.getenv("STORE_PASSWORD")
        ?: (project.findProperty("STORE_PASSWORD") as? String)
    val releaseKeyAlias = System.getenv("KEY_ALIAS")
        ?: (project.findProperty("KEY_ALIAS") as? String)
    val releaseKeyPassword = System.getenv("KEY_PASSWORD")
        ?: (project.findProperty("KEY_PASSWORD") as? String)
    val releaseKeystoreFile = rootProject.file(releaseKeystorePath)
    val hasReleaseSigning = releaseKeystoreFile.isFile &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()
    val apiBaseUrlOverride = providers.gradleProperty("geoaksesApiBaseUrl").orNull
        ?: providers.environmentVariable("GEOAKSES_API_BASE_URL").orNull

    fun apiBaseUrlField(value: String, allowHttp: Boolean): String {
        require(value.endsWith("/")) { "GeoAkses API base URL must end with /" }
        require(value.startsWith("https://") || (allowHttp && value.startsWith("http://"))) {
            "GeoAkses API base URL must use HTTPS outside debug builds"
        }
        return "\"$value\""
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreFile
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "API_BASE_URL",
                apiBaseUrlField(apiBaseUrlOverride ?: "http://10.0.2.2:8080/api/v1/", allowHttp = true)
            )
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            buildConfigField(
                "String",
                "API_BASE_URL",
                apiBaseUrlField(apiBaseUrlOverride ?: "https://api.geoakses.invalid/api/v1/", allowHttp = false)
            )
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName(
                if (hasReleaseSigning) "release" else "debug"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM & UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // OSMDroid
    implementation(libs.osmdroid.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // Play Services Location
    implementation(libs.play.services.location)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
