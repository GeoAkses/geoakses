package com.geoakses.app.ui.screen.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.geoakses.app.data.model.Bus
import com.geoakses.app.data.model.FacilityCategory
import com.geoakses.app.data.model.PublicFacility
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.viewmodel.MapViewModel
import com.geoakses.app.util.Constants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.lang.SecurityException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onStopClick: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val mapReference = remember { mutableStateOf<MapView?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val hasLocationPermission = locationPermission.permissions.any { it.status.isGranted }

    fun loadDeviceLocation() {
        try {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        locationError = null
                        viewModel.updateUserLocation(location.latitude, location.longitude)
                    } else {
                        locationError = "Lokasi belum tersedia. Pastikan GPS aktif lalu coba lagi."
                    }
                }
                .addOnFailureListener {
                    locationError = "Lokasi perangkat tidak dapat dibaca. Coba lagi."
                }
        } catch (_: SecurityException) {
            locationError = "Izin lokasi belum diberikan."
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) loadDeviceLocation()
    }

    LaunchedEffect(uiState.userLatitude, uiState.userLongitude) {
        val latitude = uiState.userLatitude
        val longitude = uiState.userLongitude
        if (latitude != null && longitude != null) {
            mapReference.value?.controller?.setZoom(16.0)
            mapReference.value?.controller?.setCenter(GeoPoint(latitude, longitude))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapReference.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapReference.value?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(Constants.DEFAULT_MAP_ZOOM)
                    controller.setCenter(GeoPoint(Constants.BANDUNG_LATITUDE, Constants.BANDUNG_LONGITUDE))
                    overlays.add(CopyrightOverlay(ctx))
                }.also { mapReference.value = it }
            },
            update = { mapView ->
                mapView.overlays.clear()
                val selectedRoute = uiState.routes.firstOrNull { it.id == uiState.selectedRouteId }
                val operatorRoutes = uiState.routes.filter { route -> route.operator.equals(uiState.selectedLayer, ignoreCase = true) }

                if (uiState.showStops) {
                    uiState.stops.filter { stop ->
                        val routeMatches = selectedRoute == null || stop.corridorsServed.contains(selectedRoute.code, ignoreCase = true)
                        val operatorMatches = uiState.selectedLayer !in listOf("TMB", "MJT") ||
                            operatorRoutes.any { stop.corridorsServed.contains(it.code, ignoreCase = true) }
                        stop.isActive && stop.latitude in -90.0..90.0 && stop.longitude in -180.0..180.0 && routeMatches && operatorMatches
                    }.forEach { stop ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(stop.latitude, stop.longitude)
                            title = stop.name
                            snippet = stop.corridorsServed
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                viewModel.selectStop(stop)
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }

                if (uiState.showBuses) {
                    uiState.liveBuses.filter { bus ->
                        val routeMatches = selectedRoute == null || bus.routeId == selectedRoute.id
                        val operatorMatches = uiState.selectedLayer !in listOf("TMB", "MJT") ||
                            uiState.routes.any { it.id == bus.routeId && it.operator.equals(uiState.selectedLayer, ignoreCase = true) }
                        bus.isActive && routeMatches && operatorMatches
                    }.forEach { bus ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(bus.latitude, bus.longitude)
                            title = "${bus.routeName} (${bus.plateNumber})"
                            snippet = "Menuju: ${bus.nextStop} | ETA: ${bus.etaMinutes?.let { "$it mnt" } ?: "tidak tersedia"}"
                            rotation = bus.heading
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { _, _ ->
                                viewModel.selectBus(bus)
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }

                if (uiState.showFacilities) {
                    uiState.facilities.filter { facility ->
                        val catMatches = uiState.selectedFacilityCategory == FacilityCategory.ALL ||
                            facility.category.equals(uiState.selectedFacilityCategory, ignoreCase = true)
                        facility.latitude in -90.0..90.0 && facility.longitude in -180.0..180.0 && catMatches
                    }.forEach { facility ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(facility.latitude, facility.longitude)
                            title = "${facility.name} (${facility.category})"
                            snippet = facility.address
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                viewModel.selectFacility(facility)
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }

                val latitude = uiState.userLatitude
                val longitude = uiState.userLongitude
                if (latitude != null && longitude != null) {
                    mapView.overlays.add(Marker(mapView).apply {
                        position = GeoPoint(latitude, longitude)
                        title = "Lokasi Anda"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    })
                }
                mapView.overlays.add(CopyrightOverlay(mapView.context))
                mapView.invalidate()
            },
            onRelease = { mapView ->
                mapView.onPause()
                mapView.onDetach()
                if (mapReference.value === mapView) mapReference.value = null
            },
            modifier = Modifier.fillMaxSize()
        )

        // Filter chips overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                            }
                        }
                        Column {
                            Text("Peta Bandung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Fasilitas umum, halte, dan koridor transportasi publik", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Semua", "Fasilitas", "TMB", "MJT", "Halte", "Bus")) { layer ->
                            FilterChip(
                                selected = uiState.selectedLayer == layer,
                                onClick = { viewModel.selectLayer(layer) },
                                label = { Text(layer) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Kategori Fasilitas Umum", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FacilityCategory.ALL_CATEGORIES) { cat ->
                            FilterChip(
                                selected = uiState.selectedFacilityCategory == cat,
                                onClick = { viewModel.selectFacilityCategory(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Koridor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.selectedRouteId == null,
                                onClick = { viewModel.selectRoute(null) },
                                label = { Text("Semua") }
                            )
                        }
                        items(uiState.routes.filter { uiState.selectedLayer !in listOf("TMB", "MJT") || it.operator.equals(uiState.selectedLayer, true) }) { route ->
                            FilterChip(
                                selected = uiState.selectedRouteId == route.id,
                                onClick = { viewModel.selectRoute(route.id) },
                                label = { Text(route.code) }
                            )
                        }
                    }
                }
            }
        }

        locationError?.let { message ->
            Text(
                message,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = 20.dp, end = 20.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(
            onClick = {
                if (hasLocationPermission) loadDeviceLocation() else locationPermission.launchMultiplePermissionRequest()
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(52.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Ke lokasi saya", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        uiState.selectedStop?.let { stop ->
            StopInfoCard(
                stop = stop,
                onDismiss = { viewModel.selectStop(null) },
                onDetails = { onStopClick(stop.id) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
            )
        }
        uiState.selectedBus?.let { bus ->
            BusInfoCard(
                bus = bus,
                onDismiss = { viewModel.selectBus(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
        uiState.selectedFacility?.let { facility ->
            val nearestStop = remember(facility, uiState.stops) {
                uiState.stops
                    .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
                    .minByOrNull { stop ->
                        val rad = Math.PI / 180.0
                        val dLat = (stop.latitude - facility.latitude) * rad
                        val dLon = (stop.longitude - facility.longitude) * rad
                        val a = kotlin.math.sin(dLat / 2).let { it * it } +
                            kotlin.math.cos(facility.latitude * rad) * kotlin.math.cos(stop.latitude * rad) *
                            kotlin.math.sin(dLon / 2).let { it * it }
                        6_371_000.0 * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
                    }
            }
            FacilityInfoCard(
                facility = facility,
                nearestStop = nearestStop,
                onDismiss = { viewModel.selectFacility(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
            )
        }
    }
}

@Composable
private fun FacilityInfoCard(
    facility: PublicFacility,
    nearestStop: Stop?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facility.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(facility.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup")
                }
            }
            if (facility.address.isNotBlank()) {
                Text(facility.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Jam Operasional: ${facility.operatingHours}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (facility.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(facility.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Rekomendasi Transportasi Publik", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            if (facility.accessibilityInfo.isNotBlank()) {
                Text(facility.accessibilityInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
            if (nearestStop != null) {
                Text("• Halte Terdekat: ${nearestStop.name}", style = MaterialTheme.typography.bodySmall)
                if (nearestStop.corridorsServed.isNotBlank()) {
                    Text("• Koridor Melintas: ${nearestStop.corridorsServed}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            } else if (facility.accessibilityInfo.isBlank()) {
                Text("Gunakan koridor TMB / MJT terdekat menuju kawasan ${facility.address.ifBlank { facility.name }}.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BusInfoCard(
    bus: Bus,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(bus.routeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup")
                }
            }
            Text("Nomor Plat: ${bus.plateNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Halte Saat Ini: ${bus.currentStop}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Halte Berikutnya: ${bus.nextStop}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("ETA: ${bus.etaMinutes?.let { "$it menit" } ?: "tidak tersedia"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Kecepatan: ${bus.speedKmH.toInt()} km/jam", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun StopInfoCard(
    stop: Stop,
    onDismiss: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(stop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup")
                }
            }
            if (stop.address.isNotBlank()) {
                Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (stop.corridorsServed.isNotBlank()) {
                Text("Koridor ${stop.corridorsServed}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = onDetails) { Text("Detail halte") }
        }
    }
}