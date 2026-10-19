package com.reservation_system.infrastructure.payment;

import java.math.BigDecimal;

public interface PaymentService {
    
    PaymentAuthorizationResult authorize(BigDecimal amount, String paymentMethodToken);
    
    PaymentCaptureResult capture(String authorizationId);
    
    PaymentRefundResult refund(String authorizationId, BigDecimal amount);
    
    class PaymentAuthorizationResult {
        private final boolean success;
        private final String authorizationId;
        private final String errorMessage;
        
        public PaymentAuthorizationResult(boolean success, String authorizationId, String errorMessage) {
            this.success = success;
            this.authorizationId = authorizationId;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getAuthorizationId() {
            return authorizationId;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    class PaymentCaptureResult {
        private final boolean success;
        private final String errorMessage;
        
        public PaymentCaptureResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    class PaymentRefundResult {
        private final boolean success;
        private final String errorMessage;
        
        public PaymentRefundResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
