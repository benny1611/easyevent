package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotNull(message = "Number of seats is required")
    private Integer numberOfSeats;

    @NotNull(message = "Date for the event must be present")
    private String date;
}
