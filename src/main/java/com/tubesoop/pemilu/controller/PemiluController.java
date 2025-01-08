package com.tubesoop.pemilu.controller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tubesoop.pemilu.dto.LoginRequest;
import com.tubesoop.pemilu.entity.Admin;
import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.entity.Pemilih;

import com.tubesoop.pemilu.repository.AdminRepository;
import com.tubesoop.pemilu.repository.CalonLegislatifRepository;
import com.tubesoop.pemilu.repository.PemilihRepository;

import com.tubesoop.pemilu.service.PemiluService;
import com.tubesoop.pemilu.utils.ApiResponse;
import com.tubesoop.pemilu.dto.VoteRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pemilu")
public class PemiluController {

    private static final Logger logger = LoggerFactory.getLogger(PemiluController.class);

    @Autowired
    private PemiluService pemiluService;

    private static final String UPLOAD_DIR = "uploads/";
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PemilihRepository pemilihRepository;
    @Autowired
    private CalonLegislatifRepository calonLegislatifRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerPemilih(@RequestBody Pemilih pemilih) {
        try {
            if (pemilihRepository.findByNik(pemilih.getNik()) != null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Pemilih dengan NIK ini sudah terdaftar."));
            }
            pemilihRepository.save(pemilih);
            return ResponseEntity.ok(new ApiResponse("success", "Pemilih berhasil didaftarkan."));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("error", "Gagal mendaftarkan pemilih."));
        }
    }
    // Login Controller
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        String role = request.getRole();
        String identifier = request.getIdentifier();
        String password = request.getPassword();

        // Cek login untuk Admin
        if ("admin".equals(role)) {
            logger.info("Login sebagai admin dengan email: {}", identifier);

            // Cari admin berdasarkan email
            Admin admin = adminRepository.findByEmail(identifier);
            if (admin != null && admin.getPassword().equals(password)) {
                return ResponseEntity.ok(new ApiResponse("success", "Admin login berhasil!"));
            } else {
                return ResponseEntity.status(401)
                        .body(new ApiResponse("error", "Login admin gagal. Periksa email dan password Anda."));
            }
        }

        // Cek login untuk Pemilih berdasarkan NIK
        if ("pemilih".equals(role)) {
            logger.info("Login sebagai pemilih dengan NIK: {}", identifier);

            // Cari pemilih berdasarkan NIK
            Pemilih pemilih = pemilihRepository.findByNik(identifier);
            if (pemilih != null) {
                return ResponseEntity.ok(new ApiResponse("success", "Pemilih login berhasil!"));
            } else {
                return ResponseEntity.status(401)
                        .body(new ApiResponse("error", "Login pemilih gagal. Periksa NIK Anda."));
            }
        }

        return ResponseEntity.status(400).body(new ApiResponse("error", "Role tidak dikenali."));
    }

    // Menambah calon legislatif dengan validasi duplikasi nama
    @PostMapping("/calon")
    public ResponseEntity<ApiResponse> addCalon(@Valid @RequestBody CalonLegislatif calonLegislatif) {
        logger.info("Menerima request untuk menambahkan calon legislatif: {}", calonLegislatif.getNama());
        try {
            if (pemiluService.existsByNama(calonLegislatif.getNama())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Calon dengan nama tersebut sudah ada."));
            }
            pemiluService.addCalon(calonLegislatif);
            logger.info("Calon legislatif berhasil ditambahkan: {}", calonLegislatif.getNama());
            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil ditambahkan"));
        } catch (DataIntegrityViolationException e) {
            logger.error("Data tidak valid: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Data tidak valid"));
        } catch (Exception e) {
            logger.error("Gagal menambahkan calon: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Terjadi kesalahan saat menambahkan calon"));
        }
    }
   
    @PostMapping("/vote")
    public ResponseEntity<ApiResponse> vote(@RequestBody @Valid VoteRequest voteRequest) {
        try {
            String nik = voteRequest.getNik();
            Long candidateId = voteRequest.getCandidateId();

            Pemilih pemilih = pemilihRepository.findByNik(nik);
            if (pemilih == null) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Pemilih tidak ditemukan."));
            }

            if (pemilih.isSudahMemilih()) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Anda sudah memberikan suara."));
            }

            CalonLegislatif calon = calonLegislatifRepository.findById(candidateId)
                    .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan untuk ID: " + candidateId));

            calon.setTotalSuara(calon.getTotalSuara() + 1);
            pemilih.setSudahMemilih(true);

            pemilihRepository.save(pemilih);
            calonLegislatifRepository.save(calon);

            return ResponseEntity.ok(new ApiResponse("success", "Suara Anda berhasil tercatat."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "Terjadi kesalahan: " + e.getMessage()));
        }
    }

    @GetMapping("/rekapitulasi")
    public ResponseEntity<List<CalonLegislatif>> getRekapitulasiSuara() {
        List<CalonLegislatif> candidates = calonLegislatifRepository.findAll();
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
    @GetMapping("/rekapitulasi-daerah/{daerahPemilihan}")
    public ResponseEntity<List<Map<String, Object>>> getRekapitulasiByDaerah(@PathVariable String daerahPemilihan) {
        try {
            List<CalonLegislatif> candidates = calonLegislatifRepository.findByDaerahPemilihan(daerahPemilihan);
            if (candidates.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Hitung total suara untuk daerah pemilihan
            int totalVotes = candidates.stream().mapToInt(CalonLegislatif::getTotalSuara).sum();

            // Format data kandidat dengan persentase suara
            List<Map<String, Object>> rekapitulasi = candidates.stream().map(candidate -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", candidate.getId());
                data.put("nama", candidate.getNama());
                data.put("partai", candidate.getPartai());
                data.put("daerahPemilihan", candidate.getDaerahPemilihan());
                data.put("visiMisi", candidate.getVisiMisi());
                data.put("foto", candidate.getFoto());
                data.put("totalSuara", candidate.getTotalSuara());
                data.put("persentaseSuara", totalVotes > 0 ? (double) candidate.getTotalSuara() / totalVotes * 100 : 0);
                return data;
            }).toList();

            return ResponseEntity.ok(rekapitulasi);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Menambah calon legislatif dengan foto
    // Endpoint untuk menambahkan Calon Legislatif
    @PostMapping(value = "/calon/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addCalonWithPhoto(
            @RequestParam("nama") String nama,
            @RequestParam("partai") String partai,
            @RequestParam("daerahPemilihan") String daerahPemilihan,
          
            @RequestParam("visiMisi") String visiMisi,
            @RequestParam("jenis") String jenis,
            @RequestParam("foto") MultipartFile foto) {

        logger.info("Menerima request untuk menambahkan calon legislatif dengan foto: {}", nama);
        try {
            if (pemiluService.existsByNama(nama)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Calon dengan nama tersebut sudah ada."));
            }

            if (foto.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Foto tidak boleh kosong"));
            }

            String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(foto.getInputStream(), uploadPath.resolve(fileName));

            CalonLegislatif calon = new CalonLegislatif(nama, partai, daerahPemilihan, visiMisi, jenis);
            calon.setFoto(fileName);
            pemiluService.addCalon(calon);

            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil ditambahkan dengan foto"));
        } catch (IOException e) {
            logger.error("Gagal menyimpan foto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal menyimpan foto"));
        } catch (Exception e) {
            logger.error("Gagal menambahkan calon: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal menambahkan calon"));
        }
    }

    // Endpoint untuk mengakses file gambar yang diupload
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/calon")
    public ResponseEntity<List<CalonLegislatif>> getAllCalon() {
        logger.info("Mengambil semua data calon legislatif");
        List<CalonLegislatif> calonList = pemiluService.getAllCalon();
        return ResponseEntity.ok(calonList);
    }

     // Endpoint untuk mengedit Calon Legislatif
     @PutMapping("/calon/{id}")
     public ResponseEntity<ApiResponse> editCalon(
             @PathVariable Long id,
             @RequestParam(value = "nama", required = false) String nama,
             @RequestParam(value = "partai", required = false) String partai,
             @RequestParam(value = "daerahPemilihan", required = false) String daerahPemilihan,
            
             @RequestParam(value = "visiMisi", required = false) String visiMisi,
             @RequestParam(value = "jenis", required = false) String jenis,
             @RequestParam(value = "foto", required = false) MultipartFile foto) {
 
         logger.info("Menerima request untuk mengedit calon legislatif dengan ID: {}", id);
 
         try {
             CalonLegislatif calon = pemiluService.findById(id)
                     .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan"));
 
             if (nama != null && !nama.isEmpty()) calon.setNama(nama);
             if (partai != null && !partai.isEmpty()) calon.setPartai(partai);
             if (daerahPemilihan != null && !daerahPemilihan.isEmpty()) calon.setDaerahPemilihan(daerahPemilihan);
             if (visiMisi != null && !visiMisi.isEmpty()) calon.setVisiMisi(visiMisi);
             if (jenis != null && !jenis.isEmpty()) calon.setJenis(jenis);
 
             if (foto != null && !foto.isEmpty()) {
                 String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                 Path uploadPath = Paths.get(UPLOAD_DIR);
 
                 if (!Files.exists(uploadPath)) {
                     Files.createDirectories(uploadPath);
                 }
 
                 Files.copy(foto.getInputStream(), uploadPath.resolve(fileName));
                 calon.setFoto(fileName);
             }
 
             pemiluService.updateCalon(calon);
             logger.info("Calon berhasil diperbarui: {}", calon);
 
             return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil diperbarui"));
         } catch (IOException e) {
             logger.error("Gagal menyimpan foto: {}", e.getMessage());
             return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal menyimpan foto"));
         } catch (Exception e) {
             logger.error("Gagal mengedit calon: {}", e.getMessage());
             return ResponseEntity.badRequest().body(new ApiResponse("error", "Gagal mengedit calon"));
         }
     }

    @DeleteMapping("/calon/{id}")
    public ResponseEntity<ApiResponse> deleteCalon(@PathVariable Long id) {
        logger.info("Request untuk menghapus calon legislatif dengan ID: {}", id);
        try {
            pemiluService.deleteCalon(id);
            logger.info("Calon legislatif dengan ID {} berhasil dihapus", id);
            return ResponseEntity.ok(new ApiResponse("success", "Calon berhasil dihapus"));
        } catch (RuntimeException e) {
            logger.error("Gagal menghapus calon: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Calon tidak ditemukan"));
        }
    }

    @GetMapping("/calon/{id}")
    public ResponseEntity<CalonLegislatif> getCalonById(@PathVariable Long id) {
        try {
            CalonLegislatif calon = pemiluService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan"));
            return ResponseEntity.ok(calon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/calon-daerah/{daerahPemilihan}")
    public ResponseEntity<List<CalonLegislatif>> getCandidatesByDaerah(@PathVariable String daerahPemilihan) {
        logger.info("Menerima permintaan untuk daerah pemilihan: {}", daerahPemilihan);

        // Cari kandidat berdasarkan daerah pemilihan
        List<CalonLegislatif> candidates = calonLegislatifRepository.findByDaerahPemilihan(daerahPemilihan);

        if (candidates.isEmpty()) {
            logger.warn("Tidak ada kandidat untuk daerah pemilihan: {}", daerahPemilihan);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/vote/batch")
    public ResponseEntity<ApiResponse> batchVote(@RequestBody @Valid List<VoteRequest> voteRequests) {
        if (voteRequests.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", "Tidak ada suara yang dikirim"));
        }
    
        try {
            // Asumsikan semua VoteRequest berasal dari pemilih yang sama
            String nik = voteRequests.get(0).getNik();
            Pemilih pemilih = pemilihRepository.findByNik(nik);
            
            if (pemilih == null) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Pemilih tidak ditemukan: " + nik));
            }
    
            if (pemilih.isSudahMemilih()) {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "Pemilih dengan NIK " + nik + " sudah memberikan suara."));
            }
    
            // Proses semua suara terlebih dahulu
            for (VoteRequest voteRequest : voteRequests) {
                Long candidateId = voteRequest.getCandidateId();
    
                CalonLegislatif calon = calonLegislatifRepository.findById(candidateId)
                        .orElseThrow(() -> new RuntimeException("Calon tidak ditemukan untuk ID: " + candidateId));
    
                calon.setTotalSuara(calon.getTotalSuara() + 1);
                calonLegislatifRepository.save(calon);
            }
    
            // Setelah semua suara diproses, tandai pemilih sudah memilih
            pemilih.setSudahMemilih(true);
            pemilihRepository.save(pemilih);
    
            return ResponseEntity.ok(new ApiResponse("success", "Semua suara berhasil tercatat."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "Terjadi kesalahan: " + e.getMessage()));
        }
    }
    

}
