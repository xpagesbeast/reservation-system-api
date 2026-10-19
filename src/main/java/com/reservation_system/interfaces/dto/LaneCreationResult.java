package com.reservation_system.interfaces.dto;

import org.springframework.hateoas.RepresentationModel;

public class LaneCreationResult extends RepresentationModel<LaneCreationResult> {

    private Long laneNumber;
    private String status; // "created" or "failed"
    private String message;

    public LaneCreationResult() {}

    public LaneCreationResult(Long laneNumber, String status, String message) {
        this.laneNumber = laneNumber;
        this.status = status;
        this.message = message;
    }

    public Long getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(Long laneNumber) {
        this.laneNumber = laneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
