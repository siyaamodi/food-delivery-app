package com.example.food_delivery.dto;

import com.example.food_delivery.enums.CuisineType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RestaurantRequest {
    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Cuisine type is required")
    private CuisineType cuisineType;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String imageUrl;
    private String openingHours;

    @PositiveOrZero(message = "Delivery fee must be positive or zero")
    private Double deliveryFee;

    @Positive(message = "Estimated delivery time must be positive")
    private Integer estimatedDeliveryTime;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;
}