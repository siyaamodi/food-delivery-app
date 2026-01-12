package com.example.food_delivery.repository;

import com.example.food_delivery.entity.Notification;
import com.example.food_delivery.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);
    Long countByUserIdAndStatus(Long userId, NotificationStatus status);
}
