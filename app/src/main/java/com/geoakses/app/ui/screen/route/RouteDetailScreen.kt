package com.geoakses.app.ui.screen.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.FavoriteKind
import com.geoakses.app.data.model.OccupancyStatus
import com.geoakses.app.data.model.Schedule
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.theme.SemanticErrorContent
import com.geoakses.app.ui.theme.SemanticSuccess
import com.geoakses.app.ui.theme.SemanticWarningContent
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.viewmodel.RouteViewModel
import com.geoakses.app.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailState.collectAsState()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()
    val favoriteItems by favoritesViewModel.favorites.collectAsState()

    LaunchedEffect(routeId) {
        viewModel.loadRouteDetail(routeId)
    }

    val route = uiState.route
    Scaffold(
        topBar = {
            GeoAksesTopBar(
                title = route?.code ?: "Detail Koridor",
                onBack = onBack,
                actions = {
                    route?.let { currentRoute ->
                        val isFavorite = favoriteItems.any {
                            it.itemId == currentRoute.id && it.kind == FavoriteKind.ROUTE
                        }
                        IconButton(onClick = {
                            favoritesViewModel.toggle(
                                currentRoute.id,
                                FavoriteKind.ROUTE,
                                currentRoute.name,
                                "${currentRoute.origin} → ${currentRoute.destination}"
                            )
                        }) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Hapus dari favorit" else "Simpan ke favorit"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (route != null) {
                    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)) {
                                    Text(route.operator, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Text(route.code, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(route.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("${route.origin} → ${route.destination}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("Sumber: ${route.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (!uiState.isLoading) {
                    GeoAksesEmptyState("Koridor tidak ditemukan", "Periksa koneksi atau kembali ke daftar rute.")
                }
            }

            item {
                TabRow(selectedTabIndex = uiState.selectedTab) {
                    Tab(selected = uiState.selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text("Informasi") })
                    Tab(selected = uiState.selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text("Rute") })
                    Tab(selected = uiState.selectedTab == 2, onClick = { viewModel.onTabSelected(2) }, text = { Text("Jadwal") })
                }
            }

            when (uiState.selectedTab) {
                0 -> {
                    if (route != null) {
                        item {
                            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    RouteInfoRow(Icons.Filled.TripOrigin, "Asal", route.origin)
                                    RouteInfoRow(Icons.Filled.Place, "Tujuan", route.destination)
                                    RouteInfoRow(Icons.Filled.AccessTime, "Jam Operasional", route.operatingHours.ifBlank { "Belum tersedia" })
                                    RouteInfoRow(Icons.Filled.AttachMoney, "Tarif dasar", route.fare.takeIf { it > 0 }?.let { "Rp $it" } ?: "Belum tersedia")
                                    RouteInfoRow(Icons.Filled.Place, "Jumlah halte", route.stopsCount.takeIf { it > 0 }?.let { "$it halte" } ?: "Belum tersedia")
                                    if (route.description.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(route.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    TextButton(onClick = onOpenMap) { Text("Lihat halte di peta") }
                                }
                            }
                        }
                        if (uiState.liveBuses.isEmpty()) {
                            item {
                                GeoAksesEmptyState(
                                    title = "Bus belum dapat dilacak",
                                    description = "Feed posisi resmi untuk koridor ini belum tersedia."
                                )
                            }
                        } else {
                            items(uiState.liveBuses) { bus -> BusDetailItem(bus = bus) }
                        }
                    }
                }
                1 -> {
                    if (uiState.stops.isEmpty()) {
                        item { GeoAksesEmptyState("Urutan halte belum tersedia", "Data halte aktif untuk koridor ini belum terverifikasi.") }
                    } else {
                        items(uiState.stops) { stop ->
                            StopRowItem(stop = stop)
                        }
                    }
                }
                2 -> {
                    if (uiState.schedules.isEmpty()) {
                        item { GeoAksesEmptyState("Jadwal belum tersedia", "Jadwal keberangkatan per halte belum terhubung ke sumber data.") }
                    } else {
                        items(uiState.schedules) { schedule ->
                            ScheduleItem(schedule = schedule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StopRowItem(stop: Stop) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(stop.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (stop.address.isNotBlank()) {
                Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BusDetailItem(bus: Bus) {
    val occupancyColor = when (bus.occupancy) {
        OccupancyStatus.EMPTY, OccupancyStatus.SEATS_AVAILABLE -> SemanticSuccess
        OccupancyStatus.STANDING_ONLY -> SemanticWarningContent
        OccupancyStatus.FULL -> SemanticErrorContent
        OccupancyStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bus.plateNumber, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Di: ${bus.currentStop}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Menuju: ${bus.nextStop}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${bus.speedKmH} km/jam", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(bus.etaMinutes?.let { "$it mnt" } ?: "ETA —", style = MaterialTheme.typography.labelLarge, color = occupancyColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScheduleItem(schedule: Schedule) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(schedule.stopName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(schedule.departureTime, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(schedule.dayType.replace('_', ' ').replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (schedule.dataStatus == "demo") {
                    Text("Jadwal contoh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

