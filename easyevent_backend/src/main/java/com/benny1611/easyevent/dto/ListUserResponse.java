package com.benny1611.easyevent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListUserResponse {
    private Long id;
    private String name;
    private String email;
    private String profilePicture;
    private boolean active;
    private boolean isBanned;
    private List<String> roles;
}
