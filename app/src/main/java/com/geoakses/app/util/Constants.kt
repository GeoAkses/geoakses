package com.geoakses.app.util

import com.geoakses.app.data.model.Route
import com.geoakses.app.BuildConfig

object Constants {
    val BASE_URL: String = BuildConfig.API_BASE_URL
    const val NETWORK_TIMEOUT = 10L
    const val DATASTORE_NAME = "geoakses_preferences"
    const val BANDUNG_LATITUDE = -6.917464
    const val BANDUNG_LONGITUDE = 107.619123
    const val DEFAULT_MAP_ZOOM = 13.5

    // Route facts only; stops, exact timetables and vehicle telemetry require separate feeds.
    val SOURCED_ROUTES = listOf(
        route("TMB-K1", "Koridor 1", "Cibiru - Cibeureum", "Cibiru", "Cibeureum", "TMB", 4000, "05:30–16:30 / 05:30–17:30", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jam berbeda menurut arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-K2", "Koridor 2", "Cicaheum - Cibeureum", "Cicaheum", "Cibeureum", "TMB", 4000, "05:30–17:00 / 05:30–17:30", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jam berbeda menurut arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-K3", "Koridor 3", "Cicaheum - Sarijadi", "Cicaheum", "Sarijadi", "TMB", 4000, "05:00–16:00", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jam yang dipublikasikan berlaku pada kedua arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-K4", "Koridor 4", "Simpang Antapani - Leuwipanjang", "Simpang Antapani", "Leuwipanjang", "TMB", 4000, "05:30–15:00 / 06:00–16:30", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jam berbeda menurut arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-K5", "Koridor 5", "Terminal Antapani - Stasiun Hall", "Terminal Antapani", "Stasiun Hall", "TMB", 4000, "05:30–16:00 / 06:30–15:00", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jam berbeda menurut arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-F1", "Feeder 1", "Stasiun Hall - Gunung Batu", "Stasiun Hall", "Gunung Batu", "TMB", 4000, "06:00–15:00 / 06:00–15:30", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Layanan feeder; jam berbeda menurut arah. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("TMB-F2", "Feeder 2", "Summarecon Mall - Cibeureum", "Summarecon Mall", "Cibeureum", "TMB", 4000, "06:30–15:00", "#757575", "official", "https://uptangkutan-bandung.id/news/trans-metro-bandung", "Jadwal arah sebaliknya tercantum sama. Tarif standar; tarif pelajar dan QRIS dapat berbeda."),
        route("MJT-1", "MJT 1", "Leuwipanjang - Soreang", "Leuwipanjang", "Soreang", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Nama koridor dari Dishub Jabar; tarif standar Rp4.900 dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-2", "MJT 2", "Alun-Alun Bandung - Kota Baru Parahyangan", "Alun-Alun Bandung", "Kota Baru Parahyangan", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Nama koridor dari Dishub Jabar; tarif standar Rp4.900 dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-3", "MJT 3", "Baleendah - BEC", "Baleendah", "BEC", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Nama koridor dari Dishub Jabar; tarif standar Rp4.900 dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-4", "MJT 4", "Leuwipanjang - Dago", "Leuwipanjang", "Dago", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Nama koridor dari Dishub Jabar; tarif standar Rp4.900 dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-5", "MJT 5", "Dipatiukur - Jatinangor", "Dipatiukur", "Jatinangor", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Via Tol Moh. Toha; nama koridor dari Dishub Jabar dan tarif dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-6", "MJT 6", "Leuwipanjang - Majalaya", "Leuwipanjang", "Majalaya", "MJT", 4900, "Belum tersedia", "#757575", "mixed", "Dishub Jabar: https://dishub.jabarprov.go.id/postingan/menjelajahi-rute-metro-jabar-trans-6a43738041defbe2d943a4cd; Tarif: https://transportforbandung.org/assets/peta/Peta_FDTB_April_2026.pdf", "Nama koridor dari Dishub Jabar; tarif standar Rp4.900 dari peta Transport for Bandung (April 2026). Jam layanan belum tersedia."),
        route("MJT-FD1", "MJT FD1", "Simpang Soetta Kiaracondong - Pasar Baru ABC", "Simpang Soetta Kiaracondong", "Pasar Baru ABC", "MJT", 0, "Belum tersedia", "#757575", "community", "https://transportforbandung.org/peta", "Rute feeder dipetakan oleh komunitas; operator, tarif, dan jam layanan belum diverifikasi."),
        route("MJT-FD2", "MJT FD2", "Cicadas - Elang", "Cicadas", "Elang", "MJT", 0, "Belum tersedia", "#757575", "community", "https://transportforbandung.org/peta", "Rute feeder dipetakan oleh komunitas; operator, tarif, dan jam layanan belum diverifikasi.")
    )

    private fun route(
        id: String, code: String, name: String, origin: String, destination: String,
        operator: String, fare: Int, hours: String, color: String, status: String,
        source: String, note: String
    ) = Route(
        id = id, code = code, name = name, origin = origin, destination = destination,
        operator = operator, fare = fare, operatingHours = hours, headwayMinutes = 0,
        colorHex = color, stopsCount = 0, isActive = true, description = note,
        dataStatus = status, source = source
    )
}
