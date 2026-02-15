package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ResendActivationMailRequest {

    @NotNull(message = "token should not be null")
    private UUID token;
}
