package com.tubesoop.pemilu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tubesoop.pemilu.dto.VoteRequest;
import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.entity.Pemilih;
import com.tubesoop.pemilu.repository.CalonLegislatifRepository;
import com.tubesoop.pemilu.repository.PemilihRepository;
import com.tubesoop.pemilu.utils.ApiResponse;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ApiResponse> voteBatch(@Valid @RequestBody List<VoteRequest> voteRequests) {
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
    public ResponseEntity<ApiResponse> vote(@Valid @RequestBody VoteRequest voteRequest) {
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
    //REVISI
    // Penanganan Exception untuk ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", exception.getMessage()));
    }

    // Penanganan Exception untuk MethodArgumentNotValidException (validasi @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", errors.toString()));
    }

    // Penanganan Exception untuk HttpMessageNotReadableException (JSON parse error)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        String message = "Terjadi kesalahan, tipe data long/integer";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", message));
    }

    // Penanganan Exception umum lainnya
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Terjadi kesalahan: " + exception.getMessage()));
    }
}
