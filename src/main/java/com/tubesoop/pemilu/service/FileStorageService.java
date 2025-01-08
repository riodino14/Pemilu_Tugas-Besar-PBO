package com.tubesoop.pemilu.service;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final String uploadDir = "uploads/";

    public String storeFile(MultipartFile file) {
        try {
            // Buat direktori jika belum ada
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                               Files.createDirectories(uploadPath);
            }

            // Simpan file
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage());
        }
    }
}


