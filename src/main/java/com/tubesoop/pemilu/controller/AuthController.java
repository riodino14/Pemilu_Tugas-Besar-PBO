package com.tubesoop.pemilu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.tubesoop.pemilu.dto.LoginAdminRequest;
import com.tubesoop.pemilu.dto.LoginPemilihRequest;
import com.tubesoop.pemilu.entity.Admin;
import com.tubesoop.pemilu.entity.Pemilih;
import com.tubesoop.pemilu.repository.AdminRepository;
import com.tubesoop.pemilu.repository.PemilihRepository;
import com.tubesoop.pemilu.utils.ApiResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PemilihRepository pemilihRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerPemilih(@RequestBody Pemilih pemilih) {
        try {
            if (pemilihRepository.findByNik(pemilih.getNik()) != null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Pemilih dengan NIK ini sudah terdaftar."));
            }

            if (pemilihRepository.findByNoTelp(pemilih.getNoTelp()) != null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Pemilih dengan nomor telepon ini sudah terdaftar."));
            }

            if (pemilihRepository.findByEmail(pemilih.getEmail()) != null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Pemilih dengan email ini sudah terdaftar."));
            }

            // Hash password sebelum disimpan
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String hashedPassword = passwordEncoder.encode(pemilih.getPassword());
            pemilih.setPassword(hashedPassword);

            pemilihRepository.save(pemilih);
            return ResponseEntity.ok(new ApiResponse("success", "Pemilih berhasil didaftarkan."));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("error", "Gagal mendaftarkan pemilih."));
        }
    }

    @PostMapping("/login/admin")
    public ResponseEntity<ApiResponse> loginAdmin(@RequestBody LoginAdminRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        logger.info("Login sebagai admin dengan email: {}", email);

        Admin admin = adminRepository.findByEmail(email);
        if (admin != null && admin.getPassword().equals(password)) {
            return ResponseEntity.ok(new ApiResponse("success", "Admin login berhasil!"));
        } else {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("error", "Login admin gagal. Periksa email dan password Anda."));
        }
    }

    @PostMapping("/login/user")
    public ResponseEntity<ApiResponse> loginPemilih(@RequestBody LoginPemilihRequest request) {
        String nik = request.getNik();
        String email = request.getEmail();
        String password = request.getPassword();

        logger.info("Login sebagai pemilih dengan NIK: {}, dan Email: {}", nik, email);

        Pemilih pemilih = pemilihRepository.findByNik(nik);
        if (pemilih != null && pemilih.getEmail().equals(email)) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(password, pemilih.getPassword())) {
                logger.info("Login berhasil!");
                return ResponseEntity.ok(new ApiResponse("success", "Login berhasil!"));
            }
        }

        logger.warn("Login gagal. NIK, Email, atau Password tidak cocok.");
        return ResponseEntity.status(401)
                .body(new ApiResponse("error", "Login gagal. Periksa NIK, Email, dan Password Anda."));
    }
}
