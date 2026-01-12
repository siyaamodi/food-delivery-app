package com.example.food_delivery.repository;

import com.example.food_delivery.entity.DeliveryPartner;
import com.example.food_delivery.enums.DeliveryPartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByUserId(Long userId);

    List<DeliveryPartner> findByStatus(DeliveryPartnerStatus status);

    List<DeliveryPartner> findByStatusOrderByRatingDesc(DeliveryPartnerStatus status);

    boolean existsByVehicleNumber(String vehicleNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    // 🔥 REQUIRED FIX — your service depends on this
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.status = 'AVAILABLE' ORDER BY dp.rating DESC")
    List<DeliveryPartner> findAvailableDeliveryPartners();
}

