package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "email is missing or blank")
    private String email;

    @NotBlank(message = "password is missing or blank")
    private String password;
}
