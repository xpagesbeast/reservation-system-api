package com.reservation_system.infrastructure.payment;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Profile({"local", "h2"})
public class MockPaymentService implements PaymentService {
    
    @Override
    public PaymentAuthorizationResult authorize(BigDecimal amount, String paymentMethodToken) {
        // Mock logic: succeed for valid data, fail for invalid
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentAuthorizationResult(false, null, "Invalid amount");
        }
        
        if (paymentMethodToken == null || paymentMethodToken.trim().isEmpty()) {
            return new PaymentAuthorizationResult(false, null, "Invalid payment method token");
        }
        
        // Simulate failure for specific test cases
        if (paymentMethodToken.equals("FAIL")) {
            return new PaymentAuthorizationResult(false, null, "Payment authorization failed");
        }
        
        String authorizationId = "auth_" + UUID.randomUUID().toString();
        return new PaymentAuthorizationResult(true, authorizationId, null);
    }
    
    @Override
    public PaymentCaptureResult capture(String authorizationId) {
        if (authorizationId == null || authorizationId.trim().isEmpty()) {
            return new PaymentCaptureResult(false, "Invalid authorization ID");
        }
        
        if (authorizationId.equals("FAIL")) {
            return new PaymentCaptureResult(false, "Payment capture failed");
        }
        
        return new PaymentCaptureResult(true, null);
    }
    
    @Override
    public PaymentRefundResult refund(String authorizationId, BigDecimal amount) {
        if (authorizationId == null || authorizationId.trim().isEmpty()) {
            return new PaymentRefundResult(false, "Invalid authorization ID");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentRefundResult(false, "Invalid refund amount");
        }
        
        if (authorizationId.equals("FAIL")) {
            return new PaymentRefundResult(false, "Payment refund failed");
        }
        
        return new PaymentRefundResult(true, null);
    }
}
