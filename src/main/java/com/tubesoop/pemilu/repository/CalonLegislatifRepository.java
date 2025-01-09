package com.tubesoop.pemilu.repository;

// Entity ini merepresentasikan tabel database yang berisi data calon legislatif.
import com.tubesoop.pemilu.entity.CalonLegislatif;

// JpaRepository menyediakan berbagai metode bawaan untuk pengelolaan data, seperti simpan, hapus, dan pencarian.
import org.springframework.data.jpa.repository.JpaRepository;


// Anotasi ini memberi tahu Spring bahwa interface ini adalah komponen repository yang dikelola Spring.
import org.springframework.stereotype.Repository;


// List digunakan untuk mengembalikan kumpulan data (list) dari hasil pencarian repository.
import java.util.List;


@Repository
public interface CalonLegislatifRepository extends JpaRepository<CalonLegislatif, Long> {

  
    // Metode ini mengabaikan huruf besar/kecil pada parameter (case-insensitive).
    List<CalonLegislatif> findByJenisIgnoreCase(String jenis);

   
    // Metode ini akan mengembalikan true jika nama ditemukan, atau false jika tidak.
    boolean existsByNama(String nama);

    // Sama seperti findByJenisIgnoreCase, metode ini juga case-insensitive.
    List<CalonLegislatif> findByDaerahPemilihanIgnoreCase(String daerahPemilihan);
}
