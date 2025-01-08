package com.tubesoop.pemilu.repository;

import com.tubesoop.pemilu.entity.CalonLegislatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalonLegislatifRepository extends JpaRepository<CalonLegislatif, Long> {
    List<CalonLegislatif> findByJenis(String jenis);  // Query untuk jenis
    boolean existsByNama(String nama);  // Cek nama calon

    List<CalonLegislatif> findByDaerahPemilihan(String daerahPemilihan);

}
