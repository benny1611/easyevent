package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dto.OauthCodeRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class OAuthService {

    private final OAuthCodeService codeService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    @Autowired
    public OAuthService(OAuthCodeService codeService, JwtUtils jwtUtils, UserRepository userRepository) {
        this.codeService = codeService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Transactional
    public String exchange(OauthCodeRequest request) {
        Long userID = codeService.consume(request.getCode());
        User user = userRepository.findByIdWithRoles(userID).orElseThrow(() -> new RuntimeException("Could not find user: " + userID));
        OffsetDateTime now = OffsetDateTime.now();
        user.setLastLoginAt(now);
        userRepository.save(user);
        return jwtUtils.generateToken(user);
    }
}
