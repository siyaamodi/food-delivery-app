package com.example.food_delivery.dto;

import com.example.food_delivery.enums.DeliveryPartnerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private DeliveryPartnerStatus status;
    private Double rating;
    private Integer totalDeliveries;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
}