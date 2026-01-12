package com.example.food_delivery.dto;

import com.example.food_delivery.enums.PaymentMethod;
import com.example.food_delivery.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String paymentGateway;
    private String paymentDescription;
    private String cardLastFour;
    private String cardType;
    private String upiId;
    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;

    @Builder.Default
    private java.util.List<PaymentResponse> payments = java.util.List.of();
}