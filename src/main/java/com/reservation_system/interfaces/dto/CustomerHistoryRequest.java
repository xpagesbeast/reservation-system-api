package com.reservation_system.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class CustomerHistoryRequest {
    
    @NotNull(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    public CustomerHistoryRequest() {}
    
    public CustomerHistoryRequest(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
