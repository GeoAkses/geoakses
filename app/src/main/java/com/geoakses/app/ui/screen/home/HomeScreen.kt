package com.geoakses.app.ui.screen.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.OccupancyStatus
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onRouteClick: (String) -> Unit,
    onStopClick: (String) -> Unit,
    onViewAllRoutes: () -> Unit,
    onViewAllStops: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenFares: () -> Unit,
    onOpenGuide: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val hasLocationPermission = locationPermission.permissions.any { it.status.isGranted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
                    .padding(top = 16.dp)
            ) {
                Column {
                    Text(
                        "Halo, Wargi Bandung",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Mau bepergian ke mana hari ini?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Cari koridor, rute, atau halte...", color = Color.White.copy(alpha = 0.7f)) },
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
        }

        if (uiState.searchQuery.isNotBlank()) {
            val query = uiState.searchQuery.trim()
            val matchingRoutes = uiState.routes.filter {
                it.name.contains(query, true) || it.code.contains(query, true) || it.operator.contains(query, true)
            }.take(3)
            val matchingStops = uiState.stops.filter {
                it.name.contains(query, true) || it.address.contains(query, true) || it.corridorsServed.contains(query, true)
            }.take(3)
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Hasil pencarian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    matchingRoutes.forEach { route ->
                        TextButton(onClick = { onRouteClick(route.id) }) {
                        Text("${route.code} - ${route.name}", maxLines = 1)
                        }
                    }
                    matchingStops.forEach { stop ->
                        TextButton(onClick = { onStopClick(stop.id) }) {
                            Text(stop.name, maxLines = 1)
                        }
                    }
                    if (matchingRoutes.isEmpty() && matchingStops.isEmpty()) {
                        Text("Tidak ada rute atau halte yang cocok.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Quick Actions
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Akses Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionItem(Icons.Filled.DirectionsBus, "Lacak Bus", MaterialTheme.colorScheme.primary, onOpenMap)
                    QuickActionItem(Icons.Filled.Schedule, "Jadwal", MaterialTheme.colorScheme.secondary) { onViewAllRoutes() }
                    QuickActionItem(Icons.Filled.AttachMoney, "Tarif", Color(0xFF2D785C), onOpenFares)
                    QuickActionItem(Icons.Filled.Info, "Panduan", MaterialTheme.colorScheme.tertiary, onOpenGuide)
                }
            }
        }

        // Active Corridors
        item {
            SectionHeader("Koridor Aktif", onViewAllRoutes)
        }
        item {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.routes.take(5)) { route ->
                        RouteCard(route = route, onClick = { onRouteClick(route.id) })
                    }
                }
            }
        }

        // Live Buses
        item { SectionHeader("Bus Sedang Beroperasi") }
        if (uiState.liveBuses.isEmpty()) {
            item { Text("Feed posisi bus belum terhubung; GeoAkses tidak menampilkan lokasi simulasi.", modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(uiState.liveBuses.take(3)) { bus ->
                LiveBusItem(bus = bus, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }

        // Nearby Stops
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Halte Terdekat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(uiState.locationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = {
                    if (!hasLocationPermission) {
                        locationPermission.launchPermissionRequest()
                    } else {
                        try {
                            LocationServices.getFusedLocationProviderClient(context).lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) viewModel.onLocationUpdated(location.latitude, location.longitude)
                                    else viewModel.onLocationUnavailable()
                                }
                                .addOnFailureListener { viewModel.onLocationUnavailable() }
                        } catch (_: SecurityException) {
                            viewModel.onLocationUnavailable()
                        }
                    }
                }) {
                    Text(if (hasLocationPermission) "Perbarui lokasi" else "Gunakan lokasi saya")
                }
            }
        }
        uiState.error?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        items(uiState.nearbyStops.take(4)) { stop ->
            NearbyStopItem(stop = stop, onClick = { onStopClick(stop.id) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
        if (uiState.nearbyStops.isEmpty()) {
            item { Text("Data halte dengan koordinat terverifikasi belum tersedia.", modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onViewAll != null) {
            TextButton(onClick = onViewAll) { Text("Lihat Semua") }
        }
    }
}

@Composable
private fun RouteCard(route: Route, onClick: () -> Unit) {
    val routeColor = try { Color(android.graphics.Color.parseColor(route.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = routeColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(routeColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(route.code, style = MaterialTheme.typography.labelMedium, color = routeColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(route.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(route.operator, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (route.dataStatus == "community" || route.dataStatus == "mixed") {
                    Text(if (route.dataStatus == "community") "Komunitas" else "Sumber campuran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(route.operatingHours, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LiveBusItem(bus: Bus, modifier: Modifier = Modifier) {
    val occupancyColor = when (bus.occupancy) {
        OccupancyStatus.EMPTY, OccupancyStatus.SEATS_AVAILABLE -> Color(0xFF2E7D32)
        OccupancyStatus.STANDING_ONLY -> Color(0xFFF57C00)
        OccupancyStatus.FULL -> Color(0xFFD32F2F)
        OccupancyStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val occupancyText = when (bus.occupancy) {
        OccupancyStatus.EMPTY -> "Kosong"
        OccupancyStatus.SEATS_AVAILABLE -> "Tersedia"
        OccupancyStatus.STANDING_ONLY -> "Padat"
        OccupancyStatus.FULL -> "Penuh"
        OccupancyStatus.UNKNOWN -> "Belum diketahui"
    }
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bus.routeName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Plat: ${bus.plateNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Menuju: ${bus.nextStop}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(bus.etaMinutes?.let { "$it menit" } ?: "ETA —", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(occupancyColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(occupancyText, style = MaterialTheme.typography.labelSmall, color = occupancyColor)
                }
            }
        }
    }
}

@Composable
private fun NearbyStopItem(stop: Stop, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stop.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(stop.corridorsServed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("${(stop.distanceMeters / 1000).let { "%.1f".format(it) }} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
