package com.reservation_system.application.services;

import com.reservation_system.domain.Customer;
import com.reservation_system.infrastructure.persistence.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    void findOrCreateCustomer_ExistingCustomer_ReturnsExisting() {
        // Arrange
        String email = "test@example.com";
        Customer existingCustomer = new Customer("John Doe", email, "555-1234");
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(existingCustomer));
        
        // Act
        Customer result = customerService.findOrCreateCustomer("John Doe", email, "555-1234");
        
        // Assert
        assertEquals(existingCustomer, result);
        verify(customerRepository, never()).save(any());
    }
    
    @Test
    void findOrCreateCustomer_NewCustomer_CreatesAndReturns() {
        // Arrange
        String email = "new@example.com";
        Customer newCustomer = new Customer("Jane Doe", email, "555-5678");
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenReturn(newCustomer);
        
        // Act
        Customer result = customerService.findOrCreateCustomer("Jane Doe", email, "555-5678");
        
        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).save(any());
    }
    
    @Test
    void findByEmail_ExistingEmail_ReturnsCustomer() {
        // Arrange
        String email = "test@example.com";
        Customer customer = new Customer("John Doe", email, "555-1234");
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        
        // Act
        Optional<Customer> result = customerService.findByEmail(email);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
    }
    
    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        Optional<Customer> result = customerService.findByEmail(email);
        
        // Assert
        assertFalse(result.isPresent());
    }
}
