package com.geoakses.app.ui.screen.search

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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.component.GeoAksesCategoryChip
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.ui.component.GeoAksesErrorState
import com.geoakses.app.ui.component.GeoAksesLoadingState
import com.geoakses.app.ui.component.GeoAksesSearchBar
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onRouteClick: (String) -> Unit,
    onStopClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var category by remember { mutableStateOf("Semua") }
    val query = uiState.query.trim()
    val routes = uiState.routes.filter { route ->
        category != "Halte" && category != "Fasilitas" &&
            (query.isBlank() || listOf(route.code, route.name, route.origin, route.destination)
                .any { value -> value.contains(query, ignoreCase = true) })
    }
    val stops = uiState.stops.filter { stop ->
        category != "Rute" && category != "Fasilitas" &&
            (query.isBlank() || listOf(stop.name, stop.address, stop.corridorsServed)
                .any { value -> value.contains(query, ignoreCase = true) })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GeoAksesTopBar(title = "Pencarian", onBack = onBack)
        GeoAksesSearchBar(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = "Cari halte, rute, atau lokasi...",
            modifier = Modifier.padding(horizontal = 16.dp),
            onClear = { viewModel.onQueryChange("") }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semua", "Rute", "Halte", "Fasilitas").forEach { item ->
                GeoAksesCategoryChip(label = item, selected = category == item, onClick = { category = item })
            }
        }

        when {
            uiState.isLoading -> GeoAksesLoadingState(modifier = Modifier.weight(1f))
            category == "Fasilitas" -> GeoAksesEmptyState(
                title = "Data fasilitas belum tersedia",
                description = "Informasi fasilitas umum belum terhubung ke sumber data GeoAkses.",
                modifier = Modifier.padding(16.dp)
            )
            query.isNotBlank() && routes.isEmpty() && stops.isEmpty() && uiState.error != null -> GeoAksesErrorState(
                title = "Pencarian belum berhasil",
                description = "Periksa koneksi Anda lalu coba kembali.",
                onRetry = viewModel::loadData,
                modifier = Modifier.padding(16.dp)
            )
            routes.isEmpty() && stops.isEmpty() -> GeoAksesEmptyState(
                title = if (query.isBlank()) "Cari rute atau halte" else "Tidak ada hasil",
                description = if (query.isBlank()) "Masukkan nama, kode koridor, atau alamat." else "Coba kata kunci atau kategori lain.",
                modifier = Modifier.padding(16.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (routes.isNotEmpty()) {
                    item { Text("Rute", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(routes, key = { "route:${it.id}" }) { route ->
                        SearchRouteCard(route = route, onClick = { onRouteClick(route.id) })
                    }
                }
                if (stops.isNotEmpty()) {
                    item { Text("Halte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(stops, key = { "stop:${it.id}" }) { stop ->
                        SearchStopCard(stop = stop, onClick = { onStopClick(stop.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchRouteCard(route: Route, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("${route.code} · ${route.operator}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(route.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${route.origin} → ${route.destination}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SearchStopCard(stop: Stop, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Halte", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(stop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (stop.address.isNotBlank()) Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
