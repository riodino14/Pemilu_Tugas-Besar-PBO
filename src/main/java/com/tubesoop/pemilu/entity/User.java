package com.tubesoop.pemilu.entity;


import jakarta.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class User {
    private String nama;
    private String email;
    private String password; 

    public User() {} // Constructor default

    public User(String nama, String email, String password) {
        this.nama = nama;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // public abstract String getRole();
}
