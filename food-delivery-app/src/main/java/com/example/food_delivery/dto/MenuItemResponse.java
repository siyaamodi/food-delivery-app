package com.example.food_delivery.dto;

import com.example.food_delivery.enums.MenuItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private MenuItemStatus status;
    private String imageUrl;
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
    @Builder.Default
    private List<MenuItemResponse> menuItems=new ArrayList<>();

    @Builder.Default
    private List<String> categories=new ArrayList<>();
}