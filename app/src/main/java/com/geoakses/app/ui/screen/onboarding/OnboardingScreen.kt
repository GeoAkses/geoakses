package com.geoakses.app.ui.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            Icons.Filled.DirectionsBus,
            "Jelajahi Angkutan Bandung",
            "Cari koridor TMB, Metro Jabar Trans, dan feeder yang datanya tersedia."
        ),
        OnboardingPage(
            Icons.Filled.Map,
            "Lacak Posisi Bus Langsung",
            "Lihat halte dan armada pada peta OpenStreetMap. Posisi live ditampilkan saat feed tersedia."
        ),
        OnboardingPage(
            Icons.Filled.Place,
            "Temukan Halte Terdekat",
            "Simpan rute, halte, dan jadwal agar tetap bisa dibuka saat jaringan lemah."
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "onboarding_page"
        ) { page ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = pages[page].icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    text = pages[page].title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = pages[page].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.weight(1f))
        // Page indicators
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .background(
                            if (index == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage < pages.size - 1) {
                TextButton(onClick = onFinish) {
                    Text("Lewati")
                }
                Button(onClick = { currentPage++ }) {
                    Text("Lanjut")
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mulai Sekarang")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

