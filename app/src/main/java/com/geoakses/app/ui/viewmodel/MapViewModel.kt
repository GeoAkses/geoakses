package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.FacilityCategory
import com.geoakses.app.data.model.PublicFacility
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.FacilityRepository
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
    val facilities: List<PublicFacility> = emptyList(),
    val selectedRouteId: String? = null,
    val selectedBus: Bus? = null,
    val selectedStop: Stop? = null,
    val selectedFacility: PublicFacility? = null,
    val selectedFacilityCategory: String = FacilityCategory.ALL,
    val selectedLayer: String = "Semua",
    val showStops: Boolean = true,
    val showBuses: Boolean = true,
    val showFacilities: Boolean = true,
    val centerLat: Double = Constants.BANDUNG_LATITUDE,
    val centerLon: Double = Constants.BANDUNG_LONGITUDE,
    val zoomLevel: Double = Constants.DEFAULT_MAP_ZOOM,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TransitRepository,
    private val facilityRepository: FacilityRepository
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
            facilityRepository.getAllFacilities()
                .catch { }
                .collect { facilities ->
                    _uiState.update { it.copy(facilities = facilities) }
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
        _uiState.update { it.copy(selectedBus = bus, selectedStop = null, selectedFacility = null) }
    }

    fun selectStop(stop: Stop?) {
        _uiState.update { it.copy(selectedStop = stop, selectedBus = null, selectedFacility = null) }
    }

    fun selectFacility(facility: PublicFacility?) {
        _uiState.update { it.copy(selectedFacility = facility, selectedStop = null, selectedBus = null) }
    }

    fun selectFacilityCategory(category: String) {
        _uiState.update { it.copy(selectedFacilityCategory = category, showFacilities = true) }
    }

    fun selectLayer(layer: String) {
        _uiState.update { state ->
            when (layer) {
                "TMB", "MJT" -> state.copy(selectedLayer = layer, showStops = true, showBuses = true, showFacilities = true, selectedRouteId = null)
                "Halte" -> state.copy(selectedLayer = layer, showStops = true, showBuses = false, showFacilities = false, selectedRouteId = null)
                "Bus" -> state.copy(selectedLayer = layer, showStops = false, showBuses = true, showFacilities = false, selectedRouteId = null)
                "Fasilitas" -> state.copy(selectedLayer = layer, showStops = false, showBuses = false, showFacilities = true, selectedRouteId = null)
                else -> state.copy(selectedLayer = "Semua", showStops = true, showBuses = true, showFacilities = true, selectedRouteId = null)
            }
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return
        _uiState.update { it.copy(userLatitude = latitude, userLongitude = longitude, centerLat = latitude, centerLon = longitude, zoomLevel = 16.0) }
    }

    fun toggleStops() {
        _uiState.update { it.copy(showStops = !it.showStops) }
    }

    fun toggleBuses() {
        _uiState.update { it.copy(showBuses = !it.showBuses) }
    }
}