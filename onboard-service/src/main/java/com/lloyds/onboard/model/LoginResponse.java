package com.lloyds.onboard.model;

public class LoginResponse {
    private String status;
    private String username;
    private String role;

    public LoginResponse(String status, String username, String role) {
        this.status = status;
        this.username = username;
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
