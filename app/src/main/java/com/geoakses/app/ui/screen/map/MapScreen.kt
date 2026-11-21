package com.geoakses.app.ui.screen.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.geoakses.app.data.model.Bus
import com.geoakses.app.ui.viewmodel.MapViewModel
import com.geoakses.app.util.Constants
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapReference = remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner, mapReference.value) {
        val mapView = mapReference.value
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onPause()
            mapView?.onDetach()
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

                if (uiState.showStops) {
                    uiState.stops.filter { stop ->
                        selectedRoute == null || stop.corridorsServed.contains(selectedRoute.code, ignoreCase = true)
                    }.forEach { stop ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(stop.latitude, stop.longitude)
                            title = stop.name
                            snippet = stop.corridorsServed
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }
                }

                if (uiState.showBuses) {
                    uiState.liveBuses.filter { bus ->
                        selectedRoute == null || bus.routeId == selectedRoute.id
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
                mapView.overlays.add(CopyrightOverlay(mapView.context))
                mapView.invalidate()
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
                    Text("Peta Transportasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.showBuses,
                            onClick = viewModel::toggleBuses,
                            label = { Text("Bus (${uiState.liveBuses.size})") },
                            leadingIcon = { Icon(Icons.Filled.DirectionsBus, null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = uiState.showStops,
                            onClick = viewModel::toggleStops,
                            label = { Text("Halte (${uiState.stops.size})") },
                            leadingIcon = { Icon(Icons.Filled.Place, null, modifier = Modifier.size(16.dp)) }
                        )
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
                        items(uiState.routes) { route ->
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

        // Detail dialog/card for bus
        uiState.selectedBus?.let { bus ->
            BusInfoCard(
                bus = bus,
                onDismiss = { viewModel.selectBus(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
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

