package com.tubesoop.pemilu.dto;

public class LoginRequest {
    private String role;  // "admin" atau "pemilih"
    private String identifier;  // Email untuk admin, NIK untuk pemilih
    private String password;  // Password (plaintext) untuk admin

    // Constructor, Getter dan Setter

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
