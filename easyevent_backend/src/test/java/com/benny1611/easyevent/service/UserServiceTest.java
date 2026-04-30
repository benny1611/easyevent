package com.benny1611.easyevent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.Set;


import com.benny1611.easyevent.dao.*;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.exception.AccountSoftDeletedException;
import com.benny1611.easyevent.exception.RoleNotFoundException;
import com.benny1611.easyevent.util.JwtUtils;
import com.benny1611.easyevent.util.LocaleProvider;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfileImageService profileImageService;
    @Mock private IMailService mailService;
    @Mock private EntityManager entityManager;
    @Mock private Session session;

    // These are required for the constructor but not used in the specific method
    @Mock private UserStateRepository userStateRepository;
    @Mock private UserBanLogRepository userBanLogRepository;
    @Mock private DeletionLogRepository logRepository;
    @Mock private UserRecoveryLogRepository recoveryLogRepository;
    @Mock private LocaleProvider localeProvider;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "entityManager", entityManager);
        lenient().when(entityManager.unwrap(Session.class)).thenReturn(session);

        validRequest = new CreateUserRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("securePassword");
        validRequest.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    @DisplayName("Should successfully create a user")
    void createUser_Success() throws IOException {
        // Arrange
        CreateUserRequest validRequest = new CreateUserRequest();
        validRequest.setEmail("test@test.com");
        validRequest.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        String picture = "smile";
        MockMultipartFile profilePicture = new MockMultipartFile(
                "profilePicture", "", "application/json", picture.getBytes()
        );
        MockMultipartFile emptyProfilePicture = new MockMultipartFile(
                "profilePicture", "", "application/json", new byte[]{}
        );
        when(profileImageService.saveAsPng(profilePicture, null)).thenReturn("/profile/picture");


        // Act
        User resultWithProfilePicture = userService.createUser(validRequest, profilePicture);
        User resultWithEmptyProfilePicture = userService.createUser(validRequest, emptyProfilePicture);
        User resultWithoutPicture = userService.createUser(validRequest, null);

        // Assert
        assertNotNull(resultWithProfilePicture);
        assertNotNull(resultWithEmptyProfilePicture);
        assertNotNull(resultWithoutPicture);
        verify(session, times(3)).disableFilter("deletedUserFilter"); // Verify it was called
        verify(userRepository, times(4)).save(any(User.class));
        verify(profileImageService, times(1)).saveAsPng(profilePicture, null);
    }

    @Test
    @DisplayName("Should throw exception when email is already taken (soft-deleted)")
    void createUser_ThrowsAccountSoftDeletedException() {
        // Arrange
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(AccountSoftDeletedException.class, () -> {
            userService.createUser(validRequest, null);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when attempting to create an Admin")
    void createUser_ThrowsAccessDeniedException_ForAdminRole() {
        // Arrange
        validRequest.setRoles(Set.of("ROLE_ADMIN"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userService.createUser(validRequest, null);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when role does not exist")
    void createUser_ThrowsRoleNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> {
            userService.createUser(validRequest, null);
        });
    }
}
