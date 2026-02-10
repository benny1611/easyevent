package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank(message = "name must not be empty")
    private String name;

    @Email
    @NotBlank(message = "email must not be empty")
    private String email;

    @NotBlank(message = "password cannot be empty")
    private String password;

    @NotNull(message = "roles must be present")
    private Set<String> roles;
}
