package com.benny1611.easyevent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


import com.benny1611.easyevent.auth.AuthenticatedUser;
import com.benny1611.easyevent.dao.*;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.dto.UserDTO;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
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

    @Test
    @DisplayName("Should create user and download profile picture successfully")
    void createUser_WithUrl_Success() throws IOException {
        // Arrange
        String email = "oauth@test.com";
        String name = "OAuth User";
        String picUrl = "https://example.com/photo.jpg";
        byte[] mockBytes = new byte[]{1, 2, 3};

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        UserState activeState = new UserState();
        activeState.setName("ACTIVE");
        when(userStateRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeState));

        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            if (u.getId() == null) u.setId((long) (Math.random() * 100000L)); // Simulate DB assigning ID
            return u;
        });

        when(profileImageService.saveAsPng(eq(mockBytes), any())).thenReturn("http://cdn.com/saved.png");

        // Mocking the static method
        try (MockedStatic<ProfileImageService> mockedStatic = mockStatic(ProfileImageService.class)) {
            mockedStatic.when(() -> ProfileImageService.downloadImage(picUrl)).thenReturn(mockBytes);

            // Act
            User result = userService.createUser(email, name, picUrl);

            // Assert
            assertNotNull(result);
            assertEquals("http://cdn.com/saved.png", result.getProfilePictureUrl());
            assertEquals(email, result.getEmail());
            verify(userRepository, times(2)).save(any(User.class));
        }
    }

    @Test
    @DisplayName("Should create user even if profile picture download fails")
    void createUser_WithUrl_DownloadFails() throws IOException {
        // Arrange
        String email = "oauth@test.com";
        String name = "OAuth User";
        String picUrl = "https://broken-link.com/photo.jpg";

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        UserState activeState = new UserState();
        activeState.setName("ACTIVE");
        when(userStateRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeState));

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Simulate static method throwing an exception
        try (MockedStatic<ProfileImageService> mockedStatic = mockStatic(ProfileImageService.class)) {
            mockedStatic.when(() -> ProfileImageService.downloadImage(anyString()))
                    .thenThrow(new RuntimeException("Network Error"));

            // Act
            User result = userService.createUser(email, name, picUrl);

            // Assert
            assertNotNull(result);
            assertNull(result.getProfilePictureUrl(), "Profile URL should be null if download fails");
            // Should only save once because the second save is inside the if(profilePicUrl != null) block
            verify(userRepository, times(1)).save(any(User.class));
            verify(profileImageService, never()).saveAsPng((MultipartFile) any(), any());
        }
    }

    @Test
    @DisplayName("Should throw RuntimeException when ROLE_USER is missing in DB")
    void createUser_RoleNotFound_ThrowsException() {
        // Arrange
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.createUser("test@test.com", "Test", null);
        });

        assertEquals("Could not find the user role", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return UserDTO when principal and user exist")
    void findById_Success() {
        // Arrange
        AuthenticatedUser principal = mock(AuthenticatedUser.class);
        when(principal.getUserId()).thenReturn(1L);

        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setLanguage("en");
        user.setProfilePictureUrl("http://image.url");
        user.setPassword(null); // Testing the logic: setLocalPasswordSet(true) if null

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.findById(principal);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertTrue(result.isLocalPasswordSet()); // Based on user.getPassword() == null
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return UserDTO when principal and user exist")
    void findById_Success_2() {
        // Arrange
        AuthenticatedUser principal = mock(AuthenticatedUser.class);
        when(principal.getUserId()).thenReturn(1L);

        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setLanguage("en");
        user.setProfilePictureUrl("http://image.url");
        user.setPassword("test"); // Testing the logic: setLocalPasswordSet(true) if null

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.findById(principal);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertFalse(result.isLocalPasswordSet()); // Based on user.getPassword() == null
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null when principal or ID is null")
    void findById_NullInput() {
        assertNull(userService.findById(null));

        AuthenticatedUser principalNoId = mock(AuthenticatedUser.class);
        when(principalNoId.getUserId()).thenReturn(null);
        assertNull(userService.findById(principalNoId));
    }

    @Test
    @DisplayName("Should return null when user is not found in database")
    void findById_NotFound() {
        AuthenticatedUser principal = mock(AuthenticatedUser.class);
        when(principal.getUserId()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(userService.findById(principal));
    }

    @Test
    @DisplayName("Should delegate call to repository")
    void findByEmail_Delegate() {
        String email = "find@me.com";
        userService.findByEmail(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should activate user and clear token when token is valid")
    void activateUser_Success() {
        // Arrange
        UUID token = UUID.randomUUID();
        User user = new User();
        user.setActivationToken(token);

        UserState activeState = new UserState();
        activeState.setName("ACTIVE");
        when(userStateRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeState));

        when(userRepository.findByActivationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        User result = userService.activateUser(token);

        // Assert
        assertNotNull(result);
        assertNull(result.getActivationToken());
        assertNull(result.getActivationSentAt());
        // Verify setUserStateActive was effectively called (assuming it changes a field)
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should return null if token does not exist")
    void activateUser_InvalidToken() {
        UUID token = UUID.randomUUID();
        when(userRepository.findByActivationToken(token)).thenReturn(Optional.empty());

        User result = userService.activateUser(token);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update token and send mail when user is NOT active")
    void resendActivation_UserNotActive_Success() {
        // Arrange
        String email = "inactive@test.com";
        User user = new User();
        user.setEmail(email);

        // Mocking the state hierarchy: user -> state -> name
        UserState inactiveState = mock(UserState.class);
        when(inactiveState.getName()).thenReturn("PENDING");
        user.setState(inactiveState);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        userService.resendActivation(email);

        // Assert
        assertNotNull(user.getActivationToken(), "A new token should have been generated");
        assertNotNull(user.getActivationSentAt(), "Timestamp should be set");

        verify(userRepository).save(user);
        verify(mailService).sendActivationEmail(user);
    }

    @Test
    @DisplayName("Should do nothing if user is already ACTIVE")
    void resendActivation_UserAlreadyActive_DoesNothing() {
        // Arrange
        String email = "active@test.com";
        User user = new User();

        UserState activeState = mock(UserState.class);
        when(activeState.getName()).thenReturn("ACTIVE");
        user.setState(activeState);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        userService.resendActivation(email);

        // Assert
        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(any());
    }

    @Test
    @DisplayName("Should do nothing if user is not found")
    void resendActivation_UserNotFound_DoesNothing() {
        // Arrange
        String email = "ghost@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        userService.resendActivation(email);

        // Assert
        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendActivationEmail(any());
    }

    @Test
    @DisplayName("Should generate a unique token every time it is called")
    void resendActivation_GeneratesNewToken() {
        // Arrange
        String email = "inactive@test.com";
        User user = new User();
        UUID oldToken = UUID.randomUUID();
        user.setActivationToken(oldToken);

        UserState inactiveState = mock(UserState.class);
        when(inactiveState.getName()).thenReturn("INACTIVE");
        user.setState(inactiveState);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        userService.resendActivation(email);

        // Assert
        assertNotEquals(oldToken, user.getActivationToken(), "The old token should have been replaced");
        verify(userRepository).save(user);
    }
}
