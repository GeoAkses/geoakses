package com.geoakses.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Onboarding: Screen("onboarding")
    object Home      : Screen("home")
    object Map       : Screen("map")
    object Routes    : Screen("routes")
    object Stops     : Screen("stops")
    object Profile   : Screen("profile")
    object Fares     : Screen("fares")
    object Guide     : Screen("guide")
    object RouteDetail: Screen("route_detail/{routeId}") {
        fun createRoute(routeId: String) = "route_detail/$routeId"
    }
    object StopDetail: Screen("stop_detail/{stopId}") {
        fun createRoute(stopId: String) = "stop_detail/$stopId"
    }
}
