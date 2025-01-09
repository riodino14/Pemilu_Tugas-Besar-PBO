package com.tubesoop.pemilu.repository;

// Entity Admin merepresentasikan tabel dalam database.
import com.tubesoop.pemilu.entity.Admin;

// Mengimpor JpaRepository dari Spring Data JPA.
// JpaRepository menyediakan berbagai operasi database bawaan seperti save, delete, dan find.
import org.springframework.data.jpa.repository.JpaRepository;


// Admin adalah nama entity, dan Long adalah tipe data primary key dari entity tersebut.
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // findByEmail menggunakan kolom "email" dalam tabel Admin sebagai parameter pencarian.
    Admin findByEmail(String email);
}
