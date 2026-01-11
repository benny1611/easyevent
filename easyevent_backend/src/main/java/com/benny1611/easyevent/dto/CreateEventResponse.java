package com.benny1611.easyevent.dto;

import lombok.Data;

@Data
public class CreateEventResponse {
    private Long id;
    private String title;
    private String date;
    private Integer numberOfSeats;
}
