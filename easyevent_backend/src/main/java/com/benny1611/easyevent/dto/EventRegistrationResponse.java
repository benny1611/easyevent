package com.benny1611.easyevent.dto;

import com.benny1611.easyevent.entity.EventRegistration;
import lombok.Data;

import java.time.Instant;

@Data
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String name;
    private String email;
    private boolean isGuest;
    private Instant registeredAt;

    public static EventRegistrationResponse fromEntity(EventRegistration reg) {
        EventRegistrationResponse response = new EventRegistrationResponse();
        response.setId(reg.getId());
        response.setEventId(reg.getEvent().getId());
        response.setRegisteredAt(Instant.parse(reg.getRegisteredAt()));

        boolean isGuest = reg.isGuestRegistration();

        if (isGuest) {
            response.setName(reg.getGuest().getName());
            response.setEmail(reg.getGuest().getEmail());
        } else {
            response.setName(reg.getUser().getName());
            response.setEmail(reg.getUser().getEmail());
        }

        return response;
    }
}
