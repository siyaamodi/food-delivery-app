package com.example.food_delivery.controller;

import com.example.food_delivery.dto.LoginRequest;
import com.example.food_delivery.dto.LoginResponse;
import com.example.food_delivery.dto.UserRequest;
import com.example.food_delivery.dto.UserResponse;
import com.example.food_delivery.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse response = userService.registerUser(userRequest);

        HttpStatus status = Boolean.TRUE.equals(response.getSuccess())
                ? HttpStatus.CREATED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.loginUser(loginRequest);

        HttpStatus status = Boolean.TRUE.equals(response.getSuccess())
                ? HttpStatus.OK
                : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);

        HttpStatus status = Boolean.TRUE.equals(response.getSuccess())
                ? HttpStatus.OK
                : HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.status(HttpStatus.OK).body("JWT Security is working!");
    }
}
