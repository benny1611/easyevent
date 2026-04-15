package com.benny1611.easyevent.service;

import com.benny1611.easyevent.auth.AuthenticatedUser;
import com.benny1611.easyevent.dao.RoleRepository;
import com.benny1611.easyevent.dao.UserBanLogRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dao.UserStateRepository;
import com.benny1611.easyevent.dto.*;
import com.benny1611.easyevent.entity.Role;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.entity.UserBanLog;
import com.benny1611.easyevent.entity.UserState;
import com.benny1611.easyevent.util.JwtUtils;
import com.benny1611.easyevent.util.LocaleProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class UserService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ProfileImageService profileImageService;
    private final UserStateRepository userStateRepository;
    private final UserBanLogRepository userBanLogRepository;
    private final IMailService mailService;
    private final LocaleProvider localeProvider;
    private final JwtUtils jwtUtils;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       ProfileImageService profileImageService,
                       @Qualifier("bcryptPasswordEncoder") PasswordEncoder passwordEncoder,
                       UserStateRepository userStateRepository,
                       UserBanLogRepository userBanLogRepository,
                       IMailService mailService,
                       LocaleProvider localeProvider, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.profileImageService = profileImageService;
        this.userStateRepository = userStateRepository;
        this.userBanLogRepository = userBanLogRepository;
        this.mailService = mailService;
        this.localeProvider = localeProvider;
        this.jwtUtils = jwtUtils;
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

    public UserDTO findById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            userDTO.setLanguage(user.getLanguage());
            userDTO.setProfilePicture(user.getProfilePictureUrl());
            userDTO.setLocalPasswordSet(user.getPassword() == null);
            return userDTO;
        } else {
            return null;
        }
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

    public ListUserResponse updateUserByAdmin(AuthenticatedUser principal, Long userId, @Valid ChangeUserRequest changeUserRequest, MultipartFile profilePicture) throws IOException {
        User target = userRepository.findByIdWithRolesAndState(userId).orElseThrow(() -> new RuntimeException("Target user not found"));
        User actor = userRepository.findByIdWithRoles(principal.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        boolean canModify = canModifyUser(actor, target);
        ListUserResponse result = null;
        if (canModify) {
            result = new ListUserResponse();
            result.setId(target.getId());
            result.setActive(target.isActive());
            result.setEmail(target.getEmail());
            result.setProfilePicture(target.getProfilePictureUrl());
            result.setBanned(isUserBanned(target));
            List<String> roles = target.getRoles().stream().map(Role::getName).toList();
            result.setRoles(roles);
            boolean used = false;
            if (changeUserRequest.getName() != null && !changeUserRequest.getName().equals(target.getName())) {
                String nameChange = changeUserRequest.getName();
                target.setName(nameChange);
                result.setName(nameChange);
                used = true;
            } else {
                result.setName(target.getName());
            }
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String profilePicUrl = profileImageService.saveAsPng(profilePicture, target.getId());
                target.setProfilePictureUrl(profilePicUrl);
                used = true;
            }
            if (used) {
                userRepository.save(target);
            }
        }
        return result;
    }

    public ListUserResponse updateUserBySuperAdmin(AuthenticatedUser principal, Long userId, @Valid ChangeUserRequest changeUserRequest, MultipartFile profilePicture) throws IOException {
        User target = userRepository.findByIdWithRolesAndState(userId).orElseThrow(() -> new RuntimeException("Target user not found"));
        User actor = userRepository.findByIdWithRoles(principal.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        boolean canModify = canModifyUser(actor, target);
        if (canModify) {
            ListUserResponse response = new ListUserResponse();
            boolean mailChanged = false;
            String newMailAddress = changeUserRequest.getEmail();
            if (newMailAddress != null) {
                if (!newMailAddress.isBlank()) { // just to be sure
                    mailChanged = changeMailAddress(newMailAddress, target, false);
                }
            }

            String newName = changeUserRequest.getName();
            boolean nameChanged = changeName(newName, target);

            if (nameChanged || mailChanged) {
                userRepository.save(target);
            }
            response.setId(target.getId());
            response.setName(target.getName());
            response.setEmail(target.getEmail());
            response.setProfilePicture(target.getProfilePictureUrl());
            response.setActive(target.isActive());
            response.setBanned(isUserBanned(target));
            List<String> roles = target.getRoles().stream().map(Role::getName).toList();
            response.setRoles(roles);
            return response;
        } else {
            return null;
        }
    }

    public UserDTO updateUser(Long id, UserDTO userDTO, MultipartFile profilePicture) throws IOException {
        Optional<User> userOptional = userRepository.findById(id);
        UserDTO result = null;
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            result = new UserDTO();
            result.setLocalPasswordSet(user.getPassword() != null);
            boolean used = false;
            boolean refreshToken = false;

            String newMailAddress = userDTO.getEmail();
            boolean mailChanged = changeMailAddress(newMailAddress, user, true);
            if (mailChanged) {
                result.setEmail(newMailAddress);
                used = true;
                refreshToken = true;
            }

            String newName = userDTO.getName();
            boolean nameChanged = changeName(newName, user);
            if (nameChanged) {
                result.setName(newName);
                used = true;
                refreshToken = true;
            }

            if (profilePicture != null && !profilePicture.isEmpty()) {
                String profilePicUrl = profileImageService.saveAsPng(profilePicture, user.getId());
                user.setProfilePictureUrl(profilePicUrl);
                result.setProfilePicture(profilePicUrl);
                used = true;
                refreshToken = true;
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

            if (userDTO.getNewPassword() != null) {
                if (user.getPassword() == null) {
                    // OAuth-only user setting first password
                    user.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
                    refreshToken = true;
                    used = true;
                } else {
                    // Password user changing password
                    if (!passwordEncoder.matches(userDTO.getOldPassword(), user.getPassword())) {
                        throw new IllegalArgumentException("PASSWORD_INCORRECT");
                    }
                    user.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
                    used = true;
                }
            }

            if (used) {
                userRepository.save(user);
                if (refreshToken) {
                    String token = jwtUtils.generateToken(user);
                    result.setToken(token);
                }
            } else {
                return null;
            }
        }
        return result;
    }

    public Page<ListUserResponse> getAllUsers(Pageable pageable) {
        Page<Long> page = userRepository.findUserIds(pageable);
        List<User> users = userRepository.findAllByIdWithRolesAndState(page.getContent());

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<ListUserResponse> dtos = page.getContent().stream()
                .map(userMap::get)
                .map(user -> {
                    ListUserResponse userDTO = new ListUserResponse();
                    userDTO.setId(user.getId());
                    userDTO.setName(user.getName());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setProfilePicture(user.getProfilePictureUrl());
                    userDTO.setActive(user.isActive());
                    userDTO.setBanned(isUserBanned(user));
                    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
                    userDTO.setRoles(roles);
                    return userDTO;
                }).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private boolean changeMailAddress(String newMailAddress, User user, boolean sendActivationMail) {
        if (newMailAddress != null && !newMailAddress.equalsIgnoreCase(user.getEmail())) {
            Optional<User> checkIfEmailAlreadyExists = userRepository.findByEmail(newMailAddress);
            if (checkIfEmailAlreadyExists.isEmpty()) {
                user.setEmail(newMailAddress);

                if (sendActivationMail) {
                    user.setActive(false);

                    UUID activationToken = UUID.randomUUID();
                    user.setActivationToken(activationToken);
                    user.setActivationSentAt(OffsetDateTime.now());

                    mailService.sendActivationEmail(user);
                }
                return true;
            } else {
                throw new IllegalArgumentException("Email already in use");
            }
        } else {
            return false;
        }
    }

    private boolean changeName(String newName, User user) {
        if (newName != null && !newName.equals(user.getName())) {
            user.setName(newName);
            return true;
        } else {
            return false;
        }
    }

    private boolean canModifyUser(User actor, User target) {
        if (hasRole(actor, "ROLE_SUPER_ADMIN")) {
            return true;
        }
        if (hasRole(actor, "ROLE_ADMIN") && hasRole(target, "ROLE_USER")) {
            return true;
        }
        if (actor.getId().longValue() == target.getId().longValue()) {
            return true;
        }
        return false;
    }

    private static boolean hasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    private boolean isUserBanned(User user) {
        UserState blockedState = userStateRepository.findByName("BLOCKED").orElseThrow(() -> new RuntimeException("Could not find active state"));
        if (user.getState().getId() != null && blockedState.getId() != null) {
            return user.getState().getId().longValue() == blockedState.getId().longValue();
        } else {
            return false;
        }
    }

    public boolean banUserById(AuthenticatedUser principal, Long userId, BanRequest banRequest) {
        User target = userRepository.findByIdWithRoles(userId).orElseThrow(() -> new RuntimeException("Target user not found"));
        User actor = userRepository.findByIdWithRoles(principal.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        boolean canModify = canModifyUser(actor, target);
        if (actor.getId().longValue() == target.getId().longValue()) {
            // A user can't ban themselves
            return false;
        }
        if (canModify) {
            UserBanLog userBanLog = new UserBanLog();
            userBanLog.setActionType(UserBanLog.ActionType.BAN);
            userBanLog.setAdmin(actor);
            userBanLog.setTargetUser(target);
            userBanLog.setReason(banRequest.getReason());
            userBanLogRepository.save(userBanLog);
            mailService.sendBanMail(target, banRequest.getReason());
            return true;
        } else {
            return false;
        }
    }

    public boolean unbanUserById(AuthenticatedUser principal, Long userId) {
        User target = userRepository.findByIdWithRoles(userId).orElseThrow(() -> new RuntimeException("Target user not found"));
        User actor = userRepository.findByIdWithRoles(principal.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        boolean canModify = canModifyUser(actor, target);
        if (canModify) {
            UserBanLog userBanLog = new UserBanLog();
            userBanLog.setActionType(UserBanLog.ActionType.UNBAN);
            userBanLog.setAdmin(actor);
            userBanLog.setTargetUser(target);
            userBanLog.setReason(null);
            userBanLogRepository.save(userBanLog);
            mailService.sendUnbanMail(target);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeUserRoles(AuthenticatedUser principal, Long userId, ChangeRolesRequest changeRolesRequest) {
        List<String> roles = changeRolesRequest.getRoles();
        if (roles != null) {
            if (!roles.isEmpty()) {

                String roleString = roles.getFirst();
                Optional<Role> roleOptional = roleRepository.findByName(roleString);
                if (roleOptional.isPresent()) {
                    User target = userRepository.findByIdWithRoles(userId).orElseThrow(() -> new RuntimeException("Target user not found"));
                    User actor = userRepository.findByIdWithRoles(principal.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

                    if (hasRole(actor, "ROLE_ADMIN") || hasRole(actor, "ROLE_SUPER_ADMIN")) {

                        Role targetsRole = target.getRoles().iterator().next();

                        Role role = roleOptional.get();
                        if (targetsRole.getName().equalsIgnoreCase(role.getName())) {
                            return true;
                        } else if (role.getName().equalsIgnoreCase("ROLE_USER")) {
                             if (hasRole(target, "ROLE_ADMIN")) {
                                if (hasRole(actor, "ROLE_SUPER_ADMIN") ||
                                        actor.getId().longValue() == target.getId().longValue()) {
                                    changeRole(target, "ROLE_USER");
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else if (role.getName().equalsIgnoreCase("ROLE_ADMIN")) {
                            if (hasRole(target, "ROLE_USER") ||
                                    actor.getId().longValue() == target.getId().longValue()) {
                                changeRole(target, "ROLE_ADMIN");
                                return true;
                            } else {
                                return false;
                            }
                        } else if (role.getName().equalsIgnoreCase("ROLE_SUPER_ADMIN")) {
                            if (hasRole(actor, "ROLE_SUPER_ADMIN")) {
                                changeRole(target, "ROLE_SUPER_ADMIN");
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void changeRole(User target, String role) {
        Role userRole = roleRepository.findByName(role).orElseThrow(() -> new RuntimeException("Could not find role: " + role));
        Set<Role> userRoleSet = Set.of(userRole);
        target.setRoles(userRoleSet);
        userRepository.save(target);
    }
}
