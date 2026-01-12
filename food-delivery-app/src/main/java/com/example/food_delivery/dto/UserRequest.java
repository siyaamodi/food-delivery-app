package com.example.food_delivery.dto;

import com.example.food_delivery.enums.Gender;
import com.example.food_delivery.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Name is required")
    @Size(min=2 , max=50 , message = "Name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value= 18, message="Age must be at least 18")
    @Max(value= 100, message="Age must be less than 100")
    private Integer age;

    @NotBlank(message="Address is required")
    @Size(min=5 , max=50 , message="Address must be between 5 and 50")
    private String address;

    @NotNull(message="Gender is required")
    private Gender gender;

    @NotBlank(message="Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message="Mobile number must be 10 digits")
    private String mobileNo;

    @NotBlank(message="Email is required")
    @Email(message = "Email should be valid")
    private String emailId;

    @NotBlank(message = "Password is required")
    @Size(min=6 ,message="Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$",message="Password must contain at least one digit, one lowercase, one uppercase, one special character and no spaces")
    private String password;

    @NotNull(message="Role is required")
    private Role role;
}
