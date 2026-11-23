package com.geoakses.app.ui.screen.stop

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geoakses.app.data.model.Stop
import com.geoakses.app.ui.component.GeoAksesEmptyState
import com.geoakses.app.ui.component.GeoAksesSearchBar
import com.geoakses.app.ui.component.GeoAksesTopBar
import com.geoakses.app.ui.viewmodel.StopViewModel

@Composable
fun StopListScreen(
    onStopClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: StopViewModel = hiltViewModel()
) {
    val uiState by viewModel.listState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GeoAksesTopBar(title = "Daftar Halte", onBack = onBack)
        GeoAksesSearchBar(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = "Cari nama halte atau koridor...",
            modifier = Modifier.padding(horizontal = 16.dp),
            onClear = { viewModel.onSearchQueryChange("") }
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredStops.isEmpty()) {
            GeoAksesEmptyState(
                title = if (uiState.searchQuery.isBlank()) "Data halte belum tersedia" else "Halte tidak ditemukan",
                description = if (uiState.searchQuery.isBlank()) "Data halte aktif dengan koordinat terverifikasi belum tersedia." else "Coba nama halte atau koridor lain.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredStops) { stop ->
                    StopListItem(stop = stop, onClick = { onStopClick(stop.id) })
                }
            }
        }
    }
}

@Composable
fun StopListItem(stop: Stop, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stop.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                if (stop.address.isNotBlank()) {
                    Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Text("Koridor: ${stop.corridorsServed}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (stop.dataStatus == "demo") {
                    Text("Contoh data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

