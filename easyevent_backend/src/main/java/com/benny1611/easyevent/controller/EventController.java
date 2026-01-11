package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.CreateEventRequest;
import com.benny1611.easyevent.dto.CreateEventResponse;
import com.benny1611.easyevent.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger LOG = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateEventResponse> createEvent(@AuthenticationPrincipal String email, @Valid @RequestBody CreateEventRequest request) {
        CreateEventResponse response = eventService.createEvent(request, email);
        return ResponseEntity.ok(response);
    }
}
