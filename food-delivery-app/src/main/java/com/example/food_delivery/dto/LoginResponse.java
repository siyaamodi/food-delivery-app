package com.example.food_delivery.dto;

import com.example.food_delivery.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String emailId;
    private String name;
    private Role role;
    private Long userId;
    private String message;
    private Boolean success;
}