package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ChangeUserRequest {
    @Email
    private String email;
    private String name;
}
