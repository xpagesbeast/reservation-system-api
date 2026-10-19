package com.reservation_system.application.services;

import com.reservation_system.domain.Lane;
import com.reservation_system.domain.LaneStatusEnum;
import com.reservation_system.infrastructure.persistence.LaneRepository;
import com.reservation_system.interfaces.ManagementController;
import com.reservation_system.interfaces.dto.CreateLaneRequest;
import com.reservation_system.interfaces.dto.LaneCreationResult;
import com.reservation_system.interfaces.dto.LaneDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@Transactional
public class ManagementService {

    private final LaneRepository laneRepository;

    public ManagementService(LaneRepository laneRepository) {
        this.laneRepository = laneRepository;
    }

    public Lane blockLane(Long laneId) {
        Lane lane = laneRepository.findById(laneId)
                .orElseThrow(() -> new IllegalArgumentException("Lane not found"));
        lane.setIsBlocked(true);
        return laneRepository.save(lane);
    }

    public Lane unblockLane(Long laneId) {
        Lane lane = laneRepository.findById(laneId)
                .orElseThrow(() -> new IllegalArgumentException("Lane not found"));
        lane.setIsBlocked(false);
        return laneRepository.save(lane);
    }

    public List<LaneCreationResult> createLanes(List<CreateLaneRequest> laneRequests) {
        List<LaneCreationResult> results = new ArrayList<>();

        for (CreateLaneRequest request : laneRequests) {
            // Check if lane number already exists
            Optional<Lane> existingLane = laneRepository.findAll().stream()
                    .filter(lane -> lane.getLaneNumber().equals(request.getLaneNumber()))
                    .findFirst();

            if (existingLane.isPresent()) {
                results.add(new LaneCreationResult(request.getLaneNumber(), "failed",
                        "Lane number " + request.getLaneNumber() + " already exists"));
            } else {
                Lane lane = new Lane();
                lane.setLaneNumber(request.getLaneNumber());

                // Truncate physicalLocation to 100 characters if exceeded
                if (request.getPhysicalLocation() != null && request.getPhysicalLocation().length() > 100) {
                    lane.setPhysicalLocation(request.getPhysicalLocation().substring(0, 100));
                } else {
                    lane.setPhysicalLocation(request.getPhysicalLocation());
                }

                // Default status to AVAILABLE if not provided
                lane.setStatus(request.getStatus() != null ? request.getStatus() : LaneStatusEnum.AVAILABLE);

                // Set isBlocked to false by default
                lane.setIsBlocked(false);

                lane.setMaintenanceNotes(request.getMaintenanceNotes());

                laneRepository.save(lane);
                results.add(new LaneCreationResult(request.getLaneNumber(), "created", "Lane created successfully"));
            }
        }

        return results;
    }

    public Lane updateLane(Long laneId, Long laneNumber, Boolean isBlocked) {
        Lane lane = laneRepository.findById(laneId)
                .orElseThrow(() -> new IllegalArgumentException("Lane not found"));

        // Check if new lane number conflicts with another lane
        Optional<Lane> existingLane = laneRepository.findAll().stream()
                .filter(l -> !l.getId().equals(laneId) && l.getLaneNumber().equals(laneNumber))
                .findFirst();

        if (existingLane.isPresent()) {
            throw new IllegalStateException("Lane number " + laneNumber + " already exists");
        }

        lane.setLaneNumber(laneNumber);
        lane.setIsBlocked(isBlocked);
        return laneRepository.save(lane);
    }

    public List<LaneCreationResult> getAllLanes() {
        return laneRepository.findAll().stream()
                .map(lane -> {
                    LaneCreationResult result = new LaneCreationResult(
                            lane.getLaneNumber(),
                            lane.getStatus().name(),
                            lane.getMaintenanceNotes()
                    );

                    result.add(linkTo(methodOn(ManagementController.class)
                            .getLane(lane.getLaneNumber()))
                            .withSelfRel());

                    return result;
                })
                .collect(Collectors.toList());
    }

    public LaneCreationResult getLane(Long laneNumber) {

        Optional<Lane> lane = laneRepository.findByLaneNumber(laneNumber);

        if (lane.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lane not found: " + laneNumber
            );
        }

        Lane entity = lane.get();

        return new LaneCreationResult(
                entity.getLaneNumber(),
                entity.getStatus().name(),
                "Lane retrieved successfully"
        );
    }
}
