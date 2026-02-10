package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.PasswordResetTokenRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.entity.PasswordResetToken;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserStateRepository userStateRepository;
    private final PasswordEncoder passwordEncoder;
    private final IMailService mailService;
    @Value("${app.password.reset.token.expiry-minutes}")
    private int expiryMinutes;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                UserStateRepository userStateRepository,
                                PasswordEncoder passwordEncoder,
                                IMailService mailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userStateRepository = userStateRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void requestReset(String email) {

        // invalidate old tokens
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.findByUsedFalseAndExpiresAtAfter(Instant.now())
                    .stream()
                    .filter(t -> t.getUser().equals(user))
                    .forEach(t -> {
                        t.setUsed(true);
                        tokenRepository.save(t);
                    });
            String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(passwordEncoder.encode(rawToken));
            token.setExpiresAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES));
            tokenRepository.save(token);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            mailService.sendPasswordResetEmail(user, resetLink, expiryMinutes);
        });

        // Always succeed (avoid user enumeration)
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository
                .findValidTokensForUpdate(Instant.now())
                .stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));
        UserState activeState = userStateRepository.findByName("ACTIVE").orElseThrow(() -> new RuntimeException("Could not find the ACTIVE state"));

        User user = token.getUser();
        user.setState(activeState);
        user.setFailedLoginAttempts(0);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
