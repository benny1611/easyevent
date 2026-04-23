package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.auth.AuthenticatedUser;
import com.benny1611.easyevent.auth.OAuthSuccessHandler;
import com.benny1611.easyevent.config.SecurityConfig;
import com.benny1611.easyevent.dto.UserDTO;
import com.benny1611.easyevent.service.CustomUserDetailsService;
import com.benny1611.easyevent.service.UserService;
import com.benny1611.easyevent.util.JwtAuthenticationFilter;
import com.benny1611.easyevent.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // if needed
public class UserControllerWithSecurityTest {
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
    private PasswordEncoder bcryptPasswordEncoder;

    @Test
    public void updateUserTest() throws Exception {
        String userDtoJson = """
    {
        "name": "test",
        "id": 1,
        "email": "test@test.com",
        "language": "en",
        "oldPassword": "test",
        "newPassword": "test1234!"
    }
    """;

        MockMultipartFile userPart = new MockMultipartFile(
                "userDTO", "", "application/json", userDtoJson.getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "profilePicture", "test.jpg", "image/jpeg", "data".getBytes()
        );

        UserDTO mockResponse = new UserDTO();
        mockResponse.setId(1L);
        when(userService.updateUser(eq(1L), eq(mockResponse), eq(filePart))).thenReturn(mockResponse);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/users/update")
                        .file(userPart)
                        .file(filePart)
                        .with(user("test@test.com").roles("USER"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }
}
