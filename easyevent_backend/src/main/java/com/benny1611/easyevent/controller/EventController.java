package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.CreateEventRequest;
import com.benny1611.easyevent.dto.CreateEventResponse;
import com.benny1611.easyevent.dto.EventResponse;
import com.benny1611.easyevent.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger LOG = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public PagedModel<EntityModel<EventResponse>> getAllEvents(@AuthenticationPrincipal String email,
                                                               @PageableDefault(size = 20, sort = "date") Pageable pageable,
                                                               PagedResourcesAssembler<EventResponse> assembler) {
        Page<EventResponse> page = eventService.getEvents(pageable);
        return assembler.toModel(page);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable("id") long id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateEventResponse> createEvent(@AuthenticationPrincipal String email, @Valid @RequestBody CreateEventRequest request) {
        CreateEventResponse response = eventService.createEvent(request, email);
        return ResponseEntity.ok(response);
    }
}
