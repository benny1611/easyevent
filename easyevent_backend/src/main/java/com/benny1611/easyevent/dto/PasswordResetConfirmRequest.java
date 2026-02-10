package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank
    private String secret;

    @NotNull
    private UUID tokenId;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
