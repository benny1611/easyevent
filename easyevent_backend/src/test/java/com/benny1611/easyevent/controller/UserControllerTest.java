package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.UserService;
import com.benny1611.easyevent.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private RoleRepository roleRepository;

    @Test
    void userCreateSuccessTest() throws Exception {
        CreateUserRequest mockRequest = new CreateUserRequest();
        mockRequest.setEmail("test@email.com");
        mockRequest.setName("test");
        mockRequest.setPassword("Password1234!!");
        mockRequest.setRoles(Set.of("USER_ROLE"));

        User user = new User();

        when(userService.createUser(mockRequest, null)).thenReturn(user);

        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "test@email.com")
                        .param("password", "Password1234!!")
                        .param("name", "test")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    void incorrectDataTest() throws Exception {
        // missing mail
        mockMvc.perform(multipart("/api/users/create")
                        .param("password", "Password1234!!")
                        .param("name", "test")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // incorrect mail
        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "testemail.com")
                        .param("password", "Password1234!!")
                        .param("name", "test")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // missing password
        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "test@email.com")
                        .param("name", "test")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // password not strong enough
        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "test@email.com")
                        .param("password", "test")
                        .param("name", "test")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // missing name
        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "test@email.com")
                        .param("password", "Password1234!!")
                        .param("roles", "USER_ROLE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // missing roles
        mockMvc.perform(multipart("/api/users/create")
                        .param("email", "test@email.com")
                        .param("password", "Password1234!!")
                        .param("name", "test")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
