package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

data class RouteListUiState(
    val isLoading: Boolean = true,
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val error: String? = null
)

data class RouteDetailUiState(
    val isLoading: Boolean = true,
    val route: Route? = null,
    val stops: List<Stop> = emptyList(),
    val liveBuses: List<Bus> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(RouteListUiState())
    val listState: StateFlow<RouteListUiState> = _listState

    private val _detailState = MutableStateFlow(RouteDetailUiState())
    val detailState: StateFlow<RouteDetailUiState> = _detailState

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            repository.getRoutes()
                .catch { e -> _listState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { result ->
                    result.onSuccess { routes ->
                        _listState.update { it.copy(routes = routes, isLoading = false) }
                        applyFilters()
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _listState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterChange(filter: String) {
        _listState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _listState.value
        val filtered = state.routes.filter { route ->
            val matchesOperator = when (state.selectedFilter) {
                "TMB" -> route.operator.equals("TMB", true) || route.operator.contains("Trans Metro Bandung", true)
                "MJT" -> route.operator.equals("MJT", true) || route.operator.contains("Metro Jabar Trans", true)
                else -> true
            }
            val query = state.searchQuery.trim()
            matchesOperator && (query.isEmpty() || route.name.contains(query, true) || route.code.contains(query, true))
        }
        _listState.update { it.copy(filteredRoutes = filtered) }
    }

    fun loadRouteDetail(routeId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            try {
                val route = repository.getRouteById(routeId)
                _detailState.update { it.copy(route = route, isLoading = false) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _detailState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
        viewModelScope.launch {
            repository.getStopsByRoute(routeId)
                .catch { }
                .collect { stops ->
                    _detailState.update { it.copy(stops = stops) }
                }
        }
        viewModelScope.launch {
            repository.getLiveBuses(routeId)
                .catch { }
                .collect { result ->
                    result.onSuccess { buses ->
                        _detailState.update { it.copy(liveBuses = buses) }
                    }
                }
        }
        viewModelScope.launch {
            repository.getSchedules(routeId)
                .catch { }
                .collect { result ->
                    result.onSuccess { schedules ->
                        _detailState.update { it.copy(schedules = schedules) }
                    }
                }
        }
    }

    fun onTabSelected(tab: Int) {
        _detailState.update { it.copy(selectedTab = tab) }
    }
}
