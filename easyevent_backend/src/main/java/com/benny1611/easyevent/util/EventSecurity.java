package com.benny1611.easyevent.util;

import com.benny1611.easyevent.dao.EventRepository;
import com.benny1611.easyevent.entity.Event;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class EventSecurity {

    private final EventRepository eventRepository;

    public EventSecurity(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean canDeleteEvent(long eventId, Authentication authentication) {
        String email = authentication.getName();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("Event " + eventId + " not found"));

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (isAdmin) {
            return true;
        } else {
            return event.getCreatedBy().getEmail().equals(email);
        }
    }
}
