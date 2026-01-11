package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.EventRegistrationRepository;
import com.benny1611.easyevent.dto.EventRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventRegistrationsService {

    private final EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    public EventRegistrationsService(EventRegistrationRepository eventRegistrationRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    public Page<EventRegistrationResponse> getRegistrationsForEvent(Long eventId, Pageable pageable) {
        return eventRegistrationRepository.findByEventId(eventId, pageable).map(EventRegistrationResponse::fromEntity);
    }
}
