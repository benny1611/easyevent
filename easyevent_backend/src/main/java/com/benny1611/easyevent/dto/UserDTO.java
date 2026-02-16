package com.benny1611.easyevent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Email must be present")
    private String email;
    private String name;
    private String profilePicture;
    private String language;
}
