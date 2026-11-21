package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Stop
import com.geoakses.app.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StopListUiState(
    val isLoading: Boolean = true,
    val stops: List<Stop> = emptyList(),
    val filteredStops: List<Stop> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

data class StopDetailUiState(
    val isLoading: Boolean = true,
    val stop: Stop? = null,
    val error: String? = null
)

@HiltViewModel
class StopViewModel @Inject constructor(
    private val repository: TransitRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(StopListUiState())
    val listState: StateFlow<StopListUiState> = _listState

    private val _detailState = MutableStateFlow(StopDetailUiState())
    val detailState: StateFlow<StopDetailUiState> = _detailState

    init {
        loadStops()
    }

    private fun loadStops() {
        viewModelScope.launch {
            repository.getStops()
                .catch { e -> _listState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { result ->
                    result.onSuccess { stops ->
                        _listState.update {
                            it.copy(stops = stops, filteredStops = stops, isLoading = false)
                        }
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        val stops = _listState.value.stops
        val filtered = if (query.isBlank()) stops
        else stops.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true) ||
            it.corridorsServed.contains(query, ignoreCase = true)
        }
        _listState.update { it.copy(searchQuery = query, filteredStops = filtered) }
    }

    fun loadStopDetail(stopId: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            val stop = repository.getStopById(stopId)
            _detailState.update { it.copy(stop = stop, isLoading = false) }
        }
    }
}
