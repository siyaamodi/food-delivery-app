package com.example.food_delivery.dto;

import com.example.food_delivery.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessingResult {
    private boolean success;
    private PaymentStatus status;
    private String transactionId;
    private String message;
    private String gatewayResponse;
}
