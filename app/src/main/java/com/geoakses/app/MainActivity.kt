package com.geoakses.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geoakses.app.ui.component.BottomNavBar
import com.geoakses.app.ui.navigation.NavGraph
import com.geoakses.app.ui.navigation.Screen
import com.geoakses.app.ui.theme.GeoAksesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = remember { getSharedPreferences("geoakses_preferences", MODE_PRIVATE) }
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
            GeoAksesTheme(darkTheme = darkTheme) {
                MainApp(darkTheme = darkTheme, onDarkThemeChange = { enabled ->
                    darkTheme = enabled
                    preferences.edit().putBoolean("dark_theme", enabled).apply()
                })
            }
        }
    }
}

@Composable
fun MainApp(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation on splash, onboarding, and detail screens
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Map.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            darkTheme = darkTheme,
            onDarkThemeChange = onDarkThemeChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
