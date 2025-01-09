package com.tubesoop.pemilu.repository;

// JpaRepository menyediakan berbagai metode bawaan untuk mengelola operasi database.
import org.springframework.data.jpa.repository.JpaRepository;


import com.tubesoop.pemilu.entity.Pemilih;


// Tipe generik pertama, Pemilih, adalah entity yang akan dimanipulasi.
// Tipe generik kedua, String, adalah tipe data primary key dari entity Pemilih.
public interface PemilihRepository extends JpaRepository<Pemilih, String> {
  
    // Metode ini mengembalikan objek Pemilih yang sesuai dengan "nik" yang diberikan, atau null jika tidak ditemukan.
    Pemilih findByNik(String nik);

    // Metode ini juga mengembalikan objek Pemilih yang sesuai dengan nomor telepon yang diberikan.
    Pemilih findByNoTelp(String noTelp);

    // Sama seperti metode sebelumnya, ini mengembalikan objek Pemilih yang cocok dengan email yang diberikan.
    Pemilih findByEmail(String email);
}
