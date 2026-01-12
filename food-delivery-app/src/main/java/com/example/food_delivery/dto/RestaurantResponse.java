package com.example.food_delivery.dto;

import com.example.food_delivery.enums.CuisineType;
import com.example.food_delivery.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private CuisineType cuisineType;
    private String phone;
    private String email;
    private RestaurantStatus status;
    private Double rating;
    private String imageUrl;
    private String openingHours;
    private Double deliveryFee;
    private Integer estimatedDeliveryTime;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
}