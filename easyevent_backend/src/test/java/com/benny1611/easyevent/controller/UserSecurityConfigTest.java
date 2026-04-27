package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.auth.OAuthSuccessHandler;
import com.benny1611.easyevent.config.SecurityConfig;
import com.benny1611.easyevent.dto.ListUserResponse;
import com.benny1611.easyevent.dto.UserDTO;
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
import static org.mockito.Mockito.when;
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
    public void updatingUserWithRightRoleShouldReturn200() throws Exception {
        performPutRequestToUpdateUsers(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatingUserWithRightRoleShouldReturn200_ADMIN() throws Exception {
        performPutRequestToUpdateUsers(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void updatingUserWithRightRoleShouldReturn200_SUPER_ADMIN() throws Exception {
        performPutRequestToUpdateUsers(status().isOk());
    }

    private void performPutRequestToUpdateUsers(ResultMatcher expectedResult) throws Exception {
        UserDTO mockUserDTO = new UserDTO();
        when(userService.updateUser(any(), any(), any())).thenReturn(mockUserDTO);
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
    public void updatingUserByAdminWithRightRoleShouldReturn200_ADMIN() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void updatingUserByAdminWithRightRoleShouldReturn200_SUPER_ADMIN() throws Exception {
        performPutRequestToUpdateUsersByAdmin(status().isOk());
    }

    // update user by super admin tests

    @Test
    public void updatingUserBySuperAdminWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/users/update/admin/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void updatingUserBySuperAdminWithWrongRoleShouldReturn403() throws Exception {
        performPutRequestToUpdateUsersBySuperAdminAdmin(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updatingUserBySuperAdminWithWrongRoleShouldReturn403_USER() throws Exception {
        performPutRequestToUpdateUsersBySuperAdminAdmin(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatingUserBySuperAdminWithWrongRoleShouldReturn403_ADMIN() throws Exception {
        performPutRequestToUpdateUsersBySuperAdminAdmin(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void updatingUserBySuperAdminWithRightRoleShouldReturn200_SUPER_ADMIN() throws Exception {
        performPutRequestToUpdateUsersBySuperAdminAdmin(status().isOk());
    }

    private void performPutRequestToUpdateUsersByAdmin(ResultMatcher expectedResult) throws Exception {
        performPutRequestToUpdateUsersByAnAdmin(expectedResult, false);
    }
    private void performPutRequestToUpdateUsersBySuperAdminAdmin(ResultMatcher expectedResult) throws Exception {
        performPutRequestToUpdateUsersByAnAdmin(expectedResult, true);
    }

    private void performPutRequestToUpdateUsersByAnAdmin(ResultMatcher expectedResult, boolean sendToSuperAdmin) throws Exception {
        ListUserResponse mockResponse = new ListUserResponse();
        when(userService.updateUserByAdmin(any(), any(), any(), any())).thenReturn(mockResponse);
        when(userService.updateUserBySuperAdmin(any(), any(), any(), any())).thenReturn(mockResponse);
        String validJsonButWrongRole = """
    {
        "email": "test@test.com"
    }
    """;

        MockMultipartFile userPart = new MockMultipartFile(
                "changeUserRequest", "", "application/json", validJsonButWrongRole.getBytes()
        );
        String url;
        if (sendToSuperAdmin) {
            url = "/api/users/update/admin/1";
        } else {
            url = "/api/users/update/1";
        }

        mockMvc.perform(multipart(url)
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
