package com.reservation_system.domain;

import com.reservation_system.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    
    @Column(name = "reservation_time_utc", nullable = false)
    private LocalDateTime reservationTimeUtc;
    
    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Column(name = "extended_hours_requested", nullable = false)
    private Boolean extendedHoursRequested = false;
    
    @Column(name = "extended_hours_approved", nullable = false)
    private Boolean extendedHoursApproved = false;
    
    @Column(name = "cancellation_policy_accepted", nullable = false)
    private Boolean cancellationPolicyAccepted = false;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Version
    private Integer version = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToMany
    @JoinTable(
        name = "reservation_lanes",
        joinColumns = @JoinColumn(name = "reservation_id"),
        inverseJoinColumns = @JoinColumn(name = "lane_id")
    )
    private Set<Lane> lanes = new HashSet<>();
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAuthorization> paymentAuthorizations = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Reservation() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public LocalDate getReservationDate() {
        return reservationDate;
    }
    
    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }
    
    public LocalDateTime getReservationTimeUtc() {
        return reservationTimeUtc;
    }
    
    public void setReservationTimeUtc(LocalDateTime reservationTimeUtc) {
        this.reservationTimeUtc = reservationTimeUtc;
    }
    
    public Integer getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public Boolean getExtendedHoursRequested() {
        return extendedHoursRequested;
    }
    
    public void setExtendedHoursRequested(Boolean extendedHoursRequested) {
        this.extendedHoursRequested = extendedHoursRequested;
    }
    
    public Boolean getExtendedHoursApproved() {
        return extendedHoursApproved;
    }
    
    public void setExtendedHoursApproved(Boolean extendedHoursApproved) {
        this.extendedHoursApproved = extendedHoursApproved;
    }
    
    public Boolean getCancellationPolicyAccepted() {
        return cancellationPolicyAccepted;
    }
    
    public void setCancellationPolicyAccepted(Boolean cancellationPolicyAccepted) {
        this.cancellationPolicyAccepted = cancellationPolicyAccepted;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<Lane> getLanes() {
        return lanes;
    }
    
    public void setLanes(Set<Lane> lanes) {
        this.lanes = lanes;
    }
    
    public void addLane(Lane lane) {
        lanes.add(lane);
    }
    
    public void removeLane(Lane lane) {
        lanes.remove(lane);
    }
    
    public List<PaymentAuthorization> getPaymentAuthorizations() {
        return paymentAuthorizations;
    }
    
    public void setPaymentAuthorizations(List<PaymentAuthorization> paymentAuthorizations) {
        this.paymentAuthorizations = paymentAuthorizations;
    }
    
    public void addPaymentAuthorization(PaymentAuthorization paymentAuthorization) {
        paymentAuthorizations.add(paymentAuthorization);
        paymentAuthorization.setReservation(this);
    }
    
    public void removePaymentAuthorization(PaymentAuthorization paymentAuthorization) {
        paymentAuthorizations.remove(paymentAuthorization);
        paymentAuthorization.setReservation(null);
    }
}
