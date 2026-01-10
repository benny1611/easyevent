package com.benny1611.easyevent.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private Set<String> roles;
}
