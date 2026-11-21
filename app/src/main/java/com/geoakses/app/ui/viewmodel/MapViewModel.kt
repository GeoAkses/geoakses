package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.TransitRepository
import com.geoakses.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val routes: List<Route> = emptyList(),
    val stops: List<Stop> = emptyList(),
    val liveBuses: List<Bus> = emptyList(),
    val selectedRouteId: String? = null,
    val selectedBus: Bus? = null,
    val showStops: Boolean = true,
    val showBuses: Boolean = true,
    val centerLat: Double = Constants.BANDUNG_LATITUDE,
    val centerLon: Double = Constants.BANDUNG_LONGITUDE,
    val zoomLevel: Double = Constants.DEFAULT_MAP_ZOOM
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch {
            repository.getRoutes()
                .catch { }
                .collect { result ->
                    result.onSuccess { routes ->
                        _uiState.update { it.copy(routes = routes, isLoading = false) }
                    }
                }
        }
        viewModelScope.launch {
            repository.getStops()
                .catch { }
                .collect { result ->
                    result.onSuccess { stops ->
                        _uiState.update { it.copy(stops = stops) }
                    }
                }
        }
        viewModelScope.launch {
            repository.getLiveBuses()
                .catch { }
                .collect { result ->
                    result.onSuccess { buses ->
                        _uiState.update { it.copy(liveBuses = buses) }
                    }
                }
        }
    }

    fun selectRoute(routeId: String?) {
        _uiState.update { it.copy(selectedRouteId = routeId) }
    }

    fun selectBus(bus: Bus?) {
        _uiState.update { it.copy(selectedBus = bus) }
    }

    fun toggleStops() {
        _uiState.update { it.copy(showStops = !it.showStops) }
    }

    fun toggleBuses() {
        _uiState.update { it.copy(showBuses = !it.showBuses) }
    }
}
