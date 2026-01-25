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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Path UPLOAD_ROOT = Paths.get("uploads/users");

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

            saveAsPng(profilePicture, avatarPath);

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

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Invalid or corrupted image");
        }
    }

    private static void saveAsPng(MultipartFile file, Path outputPath) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Corrupted PNG file");
        }

        BufferedImage resized = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, 256, 256, null);
        g.dispose();
        ImageIO.write(resized, "png", outputPath.toFile());
    }
}
