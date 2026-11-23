package com.geoakses.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoakses.app.data.model.Favorite
import com.geoakses.app.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {
    val favorites: StateFlow<List<Favorite>> = repository.observeFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun toggle(itemId: String, kind: String, displayName: String, subtitle: String) {
        viewModelScope.launch { repository.toggle(itemId, kind, displayName, subtitle) }
    }

    fun remove(itemId: String, kind: String) {
        viewModelScope.launch { repository.remove(itemId, kind) }
    }
}
