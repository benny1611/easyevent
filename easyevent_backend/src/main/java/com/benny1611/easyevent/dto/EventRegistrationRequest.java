package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotNull
    Long eventId;
}
