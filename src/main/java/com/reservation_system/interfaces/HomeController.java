package com.reservation_system.interfaces;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public EntityModel<Map<String, Object>> home() {
        Map<String, Object> content = Map.of(
            "name", "Lane Reservation API",
            "version", "1.0.0",
            "status", "operational"
        );
        
        EntityModel<Map<String, Object>> resource = EntityModel.of(content);
        
        // Add self link
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(HomeController.class).home()).withSelfRel();
        resource.add(selfLink);
        
        // Add endpoint links
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getAvailability(null)).withRel("availability"));
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ReservationController.class).queryReservations(null, null, null, null, null, 0, 50)).withRel("reservations"));
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CustomerController.class).getCustomerHistory(null)).withRel("customers"));
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ManagementController.class).approveExtendedHours(1L)).withRel("management"));
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AvailabilityController.class).getLaneStatus()).withRel("laneStatus"));
        
        // Add health and documentation links
        resource.add(Link.of("/actuator/health").withRel("health"));
        resource.add(Link.of("/javadoc/index.html").withRel("javadoc"));
        
        return resource;
    }
}
