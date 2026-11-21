# GeoAkses — Master Product Requirements Document

| Metadata | Nilai |
|---|---|
| Produk | GeoAkses — agregator informasi transportasi publik Jawa Barat |
| Versi dokumen | 1.0.0 |
| Status | Draft baseline untuk review dan implementasi bertahap |
| Pemilik | Tim GeoAkses |
| Wilayah awal | Bandung Raya dan koridor Metro Jabar Trans yang terverifikasi |
| Bahasa produk | Bahasa Indonesia; waktu ditampilkan dalam WIB (Asia/Jakarta) |
| Sumber kebenaran | Dokumen ini adalah baseline kebutuhan lintas-repo; perubahan produk dan kontrak wajib diperbarui di sini lebih dahulu |
| Tanggal | 21 November 2026 |

> **Prinsip Spec-Driven Development:** PRD menetapkan kebutuhan dan kriteria penerimaan. Kontrak API terperinci diturunkan menjadi OpenAPI di `apidocs`; setiap repo mengimplementasikan kontrak yang sama. Perubahan skema/endpoint tidak boleh hanya dilakukan di satu repo.

## 1. Ringkasan Eksekutif

GeoAkses adalah aplikasi transportasi publik cerdas untuk membantu warga Bandung Raya dan Jawa Barat merencanakan perjalanan menggunakan Trans Metro Bandung (TMB), Metro Jabar Trans (MJT/Teman Bus), serta angkutan pengumpan seperti Bemo/Angkot. Produk menyatukan peta, koridor, halte/shelter, jadwal, dan—jika operator menyediakan sumber data yang layak—posisi armada dan estimasi kedatangan (ETA).

Masalah yang diselesaikan ialah ketidakpastian lokasi dan kedatangan bus, informasi first-mile/last-mile yang tersebar, serta hilangnya akses informasi dasar saat jaringan lemah. Aplikasi Android dirancang offline-first: data rute, halte, dan jadwal yang berhasil disinkronkan tetap dapat dibaca dari Room. Data live tetap memiliki penanda waktu dan status kesegaran agar tidak disalahartikan sebagai posisi terkini.

Produk terdiri dari empat proyek: `geoakses` untuk aplikasi Android Kotlin; `backend` untuk REST API Go dengan gocroot dan MongoDB; `apidocs` untuk kontrak OpenAPI 3.0 dan Swagger UI; serta `geoakses.github.io` untuk landing page, panduan, tautan dokumentasi, dan unduhan rilis. Backend dan situs mendukung aplikasi, tetapi aplikasi tetap menyajikan data tersimpan saat jaringan/API gagal.

### 1.1 Latar belakang dan batas akurasi

- Informasi transit utama TMB dan MJT/Teman Bus perlu dapat ditemukan dalam satu katalog lintas operator.
- Bemo/Angkot menjadi koneksi feeder first-mile/last-mile. Rute feeder dimasukkan hanya jika ada data sumber yang dapat dipertanggungjawabkan; fase MVP dapat menampilkan katalog/tautan informasi tanpa mengklaim jadwal atau pelacakan live.
- GPS live, okupansi, dan ETA bergantung pada telemetry operator atau sumber data yang sah. Sampai integrasi tersebut tersedia, data simulasi/seed hanya untuk demo dan harus diberi label **simulasi**, bukan informasi operasional.
- Jadwal adalah jadwal rencana; waktu aktual/ETA merupakan data dinamis. UI dan API wajib membedakan keduanya.

## 2. Visi, Sasaran, dan Bukan Sasaran

### 2.1 Visi

Menjadi asisten mobilitas multimoda yang tepercaya dan tahan kondisi jaringan tidak stabil, dengan informasi yang jelas tentang asal, waktu pembaruan, serta tingkat kesegaran datanya.

### 2.2 Sasaran produk

1. Menemukan koridor TMB/MJT dan halte/shelter terdekat dengan cepat.
2. Menampilkan posisi armada, ETA, dan okupansi bila tersedia dari sumber operasional yang tervalidasi.
3. Memungkinkan pengguna mengakses rute, halte, dan jadwal terakhir yang tersinkronisasi saat offline.
4. Menyediakan kontrak API lintas aplikasi dan situs yang terdokumentasi dan berversi.
5. Mengirimkan APK dan AAB melalui CI/CD yang dapat diulang dan dilacak.

### 2.3 Bukan sasaran MVP

- Pembelian tiket, pembayaran, top-up, reservasi kursi, atau integrasi tarif lintas operator.
- Navigasi turn-by-turn, perhitungan rute multimoda optimal, atau jaminan koneksi antararmada.
- Klaim bahwa semua armada memiliki GPS/telemetry langsung.
- Pengumpulan lokasi latar belakang pengguna. Lokasi perangkat hanya digunakan untuk fitur sekitar setelah izin diberikan.
- Menjamin bahwa jadwal dan cakupan Bemo/Angkot tersedia merata sebelum dataset diverifikasi.

## 3. Pengguna dan Kebutuhan

| Persona | Kebutuhan utama |
|---|---|
| Komuter harian | Menemukan koridor, halte, jadwal, dan status armada tanpa menebak-nebak |
| Penumpang sesekali/pengunjung | Memahami operator, arah perjalanan, halte, dan cara mengunduh aplikasi |
| Pengguna dengan jaringan terbatas | Membuka data rute/halte/jadwal tersimpan dan mengetahui kapan data terakhir diperbarui |
| Pengelola data/operator | Memublikasikan data yang konsisten, terverifikasi, dan dapat diperbarui |
| Pengembang/maintainer | Mengubah kontrak dengan terkoordinasi dan merilis aplikasi yang dapat direproduksi |

## 4. Goals dan Success Metrics

Target berikut adalah sasaran produk, bukan klaim kondisi saat ini. Baseline harus direkam sebelum rilis MVP. Pengukuran performa dilakukan pada perangkat dan kondisi uji yang didokumentasikan.

| Metrik | Target MVP | Definisi pengukuran |
|---|---:|---|
| Respons interaksi peta dan query data lokal | p95 < 200 ms | Dari input/permintaan sampai hasil cache lokal siap dirender; diukur pada perangkat uji kelas menengah dengan dataset MVP |
| API baca katalog dan live | p95 < 200 ms | Waktu server dari penerimaan sampai respons untuk endpoint baca, pada beban uji dan ukuran payload yang ditetapkan; tidak termasuk RTT internet/perangkat pihak ketiga |
| Ketersediaan offline katalog | 100% dari data berhasil disinkronkan | Rute, halte, serta jadwal tersimpan dapat dibaca tanpa jaringan melalui Room; bukan jaminan bahwa data yang belum pernah diunduh tersedia |
| Perilaku ketika API tidak tersedia | Tidak kehilangan data cache | UI menampilkan cache terakhir, status offline/error, dan waktu sinkronisasi terakhir tanpa memblokir navigasi |
| CI/CD Auto-Tag + APK + AAB | < 10 menit | Durasi dari workflow release mulai sampai artefak build tersedia, pada runner/cache dan kondisi yang dicatat; kegagalan dependency eksternal dicatat terpisah |
| Keutuhan kontrak lintas repo | Semua endpoint MVP sesuai OpenAPI | Contract checks atau pemeriksaan manual terjadwal memastikan schema, status code, dan contoh response konsisten |
| Kualitas data katalog | 100% rekaman aktif memiliki field wajib valid | Pemeriksaan koordinat, operator, ID stabil, jam operasional, serta sumber dan waktu verifikasi |

## 5. Ruang Lingkup Fitur dan Prioritas MoSCoW

| ID | Kebutuhan / User Story | Prioritas | Kriteria penerimaan ringkas |
|---|---|---|---|
| US-01 | Sebagai komuter, saya melihat peta OpenStreetMap interaktif melalui OSMDroid dengan penanda koridor dan halte. | Must | Peta dapat digeser/zoom; marker memiliki label/detail; kegagalan tile tidak menutup akses daftar informasi. |
| US-02 | Saya melihat bus live beserta koordinat lat/lon, kecepatan, heading, okupansi, dan ETA jika sumber tersedia. | Must | Data memiliki `observed_at`, identitas sumber, dan status kesegaran; data simulasi ditandai jelas; data kedaluwarsa tidak ditampilkan sebagai live. |
| US-03 | Saya menemukan halte dan shelter terdekat dari lokasi saya atau titik peta. | Must | Izin lokasi opsional; jika ditolak, pencarian tetap dapat memakai peta/daftar; jarak disajikan dengan satuan yang jelas. |
| US-04 | Saya mencari koridor dan memfilter operator TMB atau MJT. | Must | Filter operator dapat dikombinasikan dengan pencarian nama/kode; state kosong dan error ditangani. |
| US-05 | Saya melihat jadwal keberangkatan meskipun offline setelah data tersinkronisasi. | Must | Jadwal dibaca dari Room tanpa jaringan, dibedakan dari ETA live, menampilkan hari layanan dan waktu WIB serta waktu sinkronisasi. |
| US-06 | Saya dapat melihat data terakhir saat offline atau API timeout. | Must | Cache tetap terlihat dengan indikator offline/stale; kegagalan sinkronisasi tidak menghapus data valid. |
| US-07 | Saya melihat halte dan rute yang melayani halte tersebut. | Must | Detail halte menunjukkan koordinat, alamat bila ada, aksesibilitas bila datanya tersedia, dan daftar koridor. |
| US-08 | Saya mendapat informasi feeder Bemo/Angkot sebagai koneksi perjalanan. | Should | Katalog feeder hanya menampilkan data terverifikasi dan cakupan/validitasnya; tidak menjanjikan live tracking tanpa sumber. |
| US-09 | Saya mengunduh APK terbaru dan membaca panduan/dokumentasi API dari landing page. | Should | Tombol unduh diarahkan ke APK pada rilis valid terbaru; jika tidak ada rilis, tampilkan status dan tautan release list. |
| US-10 | Saya mengganti tema dan mengatur preferensi dasar. | Could | Preferensi tersimpan lokal; tidak memengaruhi kemampuan inti transit. |
| US-11 | Pengguna menerima notifikasi gangguan/perubahan layanan. | Won't (MVP) | Ditunda sampai tersedia sumber data, proses moderasi, dan kebijakan notifikasi yang disepakati. |

## 6. Alur Pengguna Utama

1. Pengguna membuka aplikasi; data katalog tersimpan langsung ditampilkan bila tersedia.
2. Pengguna melihat peta, mencari koridor, atau mencari halte; lokasi perangkat diminta hanya saat fitur sekitar dipilih.
3. Aplikasi mengambil pembaruan API di latar depan/pekerjaan sinkronisasi terukur, memvalidasi response, lalu memperbarui Room secara atomik.
4. Jika jaringan gagal, aplikasi mempertahankan data cache dan memperlihatkan status serta waktu pembaruannya.
5. Pengguna membuka live bus; aplikasi menampilkan hanya telemetry dengan waktu observasi dan label freshness yang sesuai. Tanpa feed valid, tampilkan status “data live tidak tersedia”.

## 7. Arsitektur Lintas-Repo

### 7.1 Komponen dan tanggung jawab

| Repo | Tanggung jawab | Teknologi/status yang teramati |
|---|---|---|
| `geoakses` | Android client, UI, cache offline, konsumsi REST dan peta | Native Kotlin, Compose/Material 3, MVVM/ViewModel, Room, Retrofit/OkHttp, Hilt, OSMDroid; konfigurasi Gradle menyebut min SDK 24 dan target SDK 35 |
| `backend` | REST API, validasi, akses MongoDB, publikasi data transit | Target Go, gocroot, MongoDB; saat audit berisi README placeholder, belum tampak source server atau skema yang dapat dijadikan kontrak aktual |
| `apidocs` | OpenAPI dan Swagger UI publik | Target OpenAPI 3.0 dan GitHub Pages; saat audit hanya berisi README placeholder, belum ada spesifikasi endpoint |
| `geoakses.github.io` | Landing page, panduan, tautan dokumentasi dan rilis APK | Target vanilla JS/croot.js dan GitHub Releases API; saat audit hanya berisi README placeholder |
| Workflow pada root workspace | CI, auto-tag, release APK/AAB untuk Android | Terdapat `.github/workflows/ci.yml`, `auto-tag.yml`, dan `release-apk-aab.yml`; workflow membangun proyek di subfolder `geoakses` |

### 7.2 Aliran data

`Sumber operator/kurasi data → backend Go → MongoDB → REST JSON /api/v1 → Retrofit Android → validasi dan cache Room → UI Compose/OSMDroid`.

OpenAPI di `apidocs` menjadi kontrak mesin dan dokumentasi bagi backend serta Android. Landing page mengonsumsi informasi rilis GitHub untuk tautan APK; ia tidak menjadi sumber data transit. Data sumber harus menyertakan provenance, status verifikasi, dan timestamp. Seluruh endpoint publik hanya melalui HTTPS di deployment produksi.

### 7.3 Prinsip sinkronisasi Android

- Room adalah sumber baca utama untuk katalog offline; UI dapat menampilkan data tersimpan segera sebelum refresh jaringan.
- Respons jaringan divalidasi dan ditulis ke database dalam transaksi; response parsial/gagal tidak boleh menghapus cache yang masih valid.
- `Schedules` menjadi entitas tersimpan bersama `Routes` dan `Stops`; versi skema Room dinaikkan dengan migrasi yang diuji sebelum rilis.
- Telemetry live berumur pendek dan tidak diklaim tersedia offline sebagai live. Jika disimpan lokal untuk transisi UI, tampilkan umur data dan batas retensinya.
- Cache tile peta dikelola terpisah dari Room dengan batas kapasitas/retensi dan kebijakan provider tile. Tile yang tersimpan bukan pengganti lisensi, atribusi, atau ketersediaan jaringan.

## 8. Kontrak REST API dan Model Data

### 8.1 Konvensi API

- Base path: `/api/v1`; tipe konten JSON UTF-8; waktu API berupa RFC 3339 UTC dengan zona eksplisit, sementara UI mengonversi ke WIB.
- ID rute/halte/bus/jadwal stabil dan tidak bergantung pada nama yang dapat berubah.
- Daftar menggunakan pagination (`limit`, `cursor` atau standar yang ditetapkan OpenAPI), filter tervalidasi, dan urutan yang deterministik.
- Response sukses dan error, status HTTP, format koordinat, nullable fields, freshness, dan contoh wajib didefinisikan secara lengkap di `apidocs/openapi.yaml` sebelum implementasi server.
- Kontrak tidak menyiratkan data operator resmi bila belum ada integrasi atau izin; provenance `source`, `verified_at`, `observed_at`, serta `data_status` disertakan pada konteks yang relevan.

### 8.2 Endpoint MVP

| Method & path | Tujuan | Parameter utama |
|---|---|---|
| `GET /api/v1/routes` | Daftar koridor aktif | `operator`, `q`, `active`, pagination |
| `GET /api/v1/routes/{id}` | Detail koridor, arah, halte terurut, dan metadata | ID rute |
| `GET /api/v1/stops` | Daftar halte/shelter | `lat`, `lon`, `radius_m`, `route_id`, `q`, pagination |
| `GET /api/v1/stops/{id}` | Detail halte dan koridor yang melayani | ID halte |
| `GET /api/v1/buses/live` | Posisi/status kendaraan yang masih segar | `route_id`, `since`/pagination bila diperlukan |
| `GET /api/v1/schedules` | Jadwal rencana | `route_id` wajib; opsional `stop_id`, `service_date`, `day_type` |

Endpoint di atas adalah target kontrak; root/path saat ini belum diimplementasikan pada backend. Kontrak final, termasuk apakah detail halte dipisah atau digabungkan, dikunci dalam OpenAPI sebelum tahap backend.

### 8.3 MongoDB collections

Dokumen MongoDB menggunakan `_id` stabil (string atau ObjectId yang secara konsisten dipetakan ke `id` API), `created_at`, `updated_at`, dan metadata sumber bila relevan. Koordinat disimpan sebagai GeoJSON `location: { type: "Point", coordinates: [longitude, latitude] }` untuk indeks `2dsphere`; API mobile boleh mengekspos `latitude` dan `longitude` terpisah. Koordinat harus divalidasi terhadap rentang bumi.

**`Routes`** — `code`, `name`, `operator` (enum awal TMB/MJT/FEEDER), `origin`, `destination`, `fare` (nullable jika belum terverifikasi), `currency`, `operating_hours`, `headway_minutes` (nullable), `color_hex`, `stops`/urutan relasi, `is_active`, `service_area`, `source`, `verified_at`.

**`Stops`** — `name`, `location`, `address`, `stop_type` (halte/shelter/terminal/feeder point), `is_shelter`, `route_ids`, `accessibility` (nullable/berdasarkan data), `is_active`, `source`, `verified_at`.

**`Buses`** — `vehicle_id`, `plate_number` (akses terbatas bila diperlukan; hindari publikasi identifier sensitif), `route_id`, `location`, `speed_kmh`, `heading_degrees` (0–359), `occupancy` enum (`unknown`, `empty`, `seats_available`, `standing_only`, `full`), `current_stop_id`, `next_stop_id`, `eta_minutes` nullable, `observed_at`, `received_at`, `source`, `data_status`. Telemetry disimpan sebagai snapshot terkini atau koleksi time-series sesuai volume/retensi yang dipilih pada desain backend; PRD tidak mewajibkan retensi histori tanpa use case.

**`Schedules`** — `route_id`, `stop_id`, `stop_name_snapshot` opsional, `departure_time_local` (format dan zona ditetapkan OpenAPI; WIB untuk wilayah awal), `day_type`/hari layanan, rentang tanggal efektif, `is_active`, `source`, `verified_at`. Jadwal rencana tidak boleh diberi label realtime.

### 8.4 Indeks, validasi, dan error

- Unique index pada kode koridor dan kombinasi kunci jadwal yang disepakati; indeks `2dsphere` pada lokasi halte dan telemetry jika query spasial diperlukan; indeks pada `route_id`, `is_active`, dan timestamp telemetry.
- Batas radius/pagination, validasi koordinat, ID, enum, dan input teks ditetapkan dalam kontrak. Query terdekat mengembalikan jarak dalam meter dengan pengurutan deterministik.
- Error menggunakan bentuk konsisten (`code`, `message`, `request_id`, `details` opsional); gunakan status 400/404/429/5xx dengan semantik yang didefinisikan di OpenAPI.
- Jangan mengirim stack trace, kredensial, atau data pribadi di response. Endpoint live mengembalikan waktu observasi agar client dapat menerapkan kebijakan freshness.

## 9. Kebutuhan Data dan Tata Kelola

- Sumber data, pemilik, lisensi/izin penggunaan, cara pembaruan, cakupan wilayah, dan tingkat verifikasi harus dicatat sebelum dataset dipromosikan ke produksi.
- Seed/demo/mock dipisahkan dari data produksi dan diberi penanda eksplisit. Nama atau koordinat contoh tidak otomatis dianggap data operasional resmi.
- Tarif, jam operasi, dan jadwal dapat berubah; tampilkan tanggal verifikasi atau keterangan terakhir diperbarui.
- Koreksi data membutuhkan provenance, audit perubahan minimum (aktor/waktu), dan prosedur rollback.
- Jangan menyimpan lokasi pengguna di backend untuk pencarian halte terdekat kecuali kebutuhan baru disetujui dan kebijakan privasi ditetapkan; kalkulasi dapat dilakukan di perangkat atau request stateless.

## 10. Non-Functional Requirements

### 10.1 Kinerja dan kapasitas

- Penuhi target p95 pada tabel KPI untuk beban, ukuran dataset, jaringan, dan perangkat yang terdokumentasi.
- Query lokal harus menggunakan indeks Room yang sesuai dan tidak memblokir main thread. Peta hanya memuat marker/fitur yang relevan pada viewport atau hasil filter.
- Live polling tidak lebih cepat dari kebutuhan produk; interval awal 10–30 detik dapat dievaluasi terhadap freshness feed, kapasitas API, baterai, dan kebijakan operator. Backoff diterapkan saat error.

### 10.2 Ketahanan jaringan dan edge cases

| Kondisi | Perilaku yang diwajibkan |
|---|---|
| Perangkat offline | Sajikan rute, halte, dan jadwal yang tersimpan; label status offline serta waktu pembaruan terakhir. |
| API timeout/5xx | Batalkan sesuai timeout, pertahankan cache, tampilkan pesan dapat dipahami dan aksi coba lagi; jangan memblokir seluruh layar. |
| Response kosong/parsial/skema tidak cocok | Validasi sebelum commit; jangan mengganti cache baik dengan data invalid; catat error tanpa payload sensitif. |
| Posisi bus basi atau feed terhenti | Tandai stale/tidak tersedia berdasarkan ambang freshness dari OpenAPI/config; jangan menyebutnya live. |
| Izin lokasi ditolak atau GPS mati | Fitur peta/katalog tetap bekerja; pencarian berdasarkan lokasi peta atau query manual tersedia. |
| Tile OSM tidak tersedia | Pertahankan kontrol peta, marker/konten yang bisa dirender, dan fallback pesan; jangan mengunci direktori rute/halte pada tile. |
| Jadwal lintas tengah malam/perubahan zona | Gunakan hari layanan dan zona eksplisit; uji pergantian tanggal serta jadwal yang melewati tengah malam. |
| Sinkronisasi berulang/duplikat | Gunakan ID stabil dan upsert idempoten; sinkronisasi tidak menggandakan rekaman. |
| Cache lokal penuh/versi DB berubah | Batasi cache; migrasi database aman; kegagalan migrasi tidak diam-diam menghilangkan data tanpa jalur pemulihan yang dirancang. |
| Filter tanpa hasil | Tampilkan state kosong dengan opsi menghapus filter, bukan error jaringan. |

### 10.3 Peta dan OpenStreetMap

- Gunakan OSMDroid dan tile OpenStreetMap dengan atribusi yang terlihat serta mematuhi kebijakan tile provider yang digunakan.
- Jangan melakukan bulk download/prefetch luas dari tile server publik. Offline map region hanya boleh memakai provider/hosting dan mekanisme yang mengizinkan unduhan tersebut.
- Tetapkan batas ukuran cache, eviction, kualitas/zoom maksimum, dan kontrol unduhan bila provider mengizinkan; hormati header/cache policy server.
- Peta memerlukan fallback saat tile kosong; marker halte dan daftar informasi tetap tersedia dari Room.

### 10.4 Keamanan, privasi, dan aksesibilitas

- HTTPS wajib untuk produksi; rahasia backend dan signing key disimpan di secret manager/GitHub Secrets, bukan repo.
- Hindari logging body sensitif di release; logging debug dinonaktifkan atau disanitasi untuk produksi.
- Minta izin lokasi saat fitur relevan dipilih, jelaskan manfaatnya, dukung penolakan, dan minimalkan retensi.
- Terapkan prinsip least privilege pada workflow token dan akses database; API publik memiliki pembatasan beban dan observabilitas.
- Kontrol kontras, ukuran teks, target sentuh, label pembaca layar, dan state yang tidak hanya dibedakan oleh warna pada fitur inti.

### 10.5 Reliabilitas dan observabilitas

- Backend mencatat request ID, latency, status, dan error agregat; hindari koordinat pengguna/identifier sensitif dalam log.
- Pantau kesegaran feed live, kegagalan sinkronisasi, error API, crash aplikasi, serta durasi workflow.
- Tetapkan health/readiness check backend dan alarm operasional setelah deployment tersedia.

## 11. Rencana Validasi dan Kriteria Rilis MVP

MVP dapat dirilis saat kriteria berikut dipenuhi:

1. OpenAPI disetujui dan contoh response konsisten dengan endpoint backend serta model Android.
2. Data rute, halte, dan jadwal memiliki sumber, validasi, serta demonstrasi sinkronisasi ke Room; mode pesawat membuktikan data tersimpan tetap dapat dibaca.
3. Peta OSMDroid menampilkan atribusi dan tetap menyediakan akses daftar saat tile/jaringan gagal.
4. Live telemetry hanya berasal dari feed terverifikasi atau secara eksplisit ditandai simulasi; timestamp dan aturan stale diuji.
5. API mendukung endpoint MVP, validasi input, pagination/rate controls yang ditetapkan, dan error schema terdokumentasi.
6. Proses CI menjalankan lint, unit tests, dan debug build; release menghasilkan APK/AAB bernama konsisten dan dapat diunduh. Target waktu <10 menit diverifikasi pada kondisi runner yang ditetapkan.
7. Landing page menyebut dukungan dan batasan data secara jujur, menautkan dokumentasi dan APK terbaru, serta menyediakan fallback jika belum ada rilis.
8. Privasi lokasi, izin, atribusi peta, sumber data, dan kontak dukungan dijelaskan sebelum publikasi.

## 12. Roadmap MVP Lintas Empat Repo

Urutan berorientasi dependensi; perubahan kontrak disepakati lintas repo dan ditulis ke PRD/OpenAPI terlebih dahulu.

| Tahap | Repo | Hasil kerja / exit criteria |
|---|---|---|
| 0 — Baseline spesifikasi | Semua | PRD ini ditinjau; daftar sumber data, pemilik, batas MVP, definisi freshness dan KPI disepakati. Catat risiko dan keputusan terbuka. |
| 1 — Kontrak dan dokumentasi | `apidocs` | OpenAPI 3.0 untuk skema/error/pagination/endpoint; contoh response realistis berlabel; Swagger UI GitHub Pages dapat memuat file spec. Exit: kontrak ditinjau konsumen Android/backend. |
| 2 — Data dan API | `backend` | Struktur Go modular, konfigurasi aman, koneksi MongoDB, indeks, seed demo berlabel, endpoint katalog/jadwal/live sesuai OpenAPI, validasi dan observabilitas. Exit: API lulus contract checks dan ada data sumber yang provenance-nya jelas. |
| 3 — Aplikasi offline-first | `geoakses` | UI Compose, peta OSMDroid, filter operator, direktori halte, Room untuk routes/stops/schedules, sinkronisasi atomik dan fallback. Exit: skenario online/offline/timeout dan status freshness lolos penerimaan. |
| 4 — Release engineering | Workflow root / `geoakses` | CI lint/test/build, auto-tag berdasarkan versi, signing release dari secrets, APK/AAB artifact, release notes, audit izin token. Exit: alur diuji end-to-end dan target durasi tercatat. |
| 5 — Publikasi produk | `geoakses.github.io` | Landing page responsif, panduan, link OpenAPI, unduhan APK dari release terbaru dengan fallback. Exit: URL GitHub Pages aktif dan tautan diuji. |
| 6 — Pilot dan iterasi | Semua | Pilot wilayah/data terbatas, pantau freshness dan performa, kumpulkan umpan balik, prioritaskan feeder dan koneksi operator setelah validasi data. |

## 13. Risiko, Asumsi, dan Keputusan Terbuka

| Item | Dampak / tindak lanjut |
|---|---|
| Feed GPS dan okupansi operator belum dipastikan | Tanpa feed sah, live tracking adalah demo saja. Tetapkan pemilik integrasi dan izin sebelum memasarkan realtime. |
| Jadwal/rute feeder tidak seragam atau tidak terstruktur | Batasi cakupan awal ke data tervalidasi; publikasikan cakupan dan tanggal verifikasi. |
| Repositori backend, apidocs, dan landing page masih placeholder | Detail implementasi belum menjadi fakta; roadmap memulai kontrak dan implementasi dari PRD ini. |
| Kontrak Android saat ini memakai base URL `https://api.geoakses.bandung.go.id/v1/` dan DTO tersendiri | Belum terlihat implementasi layanan nyata. Saat membangun OpenAPI, selaraskan host/base path dan payload dengan client; jangan menganggap host contoh tersebut siap produksi. |
| Target <200 ms dapat salah ditafsirkan sebagai RTT end-to-end | KPI membedakan respons lokal/render dari server p95 dan mengecualikan RTT jaringan publik; laporkan keduanya terpisah. |
| Tarif dan jam operasional berubah | Simpan provenance/waktu verifikasi dan sediakan proses pembaruan. |
| Cache OSM memiliki kebijakan provider | Pilih layanan tile yang sesuai dan patuhi atribusi/ketentuan; jangan mengasumsikan server publik mendukung prefetch offline. |

Keputusan yang harus dikunci sebelum implementasi backend: host produksi dan base path API; daftar operator/enum; format pagination dan envelope response; ambang freshness live; sumber serta lisensi data; kebijakan retensi telemetry; model identitas publik kendaraan; definisi cakupan MVP Bemo/Angkot.

## 14. Catatan Baseline Audit Repo

Audit awal menemukan aplikasi Android telah memiliki scaffold Kotlin/Compose, konfigurasi Hilt/Room/Retrofit/OSMDroid, model `Route`, `Stop`, `Bus`, `Schedule`, DAO Route/Stop, layar utama, dan data mock. Database Room saat ini hanya mendaftarkan `Route` dan `Stop`, sehingga penyimpanan offline jadwal masih merupakan kebutuhan, bukan kemampuan yang terkonfirmasi. Repository Android mengeluarkan data mock dan mensimulasikan pergerakan bus; telemetry tersebut bukan feed live terverifikasi.

Folder `backend`, `apidocs`, dan `geoakses.github.io` pada audit ini hanya memuat README ringkas dan lisensi, tanpa implementasi layanan, OpenAPI, atau situs yang dapat diverifikasi. Workflow CI, auto-tag, dan release sudah ada di `.github/workflows` pada workspace root; workflow adalah baseline yang perlu diuji dan diselaraskan dengan target acceptance PRD, bukan bukti bahwa durasi <10 menit atau publikasi produksi telah tercapai.

Dokumen ini menjadi baseline produk lintas-repo. Status “ada” pada audit tidak menyatakan fitur siap produksi atau tervalidasi terhadap data operator. Perbarui bagian ini saat implementasi berubah dan pertahankan keputusan/kontrak terbaru di PRD serta OpenAPI.
