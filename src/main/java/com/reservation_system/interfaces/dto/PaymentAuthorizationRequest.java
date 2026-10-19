package com.reservation_system.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentAuthorizationRequest {
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method token is required")
    private String paymentMethodToken;
    
    public PaymentAuthorizationRequest() {}
    
    public PaymentAuthorizationRequest(BigDecimal amount, String paymentMethodToken) {
        this.amount = amount;
        this.paymentMethodToken = paymentMethodToken;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentMethodToken() {
        return paymentMethodToken;
    }
    
    public void setPaymentMethodToken(String paymentMethodToken) {
        this.paymentMethodToken = paymentMethodToken;
    }
}
