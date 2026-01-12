package com.example.food_delivery.repository;

import com.example.food_delivery.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderId(Long orderId);
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByDeliveryPartnerId(Long deliveryPartnerId);
    List<Review> findByCustomerId(Long customerId);

    @Query("SELECT AVG(r.restaurantRating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Double findAverageRestaurantRating(@Param("restaurantId") Long restaurantId);

    @Query("SELECT AVG(r.deliveryRating) FROM Review r WHERE r.deliveryPartner.id = :deliveryPartnerId")
    Double findAverageDeliveryRating(@Param("deliveryPartnerId") Long deliveryPartnerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Long countByRestaurantId(@Param("restaurantId") Long restaurantId);
}
