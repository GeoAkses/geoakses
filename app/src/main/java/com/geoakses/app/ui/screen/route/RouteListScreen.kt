package com.geoakses.app.ui.screen.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.data.model.Route
import com.geoakses.app.ui.component.GeoAksesCategoryChip
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.ui.component.GeoAksesLoadingState
import com.geoakses.app.ui.component.GeoAksesSearchBar
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.theme.SemanticSuccess
import com.geoakses.app.ui.viewmodel.RouteViewModel

@Composable
fun RouteListScreen(
    onRouteClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.listState.collectAsState()
    val filters = listOf("Semua", "TMB", "MJT")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GeoAksesTopBar(title = "Daftar Rute", onBack = onBack)
        GeoAksesSearchBar(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = "Cari koridor, nama rute, atau tujuan...",
            modifier = Modifier.padding(horizontal = 16.dp),
            onClear = { viewModel.onSearchQueryChange("") }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                GeoAksesCategoryChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.onFilterChange(filter) },
                    label = filter
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { GeoAksesLoadingState() }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.filteredRoutes.isEmpty()) {
                    item {
                        GeoAksesEmptyState(
                            title = "Koridor tidak ditemukan",
                            description = "Coba operator atau kata kunci lain."
                        )
                    }
                } else {
                    items(uiState.filteredRoutes) { route ->
                        RouteListItem(route = route, onClick = { onRouteClick(route.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RouteListItem(route: Route, onClick: () -> Unit) {
    val routeColor = try {
        Color(android.graphics.Color.parseColor(route.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(routeColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(route.code, style = MaterialTheme.typography.labelMedium, color = routeColor, fontWeight = FontWeight.Bold)
                    if (route.dataStatus == "community" || route.dataStatus == "mixed") {
                        Spacer(Modifier.width(8.dp))
                        Text(if (route.dataStatus == "community") "Komunitas" else "Sumber campuran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (route.isActive) SemanticSuccess.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (route.isActive) "Aktif" else "Tidak Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (route.isActive) SemanticSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(route.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${route.origin} → ${route.destination}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(2.dp))
                        if (route.stopsCount > 0) Text("${route.stopsCount} halte", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(2.dp))
                        Text("Jam ${route.operatingHours}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (route.fare > 0) Text("Rp ${route.fare}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

