package com.tubesoop.pemilu.entity;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Pemilih extends User {
    @Id
    private String nik; // NIK sebagai ID unik
    private boolean sudahMemilih;

    public Pemilih() {} // Constructor default

    public Pemilih(String nik, String nama, String email) {
        super(nama, email);
        this.nik = nik;
        this.sudahMemilih = false;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public boolean isSudahMemilih() {
        return sudahMemilih;
    }

    public void setSudahMemilih(boolean sudahMemilih) {
        this.sudahMemilih = sudahMemilih;
    }

    @Override
    public String getRole() {
        return "Pemilih";
    }
}
