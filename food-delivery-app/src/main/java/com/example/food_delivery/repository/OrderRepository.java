package com.example.food_delivery.repository;

import com.example.food_delivery.entity.Order;
import com.example.food_delivery.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
    List<Order> findByDeliveryPartnerId(Long deliveryPartnerId);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    Optional<Order> findByIdAndRestaurantId(Long id, Long restaurantId);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status IN :statuses")
    List<Order> findByRestaurantIdAndStatusIn(@Param("restaurantId") Long restaurantId,
                                              @Param("statuses") List<OrderStatus> statuses);
}