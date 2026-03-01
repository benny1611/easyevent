package com.benny1611.easyevent.auth;

import lombok.Data;

@Data
public class AuthenticatedUser {
    private final Long userId;
    private final String email;
}
