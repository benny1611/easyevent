package com.benny1611.easyevent.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class EventResponse {
    Long id;
    String title;
    Integer numberOfSeats;
    Instant date;
    String createdByEmail;

    public EventResponse(Long id, String title, Integer numberOfSeats, Instant date, String createdByEmail) {
        this.id = id;
        this.title = title;
        this.numberOfSeats = numberOfSeats;
        this.date = date;
        this.createdByEmail = createdByEmail;
    }
}
