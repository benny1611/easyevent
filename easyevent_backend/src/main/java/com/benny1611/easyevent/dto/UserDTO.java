package com.benny1611.easyevent.dto;

import com.benny1611.easyevent.util.ValidUserDTOPasswordChange;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ValidUserDTOPasswordChange
public class UserDTO {
    @NotBlank
    @Email(message = "Email must be present and valid")
    private String email;
    private String name;
    private String profilePicture;
    private String language;
    private String oldPassword;
    private String newPassword;
    private String token;
    private boolean active;
    private boolean oauthUser;
}
