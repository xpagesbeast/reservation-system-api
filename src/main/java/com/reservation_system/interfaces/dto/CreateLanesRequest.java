package com.reservation_system.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateLanesRequest {

    @NotEmpty(message = "At least one lane is required")
    @Valid
    private List<CreateLaneRequest> lanes;

    public CreateLanesRequest() {}

    public List<CreateLaneRequest> getLanes() {
        return lanes;
    }

    public void setLanes(List<CreateLaneRequest> lanes) {
        this.lanes = lanes;
    }
}
