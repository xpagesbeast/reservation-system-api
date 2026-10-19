package com.reservation_system.interfaces;

import com.reservation_system.application.TimeZoneService;
import com.reservation_system.application.services.CustomerService;
import com.reservation_system.application.services.ReservationService;
import com.reservation_system.domain.Customer;
import com.reservation_system.domain.Lane;
import com.reservation_system.domain.Reservation;
import com.reservation_system.interfaces.dto.CreateCustomerRequest;
import com.reservation_system.interfaces.dto.CustomerDTO;
import com.reservation_system.interfaces.dto.CustomerHistoryRequest;
import com.reservation_system.interfaces.dto.ReservationResponse;
import com.reservation_system.interfaces.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
 * REST controller for customer-related operations.
 * Provides endpoints to retrieve customer reservation history and manage customers.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final ReservationService reservationService;
    private final CustomerService customerService;
    private final TimeZoneService timeZoneService;
    private final BigDecimal depositAmount;

    public CustomerController(ReservationService reservationService,
                             CustomerService customerService,
                             TimeZoneService timeZoneService,
                             @Value("${reservation.deposit-amount}") BigDecimal depositAmount) {
        this.reservationService = reservationService;
        this.customerService = customerService;
        this.timeZoneService = timeZoneService;
        this.depositAmount = depositAmount;
    }
    
    /**
     * Get reservation history for a specific customer by email.
     * Email is in request body to avoid logging in URL paths.
     *
     * @param request Customer history request containing email
     * @return CollectionModel with ReservationResponse objects and HATEOAS links
     */
    @PostMapping("/history")
    public ResponseEntity<CollectionModel<EntityModel<ReservationResponse>>> getCustomerHistory(@Valid @RequestBody CustomerHistoryRequest request) {
        List<Reservation> reservations = reservationService.findByCustomerEmail(request.getEmail());

        List<EntityModel<ReservationResponse>> responseModels = reservations.stream()
                .map(reservation -> {
                    ReservationResponse response = mapToResponse(reservation);
                    EntityModel<ReservationResponse> resource = EntityModel.of(response);
                    resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class).getCustomerHistory(request)).withSelfRel());
                    return resource;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ReservationResponse>> collection = CollectionModel.of(responseModels);
        collection.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class).getCustomerHistory(request)).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    /**
     * Create a new customer.
     * TODO: Add JWT authentication - @PreAuthorize placeholder for future implementation
     *
     * @param request Customer creation request with name, email, and phone
     * @return CustomerDTO if successful, error response if validation fails
     */
    @PreAuthorize("hasRole('ADMIN')") // TODO: Placeholder for future JWT implementation
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        try {
            Customer customer = customerService.createCustomer(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone()
            );
            CustomerDTO customerDTO = new CustomerDTO(
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(customerDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Fully replace a customer.
     * TODO: Add JWT authentication - @PreAuthorize placeholder for future implementation
     *
     * @param id Customer ID to update
     * @param request Customer update request with name, email, and phone
     * @return CustomerDTO if successful, error response if validation fails
     */
    @PreAuthorize("hasRole('ADMIN')") // TODO: Placeholder for future JWT implementation
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id,
                                           @Valid @RequestBody UpdateCustomerRequest request) {
        try {
            Customer customer = customerService.updateCustomer(
                    id,
                    request.getName(),
                    request.getEmail(),
                    request.getPhone()
            );
            CustomerDTO customerDTO = new CustomerDTO(
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
            return ResponseEntity.ok(customerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Retrieve a customer by ID.
     * TODO: Add JWT authentication - @PreAuthorize placeholder for future implementation
     *
     * @param id Customer ID to retrieve
     * @return CustomerDTO if found, 404 if not found
     */
    @PreAuthorize("hasRole('ADMIN')") // TODO: Placeholder for future JWT implementation
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            Customer customer = customerService.getCustomerById(id);
            CustomerDTO customerDTO = new CustomerDTO(
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
            return ResponseEntity.ok(customerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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
