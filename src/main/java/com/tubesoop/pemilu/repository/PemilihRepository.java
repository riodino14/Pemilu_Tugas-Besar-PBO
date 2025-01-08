package com.tubesoop.pemilu.repository;


import com.tubesoop.pemilu.entity.Pemilih;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PemilihRepository extends JpaRepository<Pemilih, String> {
    Pemilih findByNik(String nik);
}

