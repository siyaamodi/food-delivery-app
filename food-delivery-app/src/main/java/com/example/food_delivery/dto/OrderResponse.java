package com.example.food_delivery.dto;

import com.example.food_delivery.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private OrderStatus status;
    private String deliveryAddress;
    private String customerNotes;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveredTime;
    private String message;
    private Boolean success;

    @Builder.Default
    private List<OrderResponse> orders = List.of();
}