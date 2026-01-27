package com.benny1611.easyevent.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CreateEventResponse {
    private Long id;
    private String title;
    private Instant date;
    private Integer numberOfSeats;
}
