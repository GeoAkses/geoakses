# GeoAkses Android

Aplikasi transportasi publik GeoAkses untuk Bandung Raya.

## Konfigurasi API Android

Build debug secara default menargetkan backend Go lokal pada Android Emulator
melalui `http://10.0.2.2:8080/api/v1/`. Untuk perangkat fisik atau host lain,
atur `GEOAKSES_API_BASE_URL` atau Gradle property
`-PgeoaksesApiBaseUrl=http://<alamat-host>:8080/api/v1/`.

Build release hanya menerima URL HTTPS. Jika URL release belum diatur, aplikasi
memakai host `.invalid` yang tidak dapat di-resolve dan tetap berjalan dengan
data lokal/cache sampai URL backend yang sah dikonfigurasi.
