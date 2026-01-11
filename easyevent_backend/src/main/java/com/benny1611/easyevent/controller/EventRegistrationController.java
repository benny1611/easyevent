package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.EventRegistrationRequest;
import com.benny1611.easyevent.dto.GetEventRegistrationResponse;
import com.benny1611.easyevent.service.EventRegistrationsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class EventRegistrationController {

    private final EventRegistrationsService eventRegistrationsService;

    public EventRegistrationController(EventRegistrationsService eventRegistrationsService) {
        this.eventRegistrationsService = eventRegistrationsService;
    }

    @GetMapping("/event/{eventId}")
    public PagedModel<EntityModel<GetEventRegistrationResponse>> getRegistrations(@PathVariable Long eventId,
                                                                                  @PageableDefault(size = 20, sort = "registeredAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                                  PagedResourcesAssembler<GetEventRegistrationResponse> assembler) {
        Page<GetEventRegistrationResponse> page = eventRegistrationsService.getRegistrationsForEvent(eventId, pageable);
        return assembler.toModel(page);
    }

    @PostMapping("/event")
    public ResponseEntity<Void> registerToAnEvent(@AuthenticationPrincipal String email,
                                                                       @Valid @RequestBody EventRegistrationRequest registrationRequest) {
        eventRegistrationsService.registerToAnEvent(email, registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
