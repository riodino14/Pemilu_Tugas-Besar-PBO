package com.tubesoop.pemilu.dto;

public class LoginAdminRequest {
    private String email;  // Email untuk admin
    private String password;  // Password untuk admin

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
}
