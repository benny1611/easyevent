package com.benny1611.easyevent.controller;

import com.benny1611.easyevent.dto.CreateUserRequest;
import com.benny1611.easyevent.entity.User;
import com.benny1611.easyevent.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // TODO: Use UserDTO instead
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> createUser(@Valid @ModelAttribute CreateUserRequest request,
                                           @RequestPart(required = false) MultipartFile profilePicture) throws IOException {
        User user = userService.createUser(request, profilePicture);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // TODO: Use ResendActivationMailRequest instead
    @GetMapping("/activate")
    public ResponseEntity<Void> activateUser (@RequestParam UUID token) {
        User user = userService.activateUser(token);
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
}
