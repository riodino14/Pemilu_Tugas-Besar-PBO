// CalonLegislatif.java
package com.tubesoop.pemilu.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class CalonLegislatif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Nama tidak boleh kosong")
    private String nama;

    @NotEmpty(message = "Partai tidak boleh kosong")
    private String partai;

    @NotEmpty(message = "Daerah Pemilihan tidak boleh kosong")
    private String daerahPemilihan;

    @Size(max = 500, message = "Visi dan Misi tidak boleh lebih dari 500 karakter")
    private String visiMisi;

    @NotNull(message = "Jenis harus dipilih")
    private String jenis; // Jenis: DPR, DPRD Provinsi, dll.

    private int totalSuara = 0; // Inisialisasi default

    private String foto;  // Path Foto

    @Transient
    private double persentaseSuara;

    // Constructor
    public CalonLegislatif() {}

    public CalonLegislatif(String nama, String partai, String daerahPemilihan, String visiMisi, String jenis) {
        this.nama = nama;
        this.partai = partai;
        this.daerahPemilihan = daerahPemilihan;
        this.visiMisi = visiMisi;
        this.jenis = jenis;
        this.totalSuara = 0; // Inisialisasi total suara
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPartai() {
        return partai;
    }

    public void setPartai(String partai) {
        this.partai = partai;
    }

    public String getDaerahPemilihan() {
        return daerahPemilihan;
    }

    public void setDaerahPemilihan(String daerahPemilihan) {
        this.daerahPemilihan = daerahPemilihan;
    }

    public String getVisiMisi() {
        return visiMisi;
    }

    public void setVisiMisi(String visiMisi) {
        this.visiMisi = visiMisi;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public int getTotalSuara() {
        return totalSuara;
    }

    public void setTotalSuara(int totalSuara) {
        this.totalSuara = totalSuara;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public double getPersentaseSuara() {
        return persentaseSuara;
    }

    public void setPersentaseSuara(double persentaseSuara) {
        this.persentaseSuara = persentaseSuara;
    }

    @Override
    public String toString() {
        return "CalonLegislatif{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", partai='" + partai + '\'' +
                ", daerahPemilihan='" + daerahPemilihan + '\'' +
                ", visiMisi='" + visiMisi + '\'' +
                ", jenis='" + jenis + '\'' +
                ", totalSuara=" + totalSuara +
                '}';
    }
}
