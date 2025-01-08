// CalonLegislatifRepository.java
package com.tubesoop.pemilu.repository;

import com.tubesoop.pemilu.entity.CalonLegislatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalonLegislatifRepository extends JpaRepository<CalonLegislatif, Long> {
    List<CalonLegislatif> findByJenisIgnoreCase(String jenis);
    boolean existsByNama(String nama);
    List<CalonLegislatif> findByDaerahPemilihanIgnoreCase(String daerahPemilihan);
}
