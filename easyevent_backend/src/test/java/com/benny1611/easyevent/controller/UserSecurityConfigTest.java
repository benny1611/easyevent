package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.auth.OAuthSuccessHandler;
import com.benny1611.easyevent.config.SecurityConfig;
import com.benny1611.easyevent.service.CustomUserDetailsService;
import com.benny1611.easyevent.service.UserService;
import com.benny1611.easyevent.util.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
public class UserSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private OAuthSuccessHandler oAuthSuccessHandler;

    @MockitoBean(name = "bcryptPasswordEncoder")
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // update user tests

    @Test
    public void updatingUserWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/users/update")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void updatingUserWithWrongRoleShouldReturn403() throws Exception {
        performPutRequestToUpdateUsers(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updatingUserWithRightRoleShouldReturn400() throws Exception {
        performPutRequestToUpdateUsers(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatingUserWithRightRoleShouldReturn400_ADMIN() throws Exception {
        performPutRequestToUpdateUsers(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void updatingUserWithRightRoleShouldReturn400_SUPER_ADMIN() throws Exception {
        performPutRequestToUpdateUsers(status().isBadRequest());
    }

    // update user by admin tests

    @Test
    public void updatingUserByAdminWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/users/update/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void updatingUserByAdminWithWrongRoleShouldReturn403() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updatingUserByAdminWithWrongRoleShouldReturn403_USER() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatingUserByAdminWithRightRoleShouldReturn400_ADMIN() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void updatingUserByAdminWithRightRoleShouldReturn400_SUPER_ADMIN() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isBadRequest());
    }

    private void performPutRequestToUpdateUsers(ResultMatcher expectedResult) throws Exception {
        String jsonForRole = """
    {
        "id": 1
    }
    """;

        MockMultipartFile userPart = new MockMultipartFile(
                "userDTO", "", "application/json", jsonForRole.getBytes()
        );

        mockMvc.perform(multipart("/api/users/update")
                        .file(userPart) // adding dummy user so that the request will go through
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andExpect(expectedResult);
    }

    private void performPutRequestToUpdateUsersByAdmin(ResultMatcher expectedResult) throws Exception {
        String validJsonButWrongRole = """
    {
        "email": "test@test.com"
    }
    """;

        MockMultipartFile userPart = new MockMultipartFile(
                "changeUserRequest", "", "application/json", validJsonButWrongRole.getBytes()
        );

        mockMvc.perform(multipart("/api/users/update/1")
                        .file(userPart) // adding dummy user so that the request will go through
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andExpect(expectedResult);
    }
}
