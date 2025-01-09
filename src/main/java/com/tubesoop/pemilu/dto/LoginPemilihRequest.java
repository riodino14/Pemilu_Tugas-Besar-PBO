package com.tubesoop.pemilu.dto;

public class LoginPemilihRequest {
    private String nik;  // NIK untuk pemilih
    private String email;  // Email pemilih
    private String password;  // Password pemilih
    
    public String getNik() {
        return nik;
    }
    public void setNik(String nik) {
        this.nik = nik;
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
    private String role;
    private String identifier;

    // Getter dan Setter
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
