package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.auth.AuthenticatedUser;
import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.dto.ActivationMailRequest;
import com.benny1611.easyevent.dto.UserDTO;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @ModelAttribute CreateUserRequest request,
                                              @RequestPart(required = false) MultipartFile profilePicture) throws IOException {
        User user = userService.createUser(request, profilePicture);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setProfilePicture(user.getProfilePictureUrl());
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateUser (@Valid @RequestBody ActivationMailRequest request) {
        User user = userService.activateUser(request.getToken());
        if (user != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Void> resendActivation(@RequestParam @Email String email) {
        userService.resendActivation(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> updateUser(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestPart("userDTO") @Valid UserDTO userDTO,
            @RequestPart(value = "profilePicture", required = false)
            MultipartFile profilePicture
    ) throws IOException {
        UserDTO user = userService.updateUser(principal.getUserId(), userDTO, profilePicture);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(
            value = "/update/{userId}"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId,
                                              @AuthenticationPrincipal AuthenticatedUser principal,
                                              @RequestPart("userDTO") @Valid UserDTO userDTO,
                                              @RequestPart(value = "profilePicture", required = false)
                                              MultipartFile profilePicture) throws IOException {
        UserDTO user = userService.updateUser(principal, userId, userDTO, profilePicture);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal AuthenticatedUser principal) {
        return getUser(principal.getUserId());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getSpecificUser(@PathVariable Long userId) {
        return getUser(userId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedModel<EntityModel<UserDTO>> getAllUsers(@PageableDefault(size = 20, sort = "registeredAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                        PagedResourcesAssembler<UserDTO> assembler) {
        Page<UserDTO> page = userService.getAllUsers(pageable);
        return assembler.toModel(page);
    }

    private ResponseEntity<UserDTO> getUser(Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            userDTO.setLanguage(user.getLanguage());
            userDTO.setProfilePicture(user.getProfilePictureUrl());
            userDTO.setActive(user.isActive());
            userDTO.setLocalPasswordSet(user.getPassword() == null);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
