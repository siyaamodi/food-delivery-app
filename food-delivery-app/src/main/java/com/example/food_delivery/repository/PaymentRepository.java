package com.example.food_delivery.repository;

import com.example.food_delivery.entity.Payment;
import com.example.food_delivery.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderCustomerId(Long customerId);
    List<Payment> findByStatus(PaymentStatus status);
}