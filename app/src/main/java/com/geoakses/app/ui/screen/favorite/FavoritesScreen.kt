package com.geoakses.app.ui.screen.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.data.model.Favorite
import com.geoakses.app.data.model.FavoriteKind
import com.geoakses.app.ui.component.GeoAksesCategoryChip
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onBrowse: () -> Unit,
    onFavoriteClick: (Favorite) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    var filter by remember { mutableStateOf("Semua") }
    val filteredFavorites = favorites.filter { favorite ->
        when (filter) {
            "Rute" -> favorite.kind == FavoriteKind.ROUTE
            "Halte" -> favorite.kind == FavoriteKind.STOP
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
        GeoAksesTopBar(title = "Favorit")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semua", "Rute", "Halte").forEach { item ->
                GeoAksesCategoryChip(label = item, selected = filter == item, onClick = { filter = item })
            }
        }

        if (filteredFavorites.isEmpty()) {
            val title = if (favorites.isEmpty()) "Belum ada favorit" else "Belum ada ${filter.lowercase()} favorit"
            GeoAksesEmptyState(
                title = title,
                description = "Simpan rute atau halte dengan ikon hati agar mudah ditemukan lagi.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                icon = Icons.Filled.FavoriteBorder,
                actionLabel = "Jelajahi rute",
                onAction = onBrowse
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFavorites, key = { "${it.kind}:${it.itemId}" }) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        onClick = { onFavoriteClick(favorite) },
                        onRemove = { viewModel.remove(favorite.itemId, favorite.kind) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (favorite.kind == FavoriteKind.ROUTE) Icons.Filled.DirectionsBus else Icons.Filled.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (favorite.kind == FavoriteKind.ROUTE) "Rute favorit" else "Halte favorit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(favorite.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (favorite.subtitle.isNotBlank()) {
                    Text(favorite.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Favorite, contentDescription = "Hapus dari favorit", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
