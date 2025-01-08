package com.tubesoop.pemilu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tubesoop.pemilu.dto.VoteRequest;
import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.entity.Pemilih;
import com.tubesoop.pemilu.repository.CalonLegislatifRepository;
import com.tubesoop.pemilu.repository.PemilihRepository;
import com.tubesoop.pemilu.utils.ApiResponse;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/pemilih")
public class PemilihController {

    @Autowired
    private PemilihRepository pemilihRepository;

    @Autowired
    private CalonLegislatifRepository calonLegislatifRepository;

    // Endpoint untuk mendapatkan daftar calon berdasarkan daerah pemilihan
    @GetMapping("/calon-daerah/{daerahPemilihan}")
    public ResponseEntity<List<CalonLegislatif>> getCandidatesByDaerah(@PathVariable String daerahPemilihan) {
        try {
            List<CalonLegislatif> candidates = calonLegislatifRepository.findByDaerahPemilihanIgnoreCase(daerahPemilihan);
            if (candidates.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint baru untuk voting batch
    @PostMapping("/vote/batch")
    @Transactional
    public ResponseEntity<ApiResponse> voteBatch(@RequestBody List<VoteRequest> voteRequests) {
        if (voteRequests == null || voteRequests.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Tidak ada vote yang dikirimkan"));
        }

        String nik = voteRequests.get(0).getNik();
        for (VoteRequest vr : voteRequests) {
            if (!vr.getNik().equals(nik)) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "NIK dalam vote batch harus sama"));
            }
        }

        Pemilih pemilih = pemilihRepository.findByNik(nik);
        if (pemilih == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Pemilih tidak ditemukan"));
        }
        if (pemilih.isSudahMemilih()) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Anda sudah memberikan suara"));
        }

        try {
            for (VoteRequest voteRequest : voteRequests) {
                CalonLegislatif calon = calonLegislatifRepository.findById(voteRequest.getCandidateId())
                        .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan dengan ID: " + voteRequest.getCandidateId()));

                calon.setTotalSuara(calon.getTotalSuara() + 1);
                calonLegislatifRepository.save(calon);
            }

            pemilih.setSudahMemilih(true);
            pemilihRepository.save(pemilih);

            return ResponseEntity.ok(new ApiResponse("success", "Suara Anda berhasil tercatat"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("error", "Terjadi kesalahan: " + e.getMessage()));
        }
    }

    // Endpoint baru untuk rekapitulasi per daerah
    @GetMapping("/rekapitulasi-daerah/{daerahPemilihan}")
    public ResponseEntity<List<CalonLegislatif>> getRekapitulasiByDaerah(@PathVariable String daerahPemilihan) {
        List<CalonLegislatif> candidates = calonLegislatifRepository.findByDaerahPemilihanIgnoreCase(daerahPemilihan);
        if (candidates.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/vote")
    public ResponseEntity<ApiResponse> vote(@RequestBody VoteRequest voteRequest) {
        try {
            Pemilih pemilih = pemilihRepository.findByNik(voteRequest.getNik());
            if (pemilih == null) return ResponseEntity.badRequest().body(new ApiResponse("error", "Pemilih tidak ditemukan"));
            if (pemilih.isSudahMemilih()) return ResponseEntity.badRequest().body(new ApiResponse("error", "Anda sudah memberikan suara"));

            CalonLegislatif calon = calonLegislatifRepository.findById(voteRequest.getCandidateId())
                    .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan"));
    
            calon.setTotalSuara(calon.getTotalSuara() + 1);
            pemilih.setSudahMemilih(true);
    
            calonLegislatifRepository.save(calon);
            pemilihRepository.save(pemilih);
    
            return ResponseEntity.ok(new ApiResponse("success", "Suara Anda berhasil tercatat"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("error", "Terjadi kesalahan: " + e.getMessage()));
        }
    }

    @GetMapping("/pemilih/{nik}")
    public ResponseEntity<Pemilih> getPemilihByNik(@PathVariable String nik) {
        Pemilih pemilih = pemilihRepository.findByNik(nik);
        if (pemilih == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pemilih);
    }
}
