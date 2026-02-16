package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.dto.UserDTO;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
import com.benny1611.easyevent.util.LocaleProvider;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final Set<String> ISO_LANGUAGES =
            Set.of(Locale.getISOLanguages());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ProfileImageService profileImageService;
    private final UserStateRepository userStateRepository;
    private final IMailService mailService;
    private final LocaleProvider localeProvider;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       ProfileImageService profileImageService,
                       @Qualifier("bcryptPasswordEncoder") PasswordEncoder passwordEncoder,
                       UserStateRepository userStateRepository,
                       IMailService mailService,
                       LocaleProvider localeProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.profileImageService = profileImageService;
        this.userStateRepository = userStateRepository;
        this.mailService = mailService;
        this.localeProvider = localeProvider;
    }

    @Transactional
    public User createUser(CreateUserRequest createUserRequest, MultipartFile profilePicture) throws IOException {
        User user = new User();

        user.setName(createUserRequest.getName());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roleNames = createUserRequest.getRoles();
        boolean isAllowedToBeCreatedByCurrentUser = false;
        if (roleNames.contains("ROLE_ADMIN") || roleNames.contains("ADMIN")) {
            if (auth != null) {
                isAllowedToBeCreatedByCurrentUser = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        } else {
            isAllowedToBeCreatedByCurrentUser = true;
        }
        if (isAllowedToBeCreatedByCurrentUser) {
            Set<Role> roles = roleNames.stream()
                    .map(roleName -> roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            throw new AccessDeniedException("You can't perform that operation");
        }

        UserState activeState = userStateRepository.findByName("ACTIVE").orElseThrow(() -> new RuntimeException("Could not find active state"));
        user.setState(activeState);

        user.setActive(false);
        UUID activationToken = UUID.randomUUID();
        user.setActivationToken(activationToken);
        user.setActivationSentAt(OffsetDateTime.now());

        user = userRepository.save(user);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePicUrl = profileImageService.saveAsPng(profilePicture, user.getId());

            user.setProfilePictureUrl(profilePicUrl);
            user = userRepository.save(user);
        }

        mailService.sendActivationEmail(user);

        return user;
    }

    @Transactional
    public User createUser(String email, String name, String pictureUrl) throws IOException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(null);
        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationSentAt(null);
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Could not find the user role"));
        user.setRoles(Set.of(role));
        UserState activeState = userStateRepository.findByName("ACTIVE").orElseThrow(() -> new RuntimeException("Could not find active state"));
        user.setState(activeState);

        user = userRepository.save(user);

        byte[] profilePicData;
        try {
            LOG.info("Profile pic URL: {}", pictureUrl);
            profilePicData = ProfileImageService.downloadImage(pictureUrl);
        } catch (IOException | InterruptedException | RuntimeException e) {
            profilePicData = null;
        }

        String profilePicUrl = null;
        if (profilePicData != null) {
            profilePicUrl = profileImageService.saveAsPng(profilePicData, user.getId());
        }
        if (profilePicUrl != null) {
            user.setProfilePictureUrl(profilePicUrl);
            user = userRepository.save(user);
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User activateUser(UUID token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(true);
            user.setActivationToken(null);
            user.setActivationSentAt(null);
            userRepository.save(user);
            return user;
        } else {
            return null;
        }
    }

    public void resendActivation(@Email String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isActive()) {
                UUID token = UUID.randomUUID();
                user.setActivationToken(token);
                user.setActivationSentAt(OffsetDateTime.now());
                userRepository.save(user);
                mailService.sendActivationEmail(user);
            }
        }
    }

    public UserDTO updateUser(String email, UserDTO userDTO, MultipartFile profilePicture) throws IOException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        UserDTO result = null;
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String emailChange = userDTO.getEmail();
            result = new UserDTO();
            boolean used = false;
            if (emailChange != null) {
                Optional<User> checkIfEmailAlreadyExists = userRepository.findByEmail(emailChange);
                if (checkIfEmailAlreadyExists.isEmpty()) {
                    user.setEmail(emailChange);
                    user.setActive(false);

                    UUID activationToken = UUID.randomUUID();
                    user.setActivationToken(activationToken);
                    user.setActivationSentAt(OffsetDateTime.now());

                    mailService.sendActivationEmail(user);
                    result.setEmail(emailChange);
                    used = true;
                }
            }
            if (userDTO.getName() != null) {
                String nameChange = userDTO.getName();
                user.setName(nameChange);
                result.setName(nameChange);
                used = true;
            }
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String profilePicUrl = profileImageService.saveAsPng(profilePicture, user.getId());
                user.setProfilePictureUrl(profilePicUrl);
                result.setProfilePicture(profilePicUrl);
                used = true;
            }
            if (userDTO.getLanguage() != null) {
                String language = userDTO.getLanguage();
                Locale requested = Locale.forLanguageTag(language);
                if (localeProvider.supports(requested)) {
                    user.setLanguage(language);
                    result.setLanguage(language);
                    used = true;
                }
            }
            if (used) {
                userRepository.save(user);
            } else {
                return null;
            }
        }

        return result;
    }

    private static boolean isValidLanguage(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return ISO_LANGUAGES.contains(input.toLowerCase());
    }
}
