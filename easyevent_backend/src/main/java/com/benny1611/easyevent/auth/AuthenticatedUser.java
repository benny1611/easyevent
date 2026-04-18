package com.benny1611.easyevent.auth;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
public class AuthenticatedUser {
    private final Long userId;
    private final String email;
    private final List<GrantedAuthority> authorities;
}
