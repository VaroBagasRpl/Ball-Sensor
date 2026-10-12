# Sensor Playground 📱⚡

Aplikasi Android modern interaktif dibangun menggunakan **Kotlin**, **Jetpack Compose (Material 3)**, dan **Hardware Sensors API**. Aplikasi ini mengintegrasikan 4 sensor dari 3 kategori berbeda dengan 3 mode kontrol untuk mengontrol arena permainan fisik, warna dinamis, dan tema visual secara real-time.

---

## 📊 Daftar Sensor & Efek ke UI

| Nama Sensor | Kategori | Tipe Konstanta | Efek ke UI |
| :--- | :--- | :--- | :--- |
| **Accelerometer** | MOTION | `Sensor.TYPE_ACCELEROMETER` | Memindahkan bola neon pada mode TILT. Memiliki dead zone ~0.4 m/s², low-pass filter alpha ~0.10, dan shake detection (>2.7g) untuk reset bola & efek confetti. |
| **Gyroscope** | MOTION | `Sensor.TYPE_GYROSCOPE` | Mengontrol kecepatan bola berdasarkan kecepatan rotasi sudut HP (rad/s) pada mode GYRO dengan dead zone ~0.05 rad/s. |
| **Rotation Vector** | POSITION | `Sensor.TYPE_ROTATION_VECTOR` | Menghitung azimuth kompas. Memutar cincin kompas digital (Canvas) & mengubah hue warna aksen border arena. |
| **Light Sensor** | ENVIRONMENT | `Sensor.TYPE_LIGHT` | Mengatur tema visual otomatis (Dark Theme di < 20 lux, Light Theme di > 60 lux dengan *hysteresis*) & menggerakkan gauge *Light Meter*. |

---

## 🎮 3 Mode Kontrol Arena

1. **Mode TILT (Kemiringan Akselerometer)**:
   - Bola bergerak mengikuti arah kemiringan HP.
   - Dilengkapi *dead zone* ~0.4 m/s² agar bola tidak hanyut/bergeser saat HP diletakkan di atas meja.
2. **Mode GYRO (Kecepatan Rotasi Giroskop)**:
   - Bola bergerak merespons kecepatan putaran rotasi HP.
   - Didaftarkan secara dinamis hanya saat mode GYRO aktif. Otomatis *fallback* ke mode TILT jika perangkat tidak memiliki sensor giroskop.
3. **Mode TOUCH (Sentuh & Lempar / Fling)**:
   - Bola dapat digenggam dan digeser langsung menggunakan jari.
   - Saat dilepas, bola mendapat dorongan momentum kecepatan (*fling physics* via `VelocityTracker`) lalu meluncur dan memantul di dinding.
   - *Catatan*: Sentuhan jari pada bola tetap bisa digunakan di mode mana pun untuk menggenggam bola sementara.

---

## 🎛️ Parameter Fisika & Sensitivitas

- **Slider Sensitivitas**: Dapat disesuaikan dari `0.2x` (halus) hingga `2.0x` (lincah) secara real-time.
- **Damping / Gesekan**: `0.985f` per frame untuk gerakan meluncur yang mulus.
- **Pantulan Dinding**: Koefisien pantulan `0.45f` dengan efek suara benturan (`ToneGenerator`) & getaran haptic (`Vibrator`).
- **Batas Kecepatan Maksimum**: Dibatasi hingga `~900 dp/s`.

---

## 🔄 Pengelolaan Lifecycle Sensor

Registrasi listener sensor dikelola secara ketat pada lifecycle **`MainActivity`**:

- **`onResume()`**: Mendaftarkan listener sensor via `sensorController.start()` menggunakan flag `isRegistered` untuk menghindari registrasi ganda.
- **`onPause()`**: Menghentikan listener via `sensorController.stop()` saat aplikasi berada di latar belakang.
- **Manajemen Giroskop Dinamis**: Listener giroskop hanya didaftarkan saat mode aktif adalah `GYRO` dan langsung dicabut saat berpindah ke mode lain.

### Potongan Kode Lifecycle (`MainActivity.kt`):
```kotlin
override fun onResume() {
    super.onResume()
    // LIFECYCLE: Register listener di onResume
    sensorController.start()
}

override fun onPause() {
    super.onPause()
    // LIFECYCLE: Unregister listener di onPause
    sensorController.stop()
}
```

---

## 🚀 Cara Menjalankan & Menguji di Emulator

1. **Jalankan Aplikasi**:
   - Buka project di Android Studio.
   - Pilih target emulator atau perangkat fisik (minSdk 26).
   - Klik tombol **Run (`▶`)**.

2. **Pengujian Sensor di Emulator**:
   - Buka panel **Extended Controls** di emulator (klik ikon tiga titik `...` di toolbar emulator).
   - Buka menu **Virtual Sensors**:
     - **Accelerometer & Gyroscope**: Putar/miringkan slider atau model 3D HP untuk menguji mode TILT dan GYRO.
     - **Rotation Vector**: Putar slider Yaw/Pitch/Roll untuk menguji pergerakan kompas dan perubahan warna aksen arena.
     - **Light Sensor**: Geser slider *Light (lux)* ke bawah < 20 lux untuk menguji transisi ke Dark Theme.

---

## 📁 Struktur Folder Project

```
com.example.balanceball/
├── sensor/
│   ├── SensorController.kt      # Manager pembungkus SensorEventListener
│   └── SensorState.kt           # Data class penampung pembacaan sensor
├── ui/
│   ├── components/
│   │   ├── ArenaView.kt         # Arena fisika bola, gyro, & touch fling
│   │   ├── CompassView.kt       # Kompas digital Canvas & smoothing
│   │   ├── ControlModeSelector.kt # Segmented button Tilt | Gyro | Sentuh
│   │   ├── LightMeterCard.kt    # Gauge lux & chip status cahaya
│   │   ├── SensitivityCard.kt   # Slider sensitivitas gerakan 0.2x - 2.0x
│   │   ├── SensorDataCard.kt    # Kartu data real-time & indikator status
│   │   └── ThemeModeSelector.kt # Segmented button Auto / Light / Dark
│   ├── theme/
│   │   ├── Color.kt             # Palet warna neon & glassmorphism
│   │   ├── Theme.kt             # Material3 Theme dengan hysteresis
│   │   └── Type.kt              # Tipografi Material3
│   ├── MainScreen.kt            # Layout utama aplikasi
│   └── MainViewModel.kt         # ViewModel status UI & kontrol
└── MainActivity.kt              # Entry point & pengelola lifecycle sensor
```
