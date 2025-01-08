package com.tubesoop.pemilu.entity;


import jakarta.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class User {
    private String nama;
    private String email;

    public User() {} // Constructor default

    public User(String nama, String email) {
        this.nama = nama;
        this.email = email;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public abstract String getRole();
}
