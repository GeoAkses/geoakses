package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val routes: List<Route> = emptyList(),
    val stops: List<Stop> = emptyList(),
    val nearbyStops: List<Stop> = emptyList(),
    val liveBuses: List<Bus> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val locationLabel: String = "Gunakan lokasi atau cari halte secara manual",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getRoutes()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { result ->
                    result.onSuccess { routes ->
                        _uiState.update { it.copy(routes = routes, isLoading = false) }
                    }
                }
        }
        viewModelScope.launch {
            repository.getStops().collect { result ->
                result.onSuccess { stops ->
                        _uiState.update { state ->
                            val latitude = state.latitude
                            val longitude = state.longitude
                            state.copy(
                                stops = stops,
                                nearbyStops = if (latitude != null && longitude != null) {
                                    closestStops(stops, latitude, longitude)
                                } else emptyList()
                            )
                        }
                }
                result.onFailure { error -> _uiState.update { it.copy(error = error.message) } }
            }
        }
        viewModelScope.launch {
            repository.getLiveBuses()
                .catch { }
                .collect { result ->
                    result.onSuccess { buses ->
                        _uiState.update { it.copy(liveBuses = buses) }
                    }
                    result.onFailure { error -> _uiState.update { it.copy(error = error.message) } }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onLocationUpdated(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            onLocationUnavailable()
            return
        }
        _uiState.update { state ->
            state.copy(
                locationLabel = "Berdasarkan lokasi perangkat",
                error = null,
                latitude = latitude,
                longitude = longitude,
                nearbyStops = closestStops(state.stops, latitude, longitude)
            )
        }
    }

    fun onLocationUnavailable() {
        _uiState.update { it.copy(error = "Lokasi belum tersedia. Aktifkan GPS lalu coba lagi.") }
    }

    private fun closestStops(stops: List<Stop>, latitude: Double, longitude: Double): List<Stop> = stops
        .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
        .map { stop -> stop.copy(distanceMeters = distanceMeters(latitude, longitude, stop.latitude, stop.longitude)) }
        .sortedBy(Stop::distanceMeters)
        .take(5)

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radians = Math.PI / 180.0
        val dLat = (lat2 - lat1) * radians
        val dLon = (lon2 - lon1) * radians
        val a = kotlin.math.sin(dLat / 2).let { it * it } +
            kotlin.math.cos(lat1 * radians) * kotlin.math.cos(lat2 * radians) *
            kotlin.math.sin(dLon / 2).let { it * it }
        return 6_371_000.0 * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}
