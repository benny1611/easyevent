package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.LoginResponse;
import com.benny1611.easyevent.dto.OauthCodeRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.OAuthCodeService;
import com.benny1611.easyevent.service.OAuthService;
import com.benny1611.easyevent.service.UserService;
import com.benny1611.easyevent.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @Autowired
    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @PostMapping("/exchange")
    public ResponseEntity<LoginResponse> exchange(@Valid @RequestBody OauthCodeRequest request) {
        String token = oAuthService.exchange(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
