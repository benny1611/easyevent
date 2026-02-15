package com.benny1611.easyevent.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String name;
    private String profilePicture;
    private boolean active;
}
