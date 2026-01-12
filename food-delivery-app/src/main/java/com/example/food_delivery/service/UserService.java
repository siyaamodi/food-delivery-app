package com.example.food_delivery.service;

import com.example.food_delivery.config.JwtService;
import com.example.food_delivery.dto.LoginRequest;
import com.example.food_delivery.dto.LoginResponse;
import com.example.food_delivery.dto.UserRequest;
import com.example.food_delivery.dto.UserResponse;
import com.example.food_delivery.entity.User;
import com.example.food_delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserResponse registerUser(UserRequest userRequest) {
        if (userRepository.existsByEmailId(userRequest.getEmailId())) {
            return UserResponse.builder()
                    .message("Email already registered: " + userRequest.getEmailId())
                    .success(false)
                    .build();
        }
        if (userRepository.existsByMobileNo(userRequest.getMobileNo())) {
            return UserResponse.builder()
                    .message("Mobile number already registered: " + userRequest.getMobileNo())
                    .success(false)
                    .build();
        }


        User user = User.builder()
                    .name(userRequest.getName())
                    .age(userRequest.getAge())
                    .address(userRequest.getAddress())
                    .gender(userRequest.getGender())
                    .mobileNo(userRequest.getMobileNo())
                    .emailId(userRequest.getEmailId())
                    .password(passwordEncoder.encode(userRequest.getPassword()))
                    .role(userRequest.getRole())
                    .build();

            // Save user
            User savedUser = userRepository.save(user);

            // Return response without password
            return UserResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .age(savedUser.getAge())
                    .address(savedUser.getAddress())
                    .gender(savedUser.getGender())
                    .mobileNo(savedUser.getMobileNo())
                    .emailId(savedUser.getEmailId())
                    .role(savedUser.getRole())
                    .message("User registered successfully")
                    .success(true)
                    .build();

    }
    public List<UserResponse> getAllUsers(){
            List<User> users=userRepository.findAll();
            return users.stream().map(user->UserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .age(user.getAge())
                            .gender(user.getGender())
                            .address(user.getAddress())
                            .mobileNo(user.getMobileNo())
                            .emailId(user.getEmailId())
                            .role(user.getRole())
                            .success(true)
                            .build())
                    .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .age(user.getAge())
                        .address(user.getAddress())
                        .gender(user.getGender())
                        .mobileNo(user.getMobileNo())
                        .emailId(user.getEmailId())
                        .role(user.getRole())
                        .success(true)
                        .build())
                        .orElse(UserResponse.builder()
                        .message("User not found: " + userId)
                        .success(false)
                        .build());
    }
    public LoginResponse loginUser(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmailId());

        try {
            // Authenticate user with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmailId(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtil.generateToken(loginRequest.getEmailId());

            // Get user details
            User user = userRepository.findByEmailId(loginRequest.getEmailId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("Login successful for user: {}", user.getEmailId());

            return LoginResponse.builder()
                    .token(jwt)
                    .emailId(user.getEmailId())
                    .name(user.getName())
                    .role(user.getRole())
                    .userId(user.getId())
                    .success(true)
                    .message("Login successful")
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", loginRequest.getEmailId());
            return LoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        } catch (Exception e) {
            log.error("Login error for email: {} - {}", loginRequest.getEmailId(), e.getMessage());
            return LoginResponse.builder()
                    .success(false)
                    .message("Authentication failed")
                    .build();
        }
    }
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn("No authenticated user found");
                return null;
            }

            String email = authentication.getName();
            log.debug("Getting current user ID for email: {}", email);

            User user = userRepository.findByEmailId(email)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found for email: {}", email);
                return null;
            }

            log.debug("Found user ID: {} for email: {}", user.getId(), email);
            return user.getId();
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
            return null;
        }
    }
}