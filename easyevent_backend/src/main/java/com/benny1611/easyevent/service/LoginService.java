package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.dto.LoginRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
import com.benny1611.easyevent.util.JwtUtils;
import com.benny1611.easyevent.util.exception.BlockedUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserStateRepository userStateRepository;
    private final int maxFailedPWAttempts;

    @Autowired
    public LoginService(UserRepository userRepository,
                        AuthenticationManager authenticationManager,
                        JwtUtils jwtUtils, UserStateRepository userStateRepository,
                        @Value("${app.max-failed-password-attempts}") int maxFailedPWAttempts) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userStateRepository = userStateRepository;
        this.maxFailedPWAttempts = maxFailedPWAttempts;
    }

    @Transactional(noRollbackFor = AuthenticationException.class)
    public String login(LoginRequest request) {
        Authentication authentication;
        User user = userRepository.findByEmailWithRolesAndState(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        // Check if the user is blocked
        UserState blockedState = userStateRepository.findByName("BLOCKED").orElseThrow(() -> new RuntimeException("Could not find the BLOCKED state"));
        UserState userState = user.getState();
        if (userState.getId().intValue() == blockedState.getId().intValue()) {
            throw new BlockedUserException(null);
        }

        // Check the credentials
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            int failedAttempts = user.getFailedLoginAttempts();
            if (failedAttempts >= maxFailedPWAttempts) {
                user.setState(blockedState);
            }
            userRepository.save(user);
            throw ex;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateToken(user);
        OffsetDateTime now = OffsetDateTime.now();
        user.setLastLoginAt(now);
        userRepository.save(user);

        return token;
    }
}
