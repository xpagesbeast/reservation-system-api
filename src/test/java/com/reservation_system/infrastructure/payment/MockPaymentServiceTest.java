package com.reservation_system.infrastructure.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MockPaymentServiceTest {
    
    private final MockPaymentService paymentService = new MockPaymentService();
    
    @Test
    void authorize_ValidData_ReturnsSuccess() {
        // Act
        PaymentService.PaymentAuthorizationResult result = paymentService.authorize(
                new BigDecimal("100.00"), "valid-token"
        );
        
        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getAuthorizationId());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void authorize_InvalidAmount_ReturnsFailure() {
        // Act
        PaymentService.PaymentAuthorizationResult result = paymentService.authorize(
                BigDecimal.ZERO, "valid-token"
        );
        
        // Assert
        assertFalse(result.isSuccess());
        assertNull(result.getAuthorizationId());
        assertEquals("Invalid amount", result.getErrorMessage());
    }
    
    @Test
    void authorise_FailToken_ReturnsFailure() {
        // Act
        PaymentService.PaymentAuthorizationResult result = paymentService.authorize(
                new BigDecimal("100.00"), "FAIL"
        );
        
        // Assert
        assertFalse(result.isSuccess());
        assertNull(result.getAuthorizationId());
        assertEquals("Payment authorization failed", result.getErrorMessage());
    }
    
    @Test
    void capture_ValidId_ReturnsSuccess() {
        // Act
        PaymentService.PaymentCaptureResult result = paymentService.capture("auth-123");
        
        // Assert
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void refund_ValidData_ReturnsSuccess() {
        // Act
        PaymentService.PaymentRefundResult result = paymentService.refund("auth-123", new BigDecimal("100.00"));
        
        // Assert
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
    }
}
