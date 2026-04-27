package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.auth.AuthenticatedUser;
import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.dto.ListUserResponse;
import com.benny1611.easyevent.dto.UserDTO;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.UserService;
import com.benny1611.easyevent.util.JwtUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
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

    @TestConfiguration
    static class TestSecurityConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(AuthenticatedUser.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {

                    return webRequest.getAttribute("TEST_USER", NativeWebRequest.SCOPE_REQUEST);
                }
            });
        }
    }

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

    @Test
    public void activationTest() throws Exception {
        UUID token = UUID.randomUUID();
        User user = new User();

        when(userService.activateUser(token)).thenReturn(user);
        JSONObject jo = new JSONObject();
        jo.put("token", token);

        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(jo.toString()))
                .andExpect(status().isOk());

        UUID fakeToken = UUID.randomUUID();
        JSONObject fakeTokenJo = new JSONObject();
        fakeTokenJo.put("token", fakeToken);

        when(userService.activateUser(fakeToken)).thenReturn(null);
        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(fakeTokenJo.toString()))
                .andExpect(status().isNotFound());

        JSONObject emptyJSON = new JSONObject();
        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJSON.toString()))
                .andExpect(status().isBadRequest());

        String invalidJSONString = "test";
        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJSONString))
                .andExpect(status().isBadRequest());


        JSONObject nullTokenJo = new JSONObject();
        nullTokenJo.put("token", null);

        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(nullTokenJo.toString()))
                .andExpect(status().isBadRequest());

        JSONObject blankTokenJo = new JSONObject();
        blankTokenJo.put("token", "");

        mockMvc.perform(post("/api/users/activate").contentType(MediaType.APPLICATION_JSON)
                        .content(blankTokenJo.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void resendActivationTest() throws Exception {
        mockMvc.perform(post("/api/users/resend-activation"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users/resend-activation?email=test"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/users/resend-activation?email=test@email.com"))
                .andExpect(status().isOk());
    }

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
        mockResponse.setId(42L);

        when(userService.updateUser(any(), any(UserDTO.class), any()))
                .thenReturn(mockResponse);
        when(userService.updateUser(isNull(), any(), any())).thenReturn(null);

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        AuthenticatedUser specificUser = new AuthenticatedUser(42L, "test@test.com", List.of(simpleGrantedAuthority));

        // All good test
        mockMvc.perform(multipart("/api/users/update")
                        .file(userPart)
                        .file(filePart)
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk());

        // All good test - no image
        mockMvc.perform(multipart("/api/users/update")
                        .file(userPart)
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk());

        // Empty request
        mockMvc.perform(multipart("/api/users/update")
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUserByAdminTest() throws Exception {
        String userDtoJson = """
    {
        "name": "test",
        "email": "test@test.com"
    }
    """;

        MockMultipartFile userPart = new MockMultipartFile(
                "changeUserRequest", "", "application/json", userDtoJson.getBytes()
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "profilePicture", "test.jpg", "image/jpeg", "data".getBytes()
        );

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        AuthenticatedUser specificUser = new AuthenticatedUser(42L, "test@test.com", List.of(simpleGrantedAuthority));

        ListUserResponse mockResponse = new ListUserResponse();
        when(userService.updateUserByAdmin(any(), eq(1L), any(), any())).thenReturn(mockResponse);
        when(userService.updateUserByAdmin(any(), eq(2L), any(), any())).thenReturn(null);

        // All good test
        mockMvc.perform(multipart("/api/users/update/1")
                        .file(userPart)
                        .file(filePart)
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk());

        // All good test - no image
        mockMvc.perform(multipart("/api/users/update/1")
                        .file(userPart)
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk());

        // service returns null test
        mockMvc.perform(multipart("/api/users/update/2")
                        .file(userPart)
                        .file(filePart)
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        // missing user part
        mockMvc.perform(multipart("/api/users/update/2")
                        .requestAttr("TEST_USER", specificUser)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
