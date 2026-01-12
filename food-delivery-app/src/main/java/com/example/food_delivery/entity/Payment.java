package com.example.food_delivery.entity;

import com.example.food_delivery.enums.PaymentMethod;
import com.example.food_delivery.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId; // From payment gateway
    private String paymentGateway; // Razorpay, Stripe, etc.

    @Column(length = 500)
    private String paymentDescription;

    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Card details (for demo - in production use tokenization)
    private String cardLastFour;
    private String cardType;

    // UPI details
    private String upiId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentTime == null && status == PaymentStatus.SUCCESS) {
            paymentTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (paymentTime == null && status == PaymentStatus.SUCCESS) {
            paymentTime = LocalDateTime.now();
        }
    }
}