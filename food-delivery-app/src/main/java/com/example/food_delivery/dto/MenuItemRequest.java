package com.example.food_delivery.dto;

import com.example.food_delivery.enums.MenuItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required") // Changed to NotBlank
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private MenuItemStatus status;
    private String imageUrl;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}