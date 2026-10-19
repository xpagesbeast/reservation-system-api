package com.reservation_system.interfaces;

import com.reservation_system.application.TimeZoneService;
import com.reservation_system.application.services.ManagementService;
import com.reservation_system.application.services.ReservationService;
import com.reservation_system.domain.Lane;
import com.reservation_system.domain.Reservation;
import com.reservation_system.interfaces.dto.CreateLanesRequest;
import com.reservation_system.interfaces.dto.CustomerDTO;
import com.reservation_system.interfaces.dto.LaneCreationResult;
import com.reservation_system.interfaces.dto.LaneDTO;
import com.reservation_system.interfaces.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for management operations.
 * Provides endpoints for approving/rejecting extended hours and blocking/unblocking lanes.
 */
@RestController
@RequestMapping("/api/v1/management")
public class ManagementController {
    
    private final ReservationService reservationService;
    private final ManagementService managementService;
    private final TimeZoneService timeZoneService;
    private final BigDecimal depositAmount;
    
    public ManagementController(ReservationService reservationService,
                               ManagementService managementService,
                               TimeZoneService timeZoneService,
                               @Value("${reservation.deposit-amount}") BigDecimal depositAmount) {
        this.reservationService = reservationService;
        this.managementService = managementService;
        this.timeZoneService = timeZoneService;
        this.depositAmount = depositAmount;
    }
    
    /**
     * Approve extended hours request for a reservation.
     * 
     * @param id Reservation ID to approve
     * @return ReservationResponse if successful, error response if validation fails
     */
    @PostMapping("/reservations/{id}/approve")
    public ResponseEntity<?> approveExtendedHours(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.approveExtendedHours(id);
            return ResponseEntity.ok(mapToResponse(reservation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Reject extended hours request for a reservation.
     * 
     * @param id Reservation ID to reject
     * @return ReservationResponse if successful, error response if validation fails
     */
    @PostMapping("/reservations/{id}/reject")
    public ResponseEntity<?> rejectExtendedHours(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.rejectExtendedHours(id);
            return ResponseEntity.ok(mapToResponse(reservation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Block a lane from being available for reservations.
     * 
     * @param id Lane ID to block
     * @return LaneDTO if successful, error response if validation fails
     */
    @PostMapping("/lanes/{id}/block")
    public ResponseEntity<?> blockLane(@PathVariable Long id) {
        try {
            Lane lane = managementService.blockLane(id);
            return ResponseEntity.ok(new LaneDTO(lane.getId(), lane.getLaneNumber()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Unblock a lane to make it available for reservations.
     *
     * @param id Lane ID to unblock
     * @return LaneDTO if successful, error response if validation fails
     */
    @PostMapping("/lanes/{id}/unblock")
    public ResponseEntity<?> unblockLane(@PathVariable Long id) {
        try {
            Lane lane = managementService.unblockLane(id);
            return ResponseEntity.ok(new LaneDTO(lane.getId(), lane.getLaneNumber()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Create lanes in bulk.
     * TODO: Add JWT authentication - @PreAuthorize placeholder for future implementation
     *
     * @param request Lane creation request with array of lane objects
     * @return Array of lane creation results with status and error messages
     */
    //@PreAuthorize("hasRole('ADMIN')") // TODO: Placeholder for future JWT implementation
    @PostMapping("/lanes")
    public ResponseEntity<?> createLanes(@Valid @RequestBody CreateLanesRequest request) {
        try {
            var results = managementService.createLanes(request.getLanes());
            return ResponseEntity.status(HttpStatus.CREATED).body(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/lanes")
    public ResponseEntity<List<LaneCreationResult>> getAllLanes() {
        return ResponseEntity.ok(managementService.getAllLanes());
    }

    @GetMapping("/lane/{laneNumber}")
    public ResponseEntity<LaneCreationResult> getLane(@PathVariable Long laneNumber) {
        return ResponseEntity.ok(managementService.getLane(laneNumber));
    }

    /**
     * Fully replace a lane.
     * TODO: Add JWT authentication - @PreAuthorize placeholder for future implementation
     *
     * @param id Lane ID to update
     * @param laneNumber New lane number
     * @param isBlocked New blocked status
     * @return Updated LaneDTO if successful, error response if validation fails
     */
    //@PreAuthorize("hasRole('ADMIN')") // TODO: Placeholder for future JWT implementation
    @PutMapping("/lanes/{id}")
    public ResponseEntity<?> updateLane(@PathVariable Long id,
                                        @RequestParam Long laneNumber,
                                        @RequestParam Boolean isBlocked) {
        try {
            Lane lane = managementService.updateLane(id, laneNumber, isBlocked);
            return ResponseEntity.ok(new LaneDTO(lane.getId(), lane.getLaneNumber()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
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
