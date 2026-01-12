package com.example.food_delivery.dto;

import com.example.food_delivery.enums.NotificationStatus;
import com.example.food_delivery.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private Long relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String messageText;
    private Boolean success;
}
