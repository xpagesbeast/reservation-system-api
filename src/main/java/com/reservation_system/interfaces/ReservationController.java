package com.reservation_system.interfaces;

import com.reservation_system.application.TimeZoneService;
import com.reservation_system.application.services.ReservationService;
import com.reservation_system.domain.Lane;
import com.reservation_system.domain.Reservation;
import com.reservation_system.domain.enums.ReservationStatus;
import com.reservation_system.infrastructure.persistence.LaneRepository;
import com.reservation_system.interfaces.dto.CreateReservationRequest;
import com.reservation_system.interfaces.dto.CustomerDTO;
import com.reservation_system.interfaces.dto.ReservationListResponse;
import com.reservation_system.interfaces.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing reservations.
 * Provides endpoints for creating, canceling, and retrieving reservations.
 */
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    
    private final ReservationService reservationService;
    private final LaneRepository laneRepository;
    private final TimeZoneService timeZoneService;
    private final BigDecimal depositAmount;
    
    public ReservationController(ReservationService reservationService,
                                 LaneRepository laneRepository,
                                 TimeZoneService timeZoneService,
                                 @Value("${reservation.deposit-amount}") BigDecimal depositAmount) {
        this.reservationService = reservationService;
        this.laneRepository = laneRepository;
        this.timeZoneService = timeZoneService;
        this.depositAmount = depositAmount;
    }
    
    /**
     * Create a new reservation.
     * 
     * @param request Reservation creation request with customer details, date, time, lanes, and payment info
     * @return ReservationResponse if successful, error response if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        try {
            CustomerDTO customerDTO = request.getCustomer();
            
            Reservation reservation = reservationService.createReservation(
                    customerDTO.getName(),
                    customerDTO.getEmail(),
                    customerDTO.getPhone(),
                    request.getDate(),
                    request.getTime(),
                    request.getDurationHours(),
                    request.getLaneIds(),
                    request.getCancellationPolicyAccepted(),
                    request.getPaymentMethodToken(),
                    request.getNote()
            );
            
            ReservationResponse response = mapToResponse(reservation);
            
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                response.setConfirmationMessage("Reservation confirmed. Deposit of $" + depositAmount + " has been captured.");
            } else if (reservation.getStatus() == ReservationStatus.PENDING_APPROVAL) {
                response.setConfirmationMessage("Reservation pending management approval for extended hours.");
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Cancel an existing reservation.
     * 
     * @param id Reservation ID to cancel
     * @return ReservationResponse if successful, 404 if reservation not found
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.cancelReservation(id);
            ReservationResponse response = mapToResponse(reservation);
            response.setConfirmationMessage("Reservation cancelled.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get reservation details by ID.
     * 
     * @param id Reservation ID to retrieve
     * @return ReservationResponse if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToResponse(reservation));
    }
    
    /**
     * Query reservations with optional date, status, and customer filters with pagination.
     * 
     * @param year Optional year filter (Aspen time)
     * @param month Optional month filter (Aspen time)
     * @param day Optional day filter (Aspen time)
     * @param status Optional status filter (comma-separated values)
     * @param customerEmail Optional customer email filter
     * @param page Page number (default 0)
     * @param size Page size (default 50)
     * @return ReservationListResponse with reservations and pagination metadata
     */
    @GetMapping
    public ResponseEntity<?> queryReservations(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        // Parse status filter
        List<ReservationStatus> statuses = null;
        if (status != null && !status.isEmpty()) {
            statuses = List.of(status.split(",")).stream()
                .map(String::trim)
                .map(ReservationStatus::valueOf)
                .collect(Collectors.toList());
        }
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size);
        
        // Query reservations
        Page<Reservation> reservationPage = reservationService.queryReservations(
            year, month, day, statuses, customerEmail, pageable);
        
        // Convert to response DTOs
        List<ReservationResponse> reservationResponses = reservationPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        ReservationListResponse response = new ReservationListResponse(reservationResponses, reservationPage);
        return ResponseEntity.ok(response);
    }
    
    private ReservationResponse mapToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        
        CustomerDTO customerDTO = new CustomerDTO(
                reservation.getCustomer().getName(),
                reservation.getCustomer().getEmail(),
                reservation.getCustomer().getPhone()
        );
        response.setCustomer(customerDTO);
        
        response.setReservationDate(reservation.getReservationDate());
        
        // Convert UTC to Aspen time for display
        LocalDateTime aspenTime = timeZoneService.convertUtcToAspen(reservation.getReservationTimeUtc());
        response.setReservationTime(aspenTime);
        
        response.setDurationHours(reservation.getDurationHours());
        response.setStatus(reservation.getStatus());
        
        Set<Long> laneIds = reservation.getLanes().stream()
                .map(Lane::getId)
                .collect(Collectors.toSet());
        response.setLaneIds(laneIds);
        
        response.setDepositAmount(depositAmount);
        response.setExtendedHoursRequested(reservation.getExtendedHoursRequested());
        response.setExtendedHoursApproved(reservation.getExtendedHoursApproved());
        response.setNote(reservation.getNote());
        
        return response;
    }
    
    private static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}
