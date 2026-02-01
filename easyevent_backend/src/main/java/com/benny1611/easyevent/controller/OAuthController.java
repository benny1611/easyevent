package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.LoginResponse;
import com.benny1611.easyevent.dto.OauthCodeRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.OAuthCodeService;
import com.benny1611.easyevent.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController {

    private final OAuthCodeService codeService;
    private final JwtUtils jwtUtils;

    public OAuthController(OAuthCodeService codeService, JwtUtils jwtUtils) {
        this.codeService = codeService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/exchange")
    public ResponseEntity<LoginResponse> exchange(@Valid @RequestBody OauthCodeRequest request) {
        User user = codeService.consume(request.getCode());
        String token = jwtUtils.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
