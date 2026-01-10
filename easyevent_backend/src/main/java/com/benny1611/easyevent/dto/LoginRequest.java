package com.benny1611.easyevent.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
