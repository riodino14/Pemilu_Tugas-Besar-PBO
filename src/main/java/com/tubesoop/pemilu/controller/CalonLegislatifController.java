// CalonLegislatifController.java
package com.tubesoop.pemilu.controller;

import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.repository.CalonLegislatifRepository;

@RestController
@RequestMapping("/api/calon")
public class CalonLegislatifController {

    @Autowired
    private CalonLegislatifRepository calonLegislatifRepository;

    @GetMapping("/rekapitulasi")
    public ResponseEntity<List<CalonLegislatif>> getRekapitulasiSuara(
        @RequestParam(value = "jenis", required = false) String jenis,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "daerahPemilihan", required = false) String daerahPemilihan
    ) {
        // Ambil semua dulu
        List<CalonLegislatif> candidates = calonLegislatifRepository.findAll();

        // Filter by jenis (jika param "jenis" tidak kosong)
        if (jenis != null && !jenis.trim().isEmpty()) {
            // Contoh: "DPR", "DPRD Provinsi", dsb.
            // di front-end, "" = "Semua"
            candidates = candidates.stream()
                    .filter(c -> c.getJenis().equalsIgnoreCase(jenis))
                    .collect(Collectors.toList());
        }

        // Filter by daerahPemilihan (partial match)
        if (daerahPemilihan != null && !daerahPemilihan.trim().isEmpty()) {
            String keyword = daerahPemilihan.toLowerCase();
            candidates = candidates.stream()
                    .filter(c -> c.getDaerahPemilihan() != null &&
                                 c.getDaerahPemilihan().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        // Hitung persentase suara
        int totalVotes = candidates.stream()
                .mapToInt(CalonLegislatif::getTotalSuara)
                .sum();
        for (CalonLegislatif calon : candidates) {
            double percentage = totalVotes == 0 ? 0 : ((double) calon.getTotalSuara() / totalVotes) * 100;
            calon.setPersentaseSuara(percentage);
        }

        // Sorting asc/desc by persentaseSuara
        if ("asc".equalsIgnoreCase(sort)) {
            candidates.sort(Comparator.comparingDouble(CalonLegislatif::getPersentaseSuara));
        } else if ("desc".equalsIgnoreCase(sort)) {
            candidates.sort(Comparator.comparingDouble(CalonLegislatif::getPersentaseSuara).reversed());
        }

        return ResponseEntity.ok(candidates);
    }
    @GetMapping("/rekapitulasi-partai")
    public ResponseEntity<Map<String, Integer>> getRekapitulasiSuaraPerPartai() {
        List<CalonLegislatif> candidates = calonLegislatifRepository.findAll();
        Map<String, Integer> suaraPerPartai = new HashMap<>();
        for (CalonLegislatif calon : candidates) {
            String partai = calon.getPartai();
            suaraPerPartai.put(partai, suaraPerPartai.getOrDefault(partai, 0) + calon.getTotalSuara());
        }
        return ResponseEntity.ok(suaraPerPartai);
    }

    @GetMapping("/calon-daerah/{daerahPemilihan}")
    public ResponseEntity<List<CalonLegislatif>> getCalonByDaerah(@PathVariable String daerahPemilihan) {
        List<CalonLegislatif> candidates = calonLegislatifRepository.findByDaerahPemilihanIgnoreCase(daerahPemilihan);
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/rekapitulasi-all")
    public ResponseEntity<List<CalonLegislatif>> getAllRekapitulasi() {
        List<CalonLegislatif> candidates = calonLegislatifRepository.findAll();
        int totalVotes = candidates.stream().mapToInt(CalonLegislatif::getTotalSuara).sum();
        for (CalonLegislatif calon : candidates) {
            double percentage = totalVotes == 0 ? 0 : ((double) calon.getTotalSuara() / totalVotes) * 100;
            calon.setPersentaseSuara(percentage);
        }
        return ResponseEntity.ok(candidates);
    }

    // Endpoint CRUD lainnya tetap seperti sebelumnya
}
