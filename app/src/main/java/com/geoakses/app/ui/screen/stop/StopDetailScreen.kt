package com.geoakses.app.ui.screen.stop

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.ui.viewmodel.StopViewModel
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.data.model.FavoriteKind
import com.geoakses.app.ui.viewmodel.FavoritesViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun StopDetailScreen(
    stopId: String,
    onBack: () -> Unit,
    viewModel: StopViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailState.collectAsState()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()
    val favoriteItems by favoritesViewModel.favorites.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(stopId) {
        viewModel.loadStopDetail(stopId)
    }

    Scaffold(
        topBar = {
            GeoAksesTopBar(
                title = uiState.stop?.name ?: "Detail Halte",
                onBack = onBack,
                actions = {
                    uiState.stop?.let { currentStop ->
                        val isFavorite = favoriteItems.any {
                            it.itemId == currentStop.id && it.kind == FavoriteKind.STOP
                        }
                        IconButton(onClick = {
                            favoritesViewModel.toggle(
                                currentStop.id,
                                FavoriteKind.STOP,
                                currentStop.name,
                                currentStop.address
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

        val stop = uiState.stop ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stop.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                if (stop.address.isNotBlank()) {
                                    Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (stop.dataStatus == "demo") {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Data titik dan koridor merupakan contoh; cek kondisi layanan sebelum berangkat.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text("Koridor yang melintas:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        stop.corridorsServed.split(",").forEach { corridor ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsBus, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(corridor.trim(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Store, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(if (stop.isShelter) "Shelter Resmi (Dengan Atap/Tempat Duduk)" else "Titik Pemberhentian Reguler", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                val validCoordinates = stop.latitude in -90.0..90.0 && stop.longitude in -180.0..180.0 && stop.dataStatus != "demo"
                if (validCoordinates) {
                    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Map, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Lokasi Halte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${stop.latitude}, ${stop.longitude}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            StopMiniMap(stop = stop)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val uri = Uri.parse("geo:${stop.latitude},${stop.longitude}?q=${stop.latitude},${stop.longitude}(${Uri.encode(stop.name)})")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                                    else Toast.makeText(context, "Aplikasi peta tidak tersedia.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Navigasi ke halte")
                            }
                        }
                    }
                } else {
                    GeoAksesEmptyState(
                        title = "Peta halte belum tersedia",
                        description = "Koordinat lokasi ini belum diverifikasi, jadi navigasi tidak ditampilkan."
                    )
                }
            }
        }
    }
}

@Composable
private fun StopMiniMap(stop: com.geoakses.app.data.model.Stop) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapReference = remember { mutableStateOf<MapView?>(null) }
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
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(stop.latitude, stop.longitude))
                overlays.add(CopyrightOverlay(context))
                overlays.add(Marker(this).apply {
                    position = GeoPoint(stop.latitude, stop.longitude)
                    title = stop.name
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                })
            }.also { mapReference.value = it }
        },
        modifier = Modifier.fillMaxWidth().height(176.dp),
        update = { it.invalidate() },
        onRelease = { mapView ->
            mapView.onPause()
            mapView.onDetach()
            if (mapReference.value === mapView) mapReference.value = null
        }
    )
}

