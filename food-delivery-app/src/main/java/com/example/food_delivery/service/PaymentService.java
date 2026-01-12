package com.example.food_delivery.service;

import com.example.food_delivery.dto.*;
import com.example.food_delivery.entity.Order;
import com.example.food_delivery.entity.Payment;
import com.example.food_delivery.enums.OrderStatus;
import com.example.food_delivery.enums.PaymentMethod;
import com.example.food_delivery.enums.PaymentStatus;
import com.example.food_delivery.repository.OrderRepository;
import com.example.food_delivery.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MockPaymentGateway paymentGateway;
    private final UserService userService;

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order ID: {}", paymentRequest.getOrderId());

        // Get current user
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return buildErrorResponse("User not authenticated");
        }

        // Get order
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElse(null);

        if (order == null) {
            return buildErrorResponse("Order not found");
        }

        // Verify order belongs to current user
        if (!order.getCustomer().getId().equals(currentUserId)) {
            return buildErrorResponse("Access denied: Order does not belong to current user");
        }

        // Check if order is in valid state for payment
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            return buildErrorResponse("Cannot process payment for order in " + order.getStatus() + " status");
        }

        // Check if payment already exists
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            return buildErrorResponse("Payment already exists for this order");
        }

        // Validate payment details based on method
        if (!validatePaymentDetails(paymentRequest)) {
            return buildErrorResponse("Invalid payment details");
        }

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .paymentGateway("MOCK_GATEWAY") // Using mock gateway
                .build();

        // Set payment method specific details
        setPaymentMethodDetails(payment, paymentRequest);

        Payment savedPayment = paymentRepository.save(payment);

        // Process payment through gateway
        PaymentProcessingResult result = paymentGateway.processPayment(paymentRequest, order.getTotalAmount());

        // Update payment with gateway response
        savedPayment.setStatus(result.getStatus());
        savedPayment.setTransactionId(result.getTransactionId());
        savedPayment.setPaymentDescription(result.getMessage());

        if (result.isSuccess()) {
            // Update order status if payment successful
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Payment successful, order confirmed: {}", order.getId());
        }

        Payment updatedPayment = paymentRepository.save(savedPayment);
        log.info("Payment processing completed for order: {}, status: {}",
                order.getId(), updatedPayment.getStatus());

        return buildPaymentResponse(updatedPayment, result.getMessage());
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.debug("Fetching payment for order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        if (payment == null) {
            log.warn("Payment not found for order ID: {}", orderId);
            return buildErrorResponse("Payment not found for order: " + orderId);
        }

        return buildPaymentResponse(payment, "Payment retrieved successfully");
    }

    public PaymentResponse getUserPayments(Long userId) {
        log.info("Fetching payments for user ID: {}", userId);

        List<Payment> payments = paymentRepository.findByOrderCustomerId(userId);

        if (payments.isEmpty()) {
            log.info("No payments found for user ID: {}", userId);
            return buildErrorResponse("No payments found");
        }

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(payment -> buildPaymentResponse(payment, ""))
                .collect(Collectors.toList());

        log.info("Retrieved {} payments for user: {}", payments.size(), userId);
        return buildListSuccessResponse(paymentResponses, "Payments retrieved successfully");
    }

    public PaymentResponse processRefund(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            return buildErrorResponse("Payment not found");
        }

        // Check if refund is possible
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return buildErrorResponse("Cannot refund payment with status: " + payment.getStatus());
        }

        // Process refund through gateway
        PaymentProcessingResult result = paymentGateway.processRefund(
                payment.getTransactionId(), payment.getAmount());

        payment.setStatus(result.getStatus());
        payment.setTransactionId(result.getTransactionId());
        payment.setPaymentDescription("Refund: " + result.getMessage());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update order status if refund successful
        if (result.isSuccess()) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Refund successful, order cancelled: {}", order.getId());
        }

        return buildPaymentResponse(updatedPayment, result.getMessage());
    }

    public PaymentResponse getPaymentStatus(Long paymentId) {
        log.debug("Checking payment status for ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            return buildErrorResponse("Payment not found");
        }

        return buildPaymentResponse(payment, "Payment status retrieved");
    }

    // ========== HELPER METHODS ==========

    private boolean validatePaymentDetails(PaymentRequest paymentRequest) {
        switch (paymentRequest.getPaymentMethod()) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return paymentGateway.validateCardDetails(
                        paymentRequest.getCardNumber(),
                        paymentRequest.getExpiryMonth(),
                        paymentRequest.getExpiryYear(),
                        paymentRequest.getCvv()
                );
            case UPI:
                return paymentGateway.validateUpiId(paymentRequest.getUpiId());
            case NET_BANKING:
            case WALLET:
            case COD:
                return true; // No specific validation needed for these in mock
            default:
                return false;
        }
    }

    private void setPaymentMethodDetails(Payment payment, PaymentRequest paymentRequest) {
        switch (paymentRequest.getPaymentMethod()) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                if (paymentRequest.getCardNumber() != null && paymentRequest.getCardNumber().length() >= 4) {
                    payment.setCardLastFour(paymentRequest.getCardNumber().substring(paymentRequest.getCardNumber().length() - 4));
                    payment.setCardType(paymentRequest.getPaymentMethod() == PaymentMethod.CREDIT_CARD ? "CREDIT" : "DEBIT");
                }
                break;
            case UPI:
                payment.setUpiId(paymentRequest.getUpiId());
                break;
            case COD:
                payment.setPaymentDescription("Cash on Delivery");
                break;
        }
    }

    private PaymentResponse buildPaymentResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentGateway(payment.getPaymentGateway())
                .paymentDescription(payment.getPaymentDescription())
                .cardLastFour(payment.getCardLastFour())
                .cardType(payment.getCardType())
                .upiId(payment.getUpiId())
                .paymentTime(payment.getPaymentTime())
                .createdAt(payment.getCreatedAt())
                .message(message)
                .success(true)
                .build();
    }

    private PaymentResponse buildErrorResponse(String message) {
        return PaymentResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private PaymentResponse buildListSuccessResponse(List<PaymentResponse> payments, String message) {
        return PaymentResponse.builder()
                .success(true)
                .message(message)
                .payments(payments)
                .build();
    }
}