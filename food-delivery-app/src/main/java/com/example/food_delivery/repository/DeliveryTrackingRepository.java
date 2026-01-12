package com.example.food_delivery.repository;

import com.example.food_delivery.entity.DeliveryTracking;
import com.example.food_delivery.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {
    Optional<DeliveryTracking> findByOrderId(Long orderId);
    List<DeliveryTracking> findByDeliveryPartnerId(Long deliveryPartnerId);
    List<DeliveryTracking> findByStatus(DeliveryStatus status);
    List<DeliveryTracking> findByDeliveryPartnerIdAndStatus(Long deliveryPartnerId, DeliveryStatus status);
}
