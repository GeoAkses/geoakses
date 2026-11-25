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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.FacilityCategory
import com.geoakses.app.data.model.OccupancyStatus
import com.geoakses.app.data.model.PublicFacility
import com.geoakses.app.data.model.Route
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.component.GeoAksesLoadingState
import com.geoakses.app.ui.component.GeoAksesSearchBar
import com.geoakses.app.ui.component.GeoAksesSectionHeader
import com.geoakses.app.ui.theme.SemanticErrorContent
import com.geoakses.app.ui.theme.SemanticSuccess
import com.geoakses.app.ui.theme.SemanticWarningContent
import com.geoakses.app.ui.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onRouteClick: (String) -> Unit,
    onStopClick: (String) -> Unit,
    onViewAllRoutes: () -> Unit,
    onViewAllStops: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenFares: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val hasLocationPermission = locationPermission.permissions.any { it.status.isGranted }

    val filteredFacilities = remember(uiState.facilities, uiState.selectedFacilityCategory) {
        if (uiState.selectedFacilityCategory == FacilityCategory.ALL) {
            uiState.facilities
        } else {
            uiState.facilities.filter {
                it.category.equals(uiState.selectedFacilityCategory, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Brand header and global search
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DirectionsTransit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("GeoAkses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Bandung Raya", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                GeoAksesSearchBar(
                    value = "",
                    onValueChange = {},
                    placeholder = "Cari fasilitas umum, halte, atau rute...",
                    readOnly = true,
                    onClick = onOpenSearch
                )
            }
        }

        item { HomeHeroCard(onExplore = onOpenMap) }

        // Quick Actions
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Akses Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickActionItem(Icons.Filled.Map, "Peta", MaterialTheme.colorScheme.primary, onOpenMap, Modifier.weight(1f))
                        QuickActionItem(Icons.Filled.DirectionsBus, "Rute", MaterialTheme.colorScheme.primary, onViewAllRoutes, Modifier.weight(1f))
                        QuickActionItem(Icons.Filled.Place, "Halte", MaterialTheme.colorScheme.secondary, onViewAllStops, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickActionItem(Icons.Filled.AttachMoney, "Tarif", SemanticSuccess, onOpenFares, Modifier.weight(1f))
                        QuickActionItem(Icons.Filled.Schedule, "Jadwal", MaterialTheme.colorScheme.secondary, onViewAllRoutes, Modifier.weight(1f))
                        QuickActionItem(Icons.Filled.Settings, "Pengaturan", MaterialTheme.colorScheme.onSurfaceVariant, onOpenProfile, Modifier.weight(1f))
                    }
                }
            }
        }

        // Fasilitas Umum Bandung
        item {
            GeoAksesSectionHeader(
                title = "Fasilitas Umum Bandung",
                actionLabel = "Lihat di Peta",
                onAction = onOpenMap
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FacilityCategory.ALL_CATEGORIES) { category ->
                    FilterChip(
                        selected = uiState.selectedFacilityCategory == category,
                        onClick = { viewModel.selectFacilityCategory(category) },
                        label = { Text(category) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (filteredFacilities.isEmpty()) {
            item {
                Text(
                    text = if (uiState.facilities.isEmpty()) "Memuat data fasilitas umum Bandung..." else "Belum ada fasilitas pada kategori ${uiState.selectedFacilityCategory}.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(filteredFacilities.take(5)) { facility ->
                PublicFacilityItem(
                    facility = facility,
                    onFindRoute = onOpenMap,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Active Corridors
        item {
            GeoAksesSectionHeader("Koridor Aktif", actionLabel = "Lihat Semua", onAction = onViewAllRoutes)
        }
        item {
            if (uiState.isLoading) {
                GeoAksesLoadingState(modifier = Modifier.height(120.dp))
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
        item { GeoAksesSectionHeader("Bus Sedang Beroperasi") }
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
                        locationPermission.launchMultiplePermissionRequest()
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
            val message = when {
                uiState.latitude == null && !hasLocationPermission -> "Aktifkan lokasi untuk melihat halte terdekat, atau cari halte secara manual."
                uiState.latitude == null -> "Tekan ‘Perbarui lokasi’ untuk mencari halte di sekitar Anda."
                uiState.stops.isEmpty() -> "Data halte dengan koordinat terverifikasi belum tersedia."
                else -> "Tidak ada halte terverifikasi dalam jangkauan pencarian."
            }
            item {
                Text(
                    message,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PublicFacilityItem(
    facility: PublicFacility,
    onFindRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = facility.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = facility.operatingHours,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(facility.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (facility.address.isNotBlank()) {
                Text(facility.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (facility.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(facility.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (facility.accessibilityInfo.isNotBlank()) facility.accessibilityInfo else "Lihat titik & rute di peta",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onFindRoute,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cari Rute", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .heightIn(min = 88.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp)
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
private fun HomeHeroCard(onExplore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Transportasi Bandung Lebih Mudah", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Temukan fasilitas umum dan rekomendasi transportasi publik di Kota Bandung.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onExplore) {
                    Text("Jelajahi peta", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(
                Icons.Filled.DirectionsTransit,
                contentDescription = "Transportasi publik",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
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
        OccupancyStatus.EMPTY, OccupancyStatus.SEATS_AVAILABLE -> SemanticSuccess
        OccupancyStatus.STANDING_ONLY -> SemanticWarningContent
        OccupancyStatus.FULL -> SemanticErrorContent
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