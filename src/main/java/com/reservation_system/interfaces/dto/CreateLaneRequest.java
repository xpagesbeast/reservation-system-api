package com.reservation_system.interfaces.dto;

import com.reservation_system.domain.LaneStatusEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public class CreateLaneRequest {

    private Long laneNumber;

    @NotNull
    private String physicalLocation;

    private LaneStatusEnum status;

    @Max(value = 500, message = "Maintenance notes must be at most 500 characters")
    private String maintenanceNotes;

    public CreateLaneRequest() {}

    public Long getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(Long laneNumber) {
        this.laneNumber = laneNumber;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public LaneStatusEnum getStatus() {
        return status;
    }

    public void setStatus(LaneStatusEnum status) {
        this.status = status;
    }

    public String getMaintenanceNotes() {
        return maintenanceNotes;
    }

    public void setMaintenanceNotes(String maintenanceNotes) {
        this.maintenanceNotes = maintenanceNotes;
    }
}
