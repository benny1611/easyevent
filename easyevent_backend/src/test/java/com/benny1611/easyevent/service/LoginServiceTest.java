package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.dto.LoginRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
import com.benny1611.easyevent.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    UserStateRepository userStateRepository;

    LoginService loginService;

    UserState blockedState;
    UserState activeState;

    @BeforeEach
    void setup() {
        loginService = new LoginService(userRepository, authenticationManager, jwtUtils, userStateRepository, 3);

        blockedState = new UserState();
        blockedState.setName("BLOCKED");
        blockedState.setId((short) 2);

        activeState = new UserState();
        activeState.setName("ACTIVE");
        activeState.setId((short) 1);

        when(userStateRepository.findByName("BLOCKED")).thenReturn(Optional.of(blockedState));
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");

        User user = new User();
        user.setEmail("test@email.com");
        user.setState(activeState);

        when(userRepository.findByEmailWithRolesAndState("test@email.com")).thenReturn(Optional.of(user));

        when(jwtUtils.generateToken(user)).thenReturn("token");

        String token = loginService.login(request);
        assertEquals("token", token);
    }

    @Test
    void loginFailure() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");

        // user does not exist
        AtomicReference<String> token = new AtomicReference<>(loginService.login(request));
        assertNull(token.get());

        User user = new User();
        user.setEmail("test@email.com");
        user.setState(activeState);
        user.setFailedLoginAttempts(0);

        when(userRepository.findByEmailWithRolesAndState("test@email.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

        assertThrows(AuthenticationException.class, () ->
                loginService.login(request)
        );
        assertEquals(1, user.getFailedLoginAttempts());

        assertThrows(AuthenticationException.class, () ->
                loginService.login(request)
        );
        assertEquals(2, user.getFailedLoginAttempts());

        assertThrows(AuthenticationException.class, () ->
                token.set(loginService.login(request))
        );
        assertNull(token.get());
        assertEquals(3, user.getFailedLoginAttempts());
        assertEquals(blockedState.getId(), user.getState().getId());
    }
}
