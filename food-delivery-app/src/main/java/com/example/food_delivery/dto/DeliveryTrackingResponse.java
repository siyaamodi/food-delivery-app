package com.example.food_delivery.dto;

import com.example.food_delivery.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {
    private Long id;
    private Long orderId;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String vehicleType;
    private String vehicleNumber;
    private DeliveryStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryNotes;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
}