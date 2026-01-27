package com.benny1611.easyevent.dto;

import com.benny1611.easyevent.entity.EventRegistration;
import lombok.Data;

import java.time.Instant;

@Data
public class GetEventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String name;
    private String email;
    private boolean isGuest;
    private Instant registeredAt;

    public static GetEventRegistrationResponse fromEntity(EventRegistration reg) {
        GetEventRegistrationResponse response = new GetEventRegistrationResponse();
        response.setId(reg.getId());
        response.setEventId(reg.getEvent().getId());
        response.setRegisteredAt(reg.getRegisteredAt());

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
