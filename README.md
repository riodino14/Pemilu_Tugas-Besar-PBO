# Aplikasi Pemilu Berbasis Spring Boot

Selamat datang di repositori **Aplikasi Pemilu**, sebuah proyek web yang dikembangkan dengan menggunakan **Spring Boot** di sisi backend dan file statis (HTML, CSS, JavaScript) di sisi frontend. Aplikasi ini dirancang untuk memudahkan proses pemilihan umum, terutama dalam pengelolaan **data calon legislatif** dan **pemilih**, serta proses **voting** yang aman dan terstruktur.

## Daftar Isi
1. [Deskripsi Singkat](#deskripsi-singkat)
2. [Fitur Utama](#fitur-utama)
3. [Struktur Proyek](#struktur-proyek)
4. [Cara Instalasi dan Menjalankan](#cara-instalasi-dan-menjalankan)
5. [Panduan Penggunaan](#panduan-penggunaan)
   - [Login Admin](#login-admin)
   - [Login Pemilih](#login-pemilih)
   - [CRUD Data Calon Legislatif](#crud-data-calon-legislatif)
   - [Voting](#voting)
   - [Rekapitulasi Hasil](#rekapitulasi-hasil)
6. [Konsep OOP yang Diterapkan](#konsep-oop-yang-diterapkan)
7. [Kontribusi](#kontribusi)
8. [Lisensi](#lisensi)

---

## Deskripsi Singkat
Aplikasi Pemilu ini dikembangkan untuk **memfasilitasi pemilihan umum** dengan menampilkan alur kerja yang **empiris, sistematis, dan logis** dalam proses pemungutan suara. Sistem ini mengusung:
- **Peran Admin** (Petugas KPU) yang bertanggung jawab mengelola data pemilih dan calon legislatif.
- **Peran Pemilih** yang dapat memberikan suara untuk setiap kategori pemilihan (DPR, DPRD Provinsi, DPRD Kabupaten/Kota).

Proyek mengadopsi **Object-Oriented Programming (OOP)** serta memanfaatkan keunggulan **Spring Boot** dalam pengembangan aplikasi berbasis Java. Bagian frontend menggunakan file statis (HTML, CSS, JS) yang diatur di folder **static**.

---

## Fitur Utama
1. **Manajemen Pengguna**  
   \- Admin dan Pemilih memiliki hak akses berbeda.  
   \- Sistem login berbasis **role** (Admin/Pemilih).

2. **CRUD Data**  
   \- Admin dapat membuat, membaca, memperbarui, dan menghapus data calon legislatif dan pemilih.  
   \- Data mudah dikelola melalui antarmuka yang ramah pengguna.

3. **Voting**  
   \- Pemilih memberikan suara untuk setiap kategori (DPR, DPRD Provinsi, DPRD Kabupaten/Kota).  
   \- Sistem otomatis merekam dan menghitung suara.

4. **Rekapitulasi Hasil**  
   \- Admin dapat melihat hasil pemilu berdasarkan **partai** maupun **nama calon legislatif**.  
   \- Rekapitulasi disajikan secara real-time.

5. **Keamanan dan Autentikasi**  
   \- Penggunaan autentikasi login untuk membedakan peran pengguna.  
   \- Menjamin integritas data dan menjauhkan pemalsuan suara.

---

## Struktur Proyek

```
.
├── config
│   └── WebConfig.java          // Pengaturan MVC, CORS, resource handler
├── controller
│   ├── AdminController.java    // Pengaturan request untuk Admin
│   └── PemilihController.java  // Pengaturan request untuk Pemilih
├── dto
│   └── LoginAdminRequest.java  // Contoh DTO untuk menerima data login
├── entity
│   ├── CalonLegislatif.java    // Entity calon legislatif
│   └── Pemilih.java            // Entity pemilih
├── repository
│   └── CalonLegislatifRepository.java  // Repository untuk entitas calon
├── service
│   └── PemiluService.java      // Logika bisnis pemilihan (CRUD, voting)
├── utils
│   └── ApiResponse.java        // Format response API
├── static
│   ├── loginAdmin.html         // Halaman login admin
│   ├── loginPemilih.html       // Halaman login pemilih
│   ├── tambahcalon.html        // Halaman untuk menambahkan calon legislatif
│   ├── admin.css               // Styling halaman admin
│   └── style.css               // Styling umum
├── application.properties      // Konfigurasi aplikasi (DB, port, dll.)
├── PemiluApplication.java      // Kelas utama Spring Boot
└── pom.xml                     // Konfigurasi Maven
```

Penjelasan singkat:
- **config**: Menangani berbagai konfigurasi (Spring MVC, CORS, dsb.).
- **controller**: Memastikan alur data dari frontend ke backend berjalan dengan benar.
- **dto**: Objek transfer data (misalnya data form login).
- **entity**: Memetakan tabel di database (menggunakan JPA).
- **repository**: Berisi interface untuk operasi database (CRUD).
- **service**: Menampung logika bisnis pemilu (validasi, penghitungan suara, dsb.).
- **utils**: Kelas pembantu untuk respons, konversi data, atau utilitas lainnya.
- **static**: Berisi file frontend (HTML, CSS, JS) untuk menampilkan antarmuka kepada pengguna.

---

## Cara Instalasi dan Menjalankan

### Prasyarat
1. **Java** 8 atau lebih baru.
2. **Maven** untuk manajemen dependensi.
3. **Database** (misal MySQL/PostgreSQL) yang sudah terinstall dan dikonfigurasi.
4. **Git** (opsional) untuk melakukan clone repo.

### Langkah-langkah

1. **Clone repositori**
   ```bash
   git clone https://github.com/username/pemilu-springboot.git
   ```
2. **Masuk ke folder proyek**
   ```bash
   cd pemilu-springboot
   ```
3. **Atur `application.properties`**  
   Sesuaikan pengaturan database, port, atau properti lain di `application.properties`.
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/db_pemilu
   spring.datasource.username=root
   spring.datasource.password=12345
   spring.jpa.hibernate.ddl-auto=update
   server.port=8080
   ```
4. **Jalankan perintah Maven**
   ```bash
   mvn clean install
   ```
5. **Menjalankan aplikasi**
   ```bash
   mvn spring-boot:run
   ```
6. **Akses aplikasi**  
   Buka browser dan arahkan ke:
   ```
   http://localhost:8080/
   ```

---

## Panduan Penggunaan

### Login Admin
1. Buka halaman: `http://localhost:8080/loginAdmin.html`
2. Masukkan **username** dan **password** Admin.
3. Setelah berhasil login, Admin dapat mengakses fitur manajemen calon legislatif dan pemilih.

### Login Pemilih
1. Buka halaman: `http://localhost:8080/loginPemilih.html`
2. Masukkan **ID Pemilih** dan **password** (atau username, bergantung implementasi).
3. Setelah login, Pemilih dapat memberikan suara.

### CRUD Data Calon Legislatif
- **Tambah Calon**: Isi form di `tambahcalon.html` untuk menambahkan data baru.
- **Edit / Hapus Calon**: Akses endpoint atau halaman khusus yang menampilkan daftar calon, lalu pilih aksi edit/hapus.
- **Lihat Daftar Calon**: Admin dapat menggunakan endpoint `/api/admin/getAllCalon` atau halaman antarmuka untuk menampilkan semua calon legislatif.

### Voting
- Pemilih memilih satu calon pada masing-masing kategori: **DPR**, **DPRD Provinsi**, **DPRD Kabupaten/Kota**.
- Sistem akan merekam suara secara otomatis ke database.

### Rekapitulasi Hasil
- Admin dapat melihat rekapitulasi hasil pemilihan berdasarkan **partai** atau **nama calon**.
- Statistik dapat diakses melalui endpoint (misalnya, `/api/admin/rekapitulasi`) atau halaman khusus yang menampilkan hasil perolehan suara.

---

## Konsep OOP yang Diterapkan
- **Inheritance (Pewarisan)**: Misalnya, `Admin` dan `Pemilih` mewarisi class `User` (jika diimplementasikan).
- **Encapsulation (Pengkapsulan)**: Penggunaan getter/setter serta access modifiers untuk melindungi data.
- **Polymorphism**: Dapat diimplementasikan lewat interface atau method overriding di service layer.
- **Class Relationships**:  
  - **Association**: `Admin` dan `Pemilih` berinteraksi dengan `CalonLegislatif` dalam proses CRUD dan voting.  
  - **Aggregation**: `CalonLegislatif` memiliki data partai, visi-misi yang tetap berdiri sendiri.  
  - **Composition**: Data voting melekat pada identitas `Pemilih` dan tidak dapat dipisahkan.

Dengan konsep ini, aplikasi lebih mudah dikelola dan diperluas di kemudian hari.

---

## Kontribusi
Kontribusi dalam bentuk **pull request**, **issue**, atau **diskusi** sangat kami hargai. Silakan ikuti langkah berikut jika ingin berkontribusi:

1. **Fork** repositori.
2. Buat **branch** baru untuk fitur atau perbaikan tertentu.
3. Lakukan perubahan, commit, dan push ke branch Anda.
4. Ajukan **pull request** ke repositori utama.

---

## Lisensi
Proyek ini dilisensikan di bawah [MIT License](LICENSE). Anda bebas memodifikasi dan mendistribusikan kembali dengan menyertakan lisensi asli.

---

Terima kasih telah menggunakan **Aplikasi Pemilu** ini! Jika Anda memiliki pertanyaan, saran, atau menemukan bug, jangan ragu untuk membuka issue atau menghubungi kami. Semoga proyek ini bermanfaat dan dapat mempermudah proses pemilihan umum di berbagai tingkatan!
