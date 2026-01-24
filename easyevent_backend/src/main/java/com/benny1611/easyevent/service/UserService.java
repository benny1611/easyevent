package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Path UPLOAD_ROOT = Paths.get("uploads/users");
    private static final byte[] PNG_MAGIC = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
    };

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

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

        user = userRepository.save(user);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            validateImage(profilePicture);

            Path userDir = UPLOAD_ROOT.resolve(String.valueOf(user.getId()));
            Files.createDirectories(userDir);

            Path avatarPath = userDir.resolve("avatar.png");
            profilePicture.transferTo(avatarPath.toFile());

            user.setProfilePictureUrl("/users/" + user.getId() + "/avatar.png");
            user = userRepository.save(user);
        }

        return user;
    }

    private static void validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Max file size is 5MB");
        }
        if (!"image/png".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Only PNG images are allowed");
        }
        try (InputStream is = file.getInputStream()) {
            byte[] header = is.readNBytes(8);

            if (header.length < 8 || !Arrays.equals(header, PNG_MAGIC)) {
                throw new IllegalArgumentException("Invalid PNG file");
            }
        }
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Corrupted PNG file");
        }
    }
}
