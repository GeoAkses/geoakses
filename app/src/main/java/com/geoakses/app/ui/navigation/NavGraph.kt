package com.geoakses.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.geoakses.app.ui.screen.home.HomeScreen
import com.geoakses.app.ui.screen.guide.GuideScreen
import com.geoakses.app.ui.screen.map.MapScreen
import com.geoakses.app.ui.screen.onboarding.OnboardingScreen
import com.geoakses.app.ui.screen.profile.ProfileScreen
import com.geoakses.app.ui.screen.route.RouteDetailScreen
import com.geoakses.app.ui.screen.route.RouteListScreen
import com.geoakses.app.ui.screen.fare.FareScreen
import com.geoakses.app.ui.screen.splash.SplashScreen
import com.geoakses.app.ui.screen.stop.StopDetailScreen
import com.geoakses.app.ui.screen.stop.StopListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToNext = {
                    val next = if (context.getSharedPreferences("geoakses_preferences", 0)
                            .getBoolean("onboarding_complete", false)) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(next) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    context.getSharedPreferences("geoakses_preferences", 0)
                        .edit().putBoolean("onboarding_complete", true).apply()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onRouteClick = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                },
                onStopClick = { stopId ->
                    navController.navigate(Screen.StopDetail.createRoute(stopId))
                },
                onViewAllRoutes = { navController.navigate(Screen.Routes.route) },
                onViewAllStops  = { navController.navigate(Screen.Stops.route) },
                onOpenMap = { navController.navigate(Screen.Map.route) },
                onOpenFares = { navController.navigate(Screen.Fares.route) },
                onOpenGuide = { navController.navigate(Screen.Guide.route) }
            )
        }

        composable(Screen.Fares.route) {
            FareScreen(
                onBack = { navController.popBackStack() },
                onRouteClick = { routeId -> navController.navigate(Screen.RouteDetail.createRoute(routeId)) }
            )
        }

        composable(Screen.Guide.route) {
            GuideScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Map.route) {
            MapScreen()
        }

        composable(Screen.Routes.route) {
            RouteListScreen(
                onRouteClick = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                }
            )
        }

        composable(
            route = Screen.RouteDetail.route,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
            RouteDetailScreen(
                routeId = routeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stops.route) {
            StopListScreen(
                onStopClick = { stopId ->
                    navController.navigate(Screen.StopDetail.createRoute(stopId))
                }
            )
        }

        composable(
            route = Screen.StopDetail.route,
            arguments = listOf(navArgument("stopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val stopId = backStackEntry.arguments?.getString("stopId") ?: return@composable
            StopDetailScreen(
                stopId = stopId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                onOpenGuide = { navController.navigate(Screen.Guide.route) }
            )
        }
    }
}
