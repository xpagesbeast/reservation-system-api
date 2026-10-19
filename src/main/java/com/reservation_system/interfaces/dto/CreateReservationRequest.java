package com.reservation_system.interfaces.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class CreateReservationRequest {
    
    @NotNull(message = "Customer information is required")
    private CustomerDTO customer;
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Time is required")
    private LocalTime time;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;
    
    @NotEmpty(message = "At least one lane must be selected")
    private Set<Long> laneIds;
    
    @NotNull(message = "Policy acceptance is required")
    private Boolean cancellationPolicyAccepted;
    
    @NotBlank(message = "Payment method token is required")
    private String paymentMethodToken;
    
    private String note;
    
    public CreateReservationRequest() {}
    
    public CustomerDTO getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
    }
    
    public Integer getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }
    
    public Set<Long> getLaneIds() {
        return laneIds;
    }
    
    public void setLaneIds(Set<Long> laneIds) {
        this.laneIds = laneIds;
    }
    
    public Boolean getCancellationPolicyAccepted() {
        return cancellationPolicyAccepted;
    }
    
    public void setCancellationPolicyAccepted(Boolean cancellationPolicyAccepted) {
        this.cancellationPolicyAccepted = cancellationPolicyAccepted;
    }
    
    public String getPaymentMethodToken() {
        return paymentMethodToken;
    }
    
    public void setPaymentMethodToken(String paymentMethodToken) {
        this.paymentMethodToken = paymentMethodToken;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
