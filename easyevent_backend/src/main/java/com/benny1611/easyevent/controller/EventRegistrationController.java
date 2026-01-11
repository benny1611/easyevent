package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.EventRegistrationResponse;
import com.benny1611.easyevent.service.EventRegistrationsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class EventRegistrationController {

    private final EventRegistrationsService eventRegistrationsService;

    public EventRegistrationController(EventRegistrationsService eventRegistrationsService) {
        this.eventRegistrationsService = eventRegistrationsService;
    }

    @GetMapping("/event/{eventId}")
    public PagedModel<EntityModel<EventRegistrationResponse>> getRegistrations(@PathVariable Long eventId,
                                                                               @PageableDefault(size = 20, sort = "registeredAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                                               PagedResourcesAssembler<EventRegistrationResponse> assembler) {
        Page<EventRegistrationResponse> page = eventRegistrationsService.getRegistrationsForEvent(eventId, pageable);
        return assembler.toModel(page);
    }
}
