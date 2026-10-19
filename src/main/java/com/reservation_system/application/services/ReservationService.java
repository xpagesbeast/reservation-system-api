package com.reservation_system.application.services;

import com.reservation_system.application.OperatingHoursService;
import com.reservation_system.application.TimeZoneService;
import com.reservation_system.domain.*;
import com.reservation_system.domain.enums.PaymentStatus;
import com.reservation_system.domain.enums.ReservationStatus;
import com.reservation_system.domain.exceptions.NotFoundException;
import com.reservation_system.infrastructure.payment.PaymentService;
import com.reservation_system.infrastructure.persistence.PaymentAuthorizationRepository;
import com.reservation_system.infrastructure.persistence.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final CustomerService customerService;
    private final AvailabilityService availabilityService;
    private final OperatingHoursService operatingHoursService;
    private final TimeZoneService timeZoneService;
    private final PaymentService paymentService;
    private final PaymentAuthorizationRepository paymentAuthorizationRepository;
    private final BigDecimal depositAmount;
    private final int cancellationDeadlineHours;
    
    public ReservationService(ReservationRepository reservationRepository,
                             CustomerService customerService,
                             AvailabilityService availabilityService,
                             OperatingHoursService operatingHoursService,
                             TimeZoneService timeZoneService,
                             PaymentService paymentService,
                             PaymentAuthorizationRepository paymentAuthorizationRepository,
                             @Value("${reservation.deposit-amount}") BigDecimal depositAmount,
                             @Value("${reservation.cancellation-deadline-hours}") int cancellationDeadlineHours) {
        this.reservationRepository = reservationRepository;
        this.customerService = customerService;
        this.availabilityService = availabilityService;
        this.operatingHoursService = operatingHoursService;
        this.timeZoneService = timeZoneService;
        this.paymentService = paymentService;
        this.paymentAuthorizationRepository = paymentAuthorizationRepository;
        this.depositAmount = depositAmount;
        this.cancellationDeadlineHours = cancellationDeadlineHours;
    }
    
    public Reservation createReservation(String customerName, String customerEmail, String customerPhone,
                                          LocalDate date, LocalTime time, int durationHours,
                                          Set<Long> laneIds, boolean policyAccepted,
                                          String paymentMethodToken, String note) {
        
        // Validate policy acceptance
        if (!policyAccepted) {
            throw new IllegalArgumentException("Cancellation policy must be accepted");
        }
        
        // Validate duration is in 1-hour increments
        if (durationHours <= 0 || durationHours % 1 != 0) {
            throw new IllegalArgumentException("Duration must be in 1-hour increments");
        }
        
        // Check booking window (max 1 year in advance)
        LocalDate maxDate = LocalDate.now().plusYears(1);
        if (date.isAfter(maxDate)) {
            throw new IllegalArgumentException("Reservations cannot be made more than 1 year in advance");
        }
        
        // Find or create customer
        Customer customer = customerService.findOrCreateCustomer(customerName, customerEmail, customerPhone);
        
        // Check daily limit (one reservation per day per customer)
        List<Reservation> existingReservations = reservationRepository.findByCustomerEmailAndReservationDate(customerEmail, date);
        if (!existingReservations.isEmpty()) {
            throw new IllegalStateException("Customer already has a reservation for this date");
        }
        
        // Check if within operating hours
        boolean withinOperatingHours = operatingHoursService.isWithinOperatingHours(date, time);
        boolean extendedHoursRequested = !withinOperatingHours;
        
        // Validate lane availability
        for (Long laneId : laneIds) {
            if (!availabilityService.isLaneAvailable(laneId, date, time, durationHours)) {
                throw new IllegalArgumentException("Lane " + laneId + " is not available for the requested time");
            }
        }
        
        // Convert to UTC
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        LocalDateTime utcDateTime = timeZoneService.convertAspenToUtc(localDateTime);
        
        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setReservationDate(date);
        reservation.setReservationTimeUtc(utcDateTime);
        reservation.setDurationHours(durationHours);
        reservation.setCancellationPolicyAccepted(policyAccepted);
        reservation.setNote(note);
        reservation.setExtendedHoursRequested(extendedHoursRequested);
        
        // Set status based on extended hours
        if (extendedHoursRequested) {
            reservation.setStatus(ReservationStatus.PENDING_APPROVAL);
        } else {
            // Process payment for normal reservations
            PaymentService.PaymentAuthorizationResult authResult = paymentService.authorize(depositAmount, paymentMethodToken);
            if (!authResult.isSuccess()) {
                throw new IllegalStateException("Payment authorization failed: " + authResult.getErrorMessage());
            }
            
            // Capture payment immediately
            PaymentService.PaymentCaptureResult captureResult = paymentService.capture(authResult.getAuthorizationId());
            if (!captureResult.isSuccess()) {
                throw new IllegalStateException("Payment capture failed: " + captureResult.getErrorMessage());
            }
            
            // Create payment authorization record
            PaymentAuthorization paymentAuth = new PaymentAuthorization(reservation, depositAmount);
            paymentAuth.setStatus(PaymentStatus.CAPTURED);
            paymentAuth.setPaymentMethodToken(paymentMethodToken);
            paymentAuthorizationRepository.save(paymentAuth);
            
            reservation.setStatus(ReservationStatus.CONFIRMED);
        }
        
        reservation = reservationRepository.save(reservation);
        
        return reservation;
    }
    
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        
        LocalDateTime reservationTime = reservation.getReservationTimeUtc();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = reservationTime.minusHours(cancellationDeadlineHours);
        
        boolean withinDeadline = now.isBefore(cancellationDeadline);
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        
        if (withinDeadline) {
            // Refund deposit
            Optional<PaymentAuthorization> paymentAuth = paymentAuthorizationRepository.findFirstByReservationOrderByCreatedAtDesc(reservation);
            if (paymentAuth.isPresent() && paymentAuth.get().getStatus() == PaymentStatus.CAPTURED) {
                PaymentService.PaymentRefundResult refundResult = paymentService.refund(paymentAuth.get().getPaymentMethodToken(), depositAmount);
                if (refundResult.isSuccess()) {
                    paymentAuth.get().setStatus(PaymentStatus.REFUNDED);
                    paymentAuthorizationRepository.save(paymentAuth.get());
                }
            }
        }
        
        return reservationRepository.save(reservation);
    }
    
    public Reservation processNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be processed as no-show");
        }
        
        reservation.setStatus(ReservationStatus.NO_SHOW);
        
        // Deposit is already captured, no refund needed
        return reservationRepository.save(reservation);
    }
    
    public Reservation approveExtendedHours(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        
        if (reservation.getStatus() != ReservationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending approval reservations can be approved");
        }
        
        reservation.setStatus(ReservationStatus.APPROVED);
        reservation.setExtendedHoursApproved(true);
        
        return reservationRepository.save(reservation);
    }
    
    public Reservation rejectExtendedHours(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        
        if (reservation.getStatus() != ReservationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending approval reservations can be rejected");
        }
        
        reservation.setStatus(ReservationStatus.REJECTED);
        
        return reservationRepository.save(reservation);
    }
    
    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }
    
    public List<Reservation> findByCustomerEmail(String email) {
        return reservationRepository.findByCustomerEmail(email);
    }
    
    /**
     * Query reservations with optional date, status, and customer filters with pagination.
     * Date filters are in Aspen time zone and converted to UTC for database queries.
     */
    public Page<Reservation> queryReservations(Integer year, Integer month, Integer day, 
                                                  List<ReservationStatus> statuses, 
                                                  String customerEmail, 
                                                  Pageable pageable) {
        List<Reservation> results;
        
        if (year != null) {
            if (month != null && day != null) {
                results = reservationRepository.findByYearAndMonthAndDay(year, month, day);
            } else if (month != null) {
                results = reservationRepository.findByYearAndMonth(year, month);
            } else {
                results = reservationRepository.findByYear(year);
            }
        } else if (statuses != null) {
            results = reservationRepository.findByStatusIn(statuses);
        } else if (customerEmail != null) {
            results = reservationRepository.findByCustomerEmail(customerEmail);
        } else {
            return reservationRepository.findAll(pageable);
        }
        
        // Apply additional filters
        if (statuses != null && year == null) {
            results = results.stream()
                .filter(r -> statuses.contains(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (customerEmail != null && year == null && statuses == null) {
            results = results.stream()
                .filter(r -> r.getCustomer().getEmail().equals(customerEmail))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply pagination manually for filtered results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        
        if (start >= results.size()) {
            return new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList(), pageable, results.size());
        }
        
        List<Reservation> pagedResults = results.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pagedResults, pageable, results.size());
    }
}
