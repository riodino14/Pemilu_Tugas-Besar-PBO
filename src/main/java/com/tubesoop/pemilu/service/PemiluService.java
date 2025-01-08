package com.tubesoop.pemilu.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.tubesoop.pemilu.entity.CalonLegislatif;
import com.tubesoop.pemilu.entity.Pemilih;
import com.tubesoop.pemilu.repository.CalonLegislatifRepository;
import com.tubesoop.pemilu.repository.PemilihRepository;

@Service
public class PemiluService {

    @Autowired
    private CalonLegislatifRepository calonRepository;

    @Autowired
    private PemilihRepository pemilihRepository;

    /**
     * Melakukan voting untuk pemilih.
     * @param nik NIK pemilih.
     * @param idCalon ID calon legislatif yang dipilih.
     */
    public void voting(String nik, Long idCalon) {
        Pemilih pemilih = pemilihRepository.findById(nik)
                .orElseThrow(() -> new RuntimeException("Pemilih tidak ditemukan"));
        if (pemilih.isSudahMemilih()) {
            throw new RuntimeException("Pemilih sudah melakukan voting");
        }

        CalonLegislatif calon = calonRepository.findById(idCalon)
                .orElseThrow(() -> new RuntimeException("Calon legislatif tidak ditemukan"));

        // Update total suara calon
        calon.setTotalSuara(calon.getTotalSuara() + 1);

        // Tandai pemilih telah memilih
        pemilih.setSudahMemilih(true);

        // Simpan perubahan ke database
        calonRepository.save(calon);
        pemilihRepository.save(pemilih);
    }

    /**
     * Mendapatkan daftar semua calon legislatif.
     * @return List CalonLegislatif
     */
    
    public List<CalonLegislatif> getAllCalon() {
        return calonRepository.findAll();
    }

    /**
     * Menambahkan calon legislatif baru.
     * @param calonLegislatif Data calon legislatif baru.
     */
    public void addCalon(CalonLegislatif calon) {
        if (calonRepository.existsByNama(calon.getNama())) {
            throw new DataIntegrityViolationException("Calon dengan nama tersebut sudah ada.");
        }
        calonRepository.save(calon);
    }
    
    public void deleteCalon(Long id) {
        calonRepository.deleteById(id);
    }
    
     // Tambahkan metode ini untuk memeriksa calon berdasarkan nama
     public boolean existsByNama(String nama) {
        return calonRepository.existsByNama(nama);
    }
    public Optional<CalonLegislatif> findById(Long id) {
        return calonRepository.findById(id);
    }
    public void updateCalon(CalonLegislatif calon) {
        calonRepository.save(calon);
    }

    
}
