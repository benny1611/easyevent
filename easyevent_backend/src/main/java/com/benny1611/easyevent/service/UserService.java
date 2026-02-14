package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserState;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ProfileImageService profileImageService;
    private final UserStateRepository userStateRepository;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       ProfileImageService profileImageService,
                       @Qualifier("bcryptPasswordEncoder") PasswordEncoder passwordEncoder,
                       UserStateRepository userStateRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.profileImageService = profileImageService;
        this.userStateRepository = userStateRepository;
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

        user = userRepository.save(user);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePicUrl = profileImageService.saveAsPng(profilePicture, user.getId());

            user.setProfilePictureUrl(profilePicUrl);
            user = userRepository.save(user);
        }

        return user;
    }

    @Transactional
    public User createUser(String email, String name, String pictureUrl) throws IOException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(null);
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

}
