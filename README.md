# MovieExplorer
MovieExplorer adalah aplikasi Android yang memungkinkan pengguna mengeksplorasi film populer, mencari berdasarkan genre, dan menyimpan film favorit mereka untuk ditonton nanti. Dibangun dengan Java dan menggunakan API The Movie Database (TMDB), aplikasi ini menawarkan pengalaman pengguna yang intuitif dengan dukungan tema terang dan gelap.

## 📝 Deskripsi Aplikasi
MovieExplorer adalah aplikasi Android untuk menjelajahi dan menyimpan informasi film populer dari The Movie Database (TMDB). Aplikasi ini dirancang untuk pecinta film yang ingin mengeksplorasi film terbaru, mencari film berdasarkan genre, dan menyimpan film favorit mereka.

### Fitur Utama
- **Beranda** - Menampilkan film Top Rated dan film Latest (Now Playing)
- **Pencarian** - Mencari film berdasarkan judul dan genre
- **Detail Film** - Melihat informasi lengkap dan trailer film
- **Bookmark** - Menyimpan dan mengelola daftar film favorit
- **Toggle Tema** - Beralih antara tema terang dan gelap
## 🚀 Cara Penggunaan
1. Beranda
   - Tab pertama menampilkan film Top Rated dan film terbaru
   - Tap pada film untuk melihat detail
   - Tap "See More" untuk melihat lebih banyak film terbaru
2. Pencarian
   - Tap ikon pencarian di navigasi bawah
   - Ketik judul film di kolom pencarian
   - Pilih genre untuk memfilter hasil pencarian
   - Riwayat pencarian terakhir akan ditampilkan untuk pencarian cepat
3. Detail Film
   - Lihat informasi lengkap tentang film
   - Putar trailer dengan tap pada thumbnail atau tombol play
   - Bookmark film dengan tap ikon bookmark di pojok kanan atas
   - Lihat daftar pemain (cast) film
4. Bookmark
   - Tap ikon bookmark di navigasi bawah untuk melihat film yang telah disimpan
   - Tap film untuk melihat detailnya
   - Hapus film dari bookmark dengan tap ikon hapus
5. Mengubah Tema
   - Tap ikon tema di pojok kanan atas
   - Aplikasi akan beralih antara tema terang dan gelap
    
## 🔧 Implementasi Teknis
### Struktur Folder
```
java/com/example/movieapp/
├── data/
│   ├── local/
│   │   ├── BookmarkContract.java
│   │   ├── BookmarkManager.java
│   │   └── DatabaseHelper.java
│   ├── model/
│   │   ├── Cast.java
│   │   ├── CastResponse.java
│   │   ├── Film.java
│   │   ├── MovieResponse.java
│   │   ├── Video.java
│   │   └── VideoResponse.java
│   └── network/
│       ├── ApiConfig.java
│       └── ApiService.java
├── ui/
│   ├── BookmarkFragment.java
│   ├── CastAdapter.java
│   ├── DetailActivity.java
│   ├── DiscoverActivity.java
│   ├── FilmAdapter.java
│   ├── HomeFragment.java
│   ├── MainActivity.java
│   └── SearchFragment.java
└── utils/
    └── ThemeUtils.java
```
### Manajemen Data
1. **Remote Data**
    - Integrasi dengan TMDB API menggunakan Retrofit
    - Endpoint: top_rated, now_playing, search, videos, credits
3. **Local Storage**
   - SQLite untuk menyimpan film yang di-bookmark
   - SharedPreferences untuk menyimpan riwayat pencarian dan pengaturan tema
5. **Background Processing**
   - ExecutorService untuk operasi database
   - Handler untuk komunikasi dengan UI thread
### Penanganan Media
- **Image Loading**: Glide untuk memuat dan cache gambar film
- **Video Playback**: YouTube Android Player API untuk pemutaran trailer
### Responsive Media
- RecyclerView dengan berbagai ViewHolder untuk menampilkan film dalam format yang berbeda
- ConstraintLayout untuk layout yang responsif di berbagai ukuran layar
- Support untuk tema terang dan gelap

## 📋 Persyaratan Teknis
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34
- Bahasa Pemrograman: Java
- Dependensi Utama:
  - androidx.appcompat:appcompat
  - com.google.android.material:material
  - androidx.constraintlayout:constraintlayout
  - com.github.bumptech.glide:glide
  - com.squareup.retrofit2:retrofit
  - com.pierfrancescosoffritti.androidyoutubeplayer:core


