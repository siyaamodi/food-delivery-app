package com.example.food_delivery.dto;

import com.example.food_delivery.enums.Gender;
import com.example.food_delivery.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private Integer age;
    private String address;
    private Gender gender;
    private String mobileNo;
    private String emailId;
    private Role role;

    private Boolean success;
    private String message;
}
