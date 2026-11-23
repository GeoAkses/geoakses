package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = true,
    val routes: List<Route> = emptyList(),
    val stops: List<Stop> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val routesLoaded: Boolean = false,
    val stopsLoaded: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    init { loadData() }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null, routesLoaded = false, stopsLoaded = false) }
        viewModelScope.launch {
            repository.getRoutes()
                .catch { error -> finishRouteLoad(error.message) }
                .collect { result ->
                    result.onSuccess { routes ->
                        _uiState.update { it.copy(routes = routes, routesLoaded = true, isLoading = !it.stopsLoaded) }
                    }.onFailure { error -> finishRouteLoad(error.message) }
                }
        }
        viewModelScope.launch {
            repository.getStops()
                .catch { error -> finishStopLoad(error.message) }
                .collect { result ->
                    result.onSuccess { stops ->
                        _uiState.update { it.copy(stops = stops, stopsLoaded = true, isLoading = !it.routesLoaded) }
                    }.onFailure { error -> finishStopLoad(error.message) }
                }
        }
    }

    private fun finishRouteLoad(message: String?) {
        _uiState.update { it.copy(routesLoaded = true, isLoading = !it.stopsLoaded, error = message ?: it.error) }
    }

    private fun finishStopLoad(message: String?) {
        _uiState.update { it.copy(stopsLoaded = true, isLoading = !it.routesLoaded, error = message ?: it.error) }
    }
}
