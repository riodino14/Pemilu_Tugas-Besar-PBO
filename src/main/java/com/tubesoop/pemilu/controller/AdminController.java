// AdminController.java
package com.tubesoop.pemilu.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.service.PemiluService;
import com.tubesoop.pemilu.utils.ApiResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PemiluService pemiluService;

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/calon")
    public ResponseEntity<ApiResponse> addCalon(
        @RequestParam("nama") String nama,
        @RequestParam("partai") String partai,
        @RequestParam("daerahPemilihan") String daerahPemilihan,
        @RequestParam("visiMisi") String visiMisi,
        @RequestParam("jenis") String jenis,
        @RequestParam(value = "foto", required = false) MultipartFile foto) {

        try {
            if (nama == null || nama.trim().isEmpty() || partai == null || partai.trim().isEmpty() ||
                daerahPemilihan == null || daerahPemilihan.trim().isEmpty() ||
                visiMisi == null || visiMisi.trim().isEmpty() ||
                jenis == null || jenis.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Semua kolom wajib diisi"));
            }

            CalonLegislatif calon = new CalonLegislatif(nama, partai, daerahPemilihan, visiMisi, jenis);

            if (foto != null && !foto.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                Files.copy(foto.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                calon.setFoto(fileName);
            }

            pemiluService.addCalon(calon);
            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil ditambahkan"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Calon dengan nama tersebut sudah ada."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", "Gagal menyimpan foto."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", "Terjadi kesalahan saat menambahkan calon."));
        }
    }

    @PutMapping("/calon/{id}")
    public ResponseEntity<ApiResponse> editCalon(
        @PathVariable Long id,
        @RequestParam(value = "nama", required = false) String nama,
        @RequestParam(value = "partai", required = false) String partai,
        @RequestParam(value = "daerahPemilihan", required = false) String daerahPemilihan,
        @RequestParam(value = "visiMisi", required = false) String visiMisi,
        @RequestParam(value = "jenis", required = false) String jenis,
        @RequestParam(value = "foto", required = false) MultipartFile foto) {

        try {
            CalonLegislatif calon = pemiluService.findById(id)
                .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan"));

            if (nama != null) calon.setNama(nama);
            if (partai != null) calon.setPartai(partai);
            if (daerahPemilihan != null) calon.setDaerahPemilihan(daerahPemilihan);
            if (visiMisi != null) calon.setVisiMisi(visiMisi);
            if (jenis != null) calon.setJenis(jenis);

            if (foto != null && !foto.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                Files.copy(foto.getInputStream(), uploadPath.resolve(fileName));
                calon.setFoto(fileName);
            }

            pemiluService.updateCalon(calon);
            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil diperbarui"));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal menyimpan foto"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal mengedit calon"));
        }
    }

    @DeleteMapping("/calon/{id}")
    public ResponseEntity<ApiResponse> deleteCalon(@PathVariable Long id) {
        try {
            pemiluService.deleteCalon(id);
            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Calon tidak ditemukan"));
        }
    }
    

    @GetMapping("/calon/{id}")
    public ResponseEntity<CalonLegislatif> getCalonById(@PathVariable Long id) {
        CalonLegislatif calon = pemiluService.findById(id)
                .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan"));
        return ResponseEntity.ok(calon);
    }

    @GetMapping("/calon")
    public ResponseEntity<List<CalonLegislatif>> getAllCalon() {
        List<CalonLegislatif> calonList = pemiluService.getAllCalon();
        return ResponseEntity.ok(calonList);
    }
}
