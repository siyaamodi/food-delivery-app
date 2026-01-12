package com.example.food_delivery.service;

import com.example.food_delivery.dto.PaymentProcessingResult;
import com.example.food_delivery.dto.PaymentRequest;
import com.example.food_delivery.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class MockPaymentGateway {

    private final Random random = new Random();

    public PaymentProcessingResult processPayment(PaymentRequest paymentRequest, Double amount) {
        log.info("Processing payment for order: {}, amount: {}, method: {}",
                paymentRequest.getOrderId(), amount, paymentRequest.getPaymentMethod());

        // Simulate payment processing delay
        simulateProcessingDelay();

        // Generate random transaction ID
        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Simulate different outcomes (85% success, 10% failure, 5% processing)
        double outcome = random.nextDouble();

        if (outcome < 0.85) {
            // Success case
            log.info("Payment successful for transaction: {}", transactionId);
            return PaymentProcessingResult.builder()
                    .success(true)
                    .status(PaymentStatus.SUCCESS)
                    .transactionId(transactionId)
                    .message("Payment processed successfully")
                    .gatewayResponse("APPROVED")
                    .build();
        } else if (outcome < 0.95) {
            // Failure case
            log.warn("Payment failed for transaction: {}", transactionId);
            return PaymentProcessingResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .transactionId(transactionId)
                    .message("Payment failed: Insufficient funds")
                    .gatewayResponse("DECLINED")
                    .build();
        } else {
            // Processing case (needs retry)
            log.info("Payment processing for transaction: {}", transactionId);
            return PaymentProcessingResult.builder()
                    .success(false)
                    .status(PaymentStatus.PROCESSING)
                    .transactionId(transactionId)
                    .message("Payment is being processed")
                    .gatewayResponse("PENDING")
                    .build();
        }
    }

    public PaymentProcessingResult processRefund(String originalTransactionId, Double amount) {
        log.info("Processing refund for transaction: {}, amount: {}", originalTransactionId, amount);

        simulateProcessingDelay();

        String refundTransactionId = "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 90% success rate for refunds
        if (random.nextDouble() < 0.90) {
            log.info("Refund successful: {}", refundTransactionId);
            return PaymentProcessingResult.builder()
                    .success(true)
                    .status(PaymentStatus.REFUNDED)
                    .transactionId(refundTransactionId)
                    .message("Refund processed successfully")
                    .gatewayResponse("REFUNDED")
                    .build();
        } else {
            log.warn("Refund failed: {}", refundTransactionId);
            return PaymentProcessingResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .transactionId(refundTransactionId)
                    .message("Refund failed: Technical error")
                    .gatewayResponse("REFUND_FAILED")
                    .build();
        }
    }

    private void simulateProcessingDelay() {
        try {
            // Simulate network delay (1-3 seconds)
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment processing interrupted");
        }
    }

    // Validate card details (basic validation)
    public boolean validateCardDetails(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        if (cardNumber == null || cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            return false;
        }
        if (cvv == null || cvv.length() != 3 || !cvv.matches("\\d+")) {
            return false;
        }
        // Basic expiry validation
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            return month >= 1 && month <= 12 && year >= 2024;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate UPI ID
    public boolean validateUpiId(String upiId) {
        return upiId != null && upiId.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");
    }
}
