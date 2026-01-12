package com.example.food_delivery.dto;

import com.example.food_delivery.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Card details (optional for card payments)
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;

    // UPI details (optional for UPI payments)
    private String upiId;

    // Wallet details (optional for wallet payments)
    private String walletType;
}