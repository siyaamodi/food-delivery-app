package com.example.food_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private Integer restaurantRating;
    private Integer deliveryRating;
    private String restaurantComment;
    private String deliveryComment;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
}
