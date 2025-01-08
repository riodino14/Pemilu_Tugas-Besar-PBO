package com.tubesoop.pemilu.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.tubesoop.pemilu.entity.Pemilih;


public interface PemilihRepository extends JpaRepository<Pemilih, String> {
    Pemilih findByNik(String nik);
    Pemilih findByNoTelp(String noTelp);
    Pemilih findByEmail(String email);
    
}

