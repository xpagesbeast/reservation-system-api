package com.reservation_system.interfaces.dto;

import com.reservation_system.domain.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class ReservationResponse {
    
    private Long id;
    private CustomerDTO customer;
    private LocalDate reservationDate;
    private LocalDateTime reservationTime;
    private Integer durationHours;
    private ReservationStatus status;
    private Set<Long> laneIds;
    private BigDecimal depositAmount;
    private Boolean extendedHoursRequested;
    private Boolean extendedHoursApproved;
    private String note;
    private String confirmationMessage;
    
    public ReservationResponse() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public CustomerDTO getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }
    
    public LocalDate getReservationDate() {
        return reservationDate;
    }
    
    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }
    
    public LocalDateTime getReservationTime() {
        return reservationTime;
    }
    
    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
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
    
    public Set<Long> getLaneIds() {
        return laneIds;
    }
    
    public void setLaneIds(Set<Long> laneIds) {
        this.laneIds = laneIds;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
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
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getConfirmationMessage() {
        return confirmationMessage;
    }
    
    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}
