package com.reservation_system.domain.exceptions;

/**
 * Exception thrown when a requested resource is not found in the system.
 * This exception is used to distinguish between "resource not found" scenarios
 * (which should return HTTP 404) and business validation errors (which should return HTTP 400).
 */
public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
}
