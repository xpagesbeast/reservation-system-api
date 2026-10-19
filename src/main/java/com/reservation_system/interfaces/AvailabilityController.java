package com.reservation_system.interfaces;

import com.reservation_system.application.OperatingHoursService;
import com.reservation_system.application.TimeZoneService;
import com.reservation_system.application.services.AvailabilityService;
import com.reservation_system.domain.Lane;
import com.reservation_system.interfaces.dto.AvailabilityRequest;
import com.reservation_system.interfaces.dto.AvailabilityResponse;
import com.reservation_system.interfaces.dto.LaneDTO;
import com.reservation_system.interfaces.dto.LaneStatusResponse;
import com.reservation_system.interfaces.dto.ReservedLaneInfo;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for checking lane availability.
 * Provides endpoints to query available lanes for specific dates and times.
 */
@RestController
@RequestMapping("/api/v1")
public class AvailabilityController {
    
    private final AvailabilityService availabilityService;
    private final OperatingHoursService operatingHoursService;
    private final TimeZoneService timeZoneService;
    
    public AvailabilityController(AvailabilityService availabilityService, 
                                 OperatingHoursService operatingHoursService,
                                 TimeZoneService timeZoneService) {
        this.availabilityService = availabilityService;
        this.operatingHoursService = operatingHoursService;
        this.timeZoneService = timeZoneService;
    }
    
    /**
     * Get available lanes for a specific date, time, and duration.
     * 
     * @param request Availability request containing date, time, and duration
     * @return EntityModel with AvailabilityResponse and HATEOAS links
     */
    @PostMapping("/availability")
    public ResponseEntity<EntityModel<AvailabilityResponse>> getAvailability(@Valid @RequestBody AvailabilityRequest request) {
        LocalDate date = request.getDate();
        LocalTime time = request.getTime();
        int durationHours = request.getDurationHours();
        
        // Get operating hours
        LocalTime openingTime = operatingHoursService.getOpeningTime(date);
        LocalTime closingTime = operatingHoursService.getClosingTime(date);
        
        // Check if within operating hours
        if (!operatingHoursService.isWithinOperatingHours(date, time)) {
            AvailabilityResponse response = new AvailabilityResponse(
                    List.of(),
                    openingTime,
                    closingTime,
                    "Requested time is outside operating hours"
            );
            EntityModel<AvailabilityResponse> resource = EntityModel.of(response);
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getAvailability(request)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getLaneStatus()).withRel("laneStatus"));
            return ResponseEntity.ok(resource);
        }
        
        // Get available lanes
        List<Lane> availableLanes = availabilityService.getAvailableLanes(date, time, durationHours);
        
        // Convert to DTOs
        List<LaneDTO> laneDTOs = availableLanes.stream()
                .map(lane -> new LaneDTO(lane.getId(), lane.getLaneNumber()))
                .collect(Collectors.toList());
        
        String message = laneDTOs.isEmpty() ? "No lanes available for the requested time" : null;
        
        AvailabilityResponse response = new AvailabilityResponse(laneDTOs, openingTime, closingTime, message);
        EntityModel<AvailabilityResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getAvailability(request)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getLaneStatus()).withRel("laneStatus"));
        
        return ResponseEntity.ok(resource);
    }
    
    /**
     * Get lane status for the current hour in Aspen time zone.
     * Returns available and reserved lanes with reservation details.
     * 
     * @return EntityModel with LaneStatusResponse and HATEOAS links
     */
    @GetMapping("/lanes/status")
    public ResponseEntity<EntityModel<LaneStatusResponse>> getLaneStatus() {
        // Get reserved lanes for current hour
        List<Object[]> reservedLanesData = availabilityService.getLaneStatusForCurrentHour();
        
        // Get all lanes
        List<Lane> allLanes = availabilityService.getAvailableLanes(LocalDate.now(), LocalTime.now(), 1);
        
        // Extract reserved lane IDs
        Set<Long> reservedLaneIds = reservedLanesData.stream()
            .map(row -> (Long) row[0])
            .collect(Collectors.toSet());
        
        // Build available lanes list (all lanes minus reserved)
        List<Long> availableLanes = allLanes.stream()
            .filter(lane -> !reservedLaneIds.contains(lane.getId()))
            .map(Lane::getId)
            .collect(Collectors.toList());
        
        // Build reserved lanes info
        List<ReservedLaneInfo> reservedLanes = reservedLanesData.stream()
            .map(row -> {
                Long laneId = (Long) row[0];
                LocalDateTime reservationEndUtc = (LocalDateTime) row[2];
                int durationHours = (int) row[3];
                String customerName = (String) row[4];
                
                // Calculate end time in Aspen
                LocalDateTime reservationEndAspen = timeZoneService.convertUtcToAspen(reservationEndUtc);
                LocalTime until = reservationEndAspen.toLocalTime();
                
                return new ReservedLaneInfo(laneId, until, customerName);
            })
            .collect(Collectors.toList());
        
        LaneStatusResponse response = new LaneStatusResponse(availableLanes, reservedLanes);
        EntityModel<LaneStatusResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getLaneStatus()).withSelfRel());
        
        return ResponseEntity.ok(resource);
    }
}
