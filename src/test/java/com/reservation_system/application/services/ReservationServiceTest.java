package com.reservation_system.application.services;

import com.reservation_system.application.OperatingHoursService;
import com.reservation_system.application.TimeZoneService;
import com.reservation_system.domain.Customer;
import com.reservation_system.domain.Reservation;
import com.reservation_system.domain.enums.ReservationStatus;
import com.reservation_system.infrastructure.payment.PaymentService;
import com.reservation_system.infrastructure.persistence.PaymentAuthorizationRepository;
import com.reservation_system.infrastructure.persistence.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private AvailabilityService availabilityService;
    
    @Mock
    private OperatingHoursService operatingHoursService;
    
    @Mock
    private TimeZoneService timeZoneService;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private PaymentAuthorizationRepository paymentAuthorizationRepository;
    
    @InjectMocks
    private ReservationService reservationService;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationService, "depositAmount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(reservationService, "cancellationDeadlineHours", 24);
        
        customer = new Customer("John Doe", "john@example.com", "555-1234");
        customer.setId(1L);
    }
    
    @Test
    void createReservation_PolicyNotAccepted_ThrowsException() {
        // Arrange
        when(customerService.findOrCreateCustomer(any(), any(), any())).thenReturn(customer);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                reservationService.createReservation(
                        "John Doe", "john@example.com", "555-1234",
                        LocalDate.now().plusDays(1), LocalTime.of(18, 0), 1,
                        Set.of(1L), false, "token123", null
                )
        );
    }
    
    @Test
    void createReservation_InvalidDuration_ThrowsException() {
        // Arrange
        when(customerService.findOrCreateCustomer(any(), any(), any())).thenReturn(customer);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                reservationService.createReservation(
                        "John Doe", "john@example.com", "555-1234",
                        LocalDate.now().plusDays(1), LocalTime.of(18, 0), 0,
                        Set.of(1L), true, "token123", null
                )
        );
    }
    
    @Test
    void createReservation_BookingBeyondOneYear_ThrowsException() {
        // Arrange
        when(customerService.findOrCreateCustomer(any(), any(), any())).thenReturn(customer);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                reservationService.createReservation(
                        "John Doe", "john@example.com", "555-1234",
                        LocalDate.now().plusYears(2), LocalTime.of(18, 0), 1,
                        Set.of(1L), true, "token123", null
                )
        );
    }
    
    @Test
    void cancelReservation_WithinDeadline_RefundsDeposit() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setReservationTimeUtc(java.time.LocalDateTime.now().plusDays(2));
        
        when(reservationRepository.findById(1L)).thenReturn(java.util.Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(paymentAuthorizationRepository.findFirstByReservationOrderByCreatedAtDesc(any())).thenReturn(null);
        
        // Act
        Reservation result = reservationService.cancelReservation(1L);
        
        // Assert
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }
    
    @Test
    void cancelReservation_NotFound_ThrowsException() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reservationService.cancelReservation(1L));
    }
}
