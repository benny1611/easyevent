package com.benny1611.easyevent.auth;

import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.OAuthCodeService;
import com.benny1611.easyevent.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    private final OAuthCodeService codeService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuthSuccessHandler(UserService userService, OAuthCodeService codeService) {
        this.userService = userService;
        this.codeService = codeService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuthUser = (OAuth2User) authentication.getPrincipal();

        String email = oAuthUser.getAttribute("email");
        String name = oAuthUser.getAttribute("name");
        String pictureUrl = oAuthUser.getAttribute("picture");

        Optional<User> userOptional = userService.findByEmail(email);
        User user = userOptional.orElseGet(() -> {
            try {
                return createUser(email, name, pictureUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        String code = codeService.create(user);

        response.sendRedirect(frontendUrl + "/oauth2/callback?code=" + code);
    }

    private User createUser(String email, String name, String pictureUrl) throws IOException {
        return userService.createUser(email, name, pictureUrl);
    }
}
