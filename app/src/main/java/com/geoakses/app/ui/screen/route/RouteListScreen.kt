package com.geoakses.app.ui.screen.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.geoakses.app.ui.viewmodel.RouteViewModel

@Composable
fun RouteListScreen(
    onRouteClick: (String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.listState.collectAsState()
    val filters = listOf("Semua", "TMB", "MJT")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
                .padding(top = 8.dp)
        ) {
            Column {
                Text(
                    "Daftar Rute & Koridor",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Cari koridor TMB/MJT...", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.onFilterChange(filter) },
                    label = { Text(filter) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.filteredRoutes.isEmpty()) {
                    item {
                        Text(
                            "Tidak ada koridor yang cocok. Coba operator atau kata kunci lain.",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(if (route.isActive) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (route.isActive) "Aktif" else "Tidak Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (route.isActive) Color(0xFF2E7D32) else Color.Gray
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

