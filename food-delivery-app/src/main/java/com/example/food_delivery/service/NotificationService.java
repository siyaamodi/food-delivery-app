package com.example.food_delivery.service;
import com.example.food_delivery.dto.NotificationResponse;
import com.example.food_delivery.entity.*;
import com.example.food_delivery.enums.NotificationStatus;
import com.example.food_delivery.enums.NotificationType;
import com.example.food_delivery.repository.NotificationRepository;
import com.example.food_delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    public void sendOrderConfirmedNotification(Order order) {

        String title = "Order Confirmed!";
        String message = String.format("Your order #%d has been confirmed and is being prepared.", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.ORDER_CONFIRMED, title, message, order.getId());

        log.info("Order confirmed notification sent for order: {}", order.getId());
    }
    public void sendOrderPreparingNotification(Order order)
    {
        String title = "Order Being Prepared";
        String message = String.format("Restaurant has started preparing your order #%d.", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.ORDER_PREPARING, title, message, order.getId());
    }
    public void sendOrderReadyNotification(Order order) { String title = "Order Ready for Pickup";
        String message = String.format("Your order #%d is ready for pickup by delivery partner.", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.ORDER_READY, title, message, order.getId());
    }
    public void sendOrderPickedUpNotification(Order order, DeliveryPartner deliveryPartner)
    {
        String title = "Order Picked Up";
        String message = String.format("Your order #%d has been picked up by %s and is on the way.", order.getId(), deliveryPartner.getUser().getName());
        createNotification(order.getCustomer().getId(), NotificationType.ORDER_PICKED_UP, title, message, order.getId());
    }
    public void sendOrderDeliveredNotification(Order order) { String title = "Order Delivered!";
        String message = String.format("Your order #%d has been successfully delivered. Enjoy your meal!", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.ORDER_DELIVERED, title, message, order.getId());
    }
    public void sendPaymentSuccessNotification(Order order) { String title = "Payment Successful";
        String message = String.format("Payment for order #%d has been processed successfully.", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.PAYMENT_SUCCESS, title, message, order.getId());
    }
    public void sendPaymentFailedNotification(Order order) { String title = "Payment Failed";
        String message = String.format("Payment for order #%d failed. Please try again.", order.getId());
        createNotification(order.getCustomer().getId(), NotificationType.PAYMENT_FAILED, title, message, order.getId());
    }
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("Fetching notifications for user ID: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream() .map(this::buildNotificationResponse) .collect(Collectors.toList());
    }
    public NotificationResponse markAsRead(Long notificationId) {
        log.debug("Marking notification as read: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId) .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(java.time.LocalDateTime.now());
        Notification updatedNotification = notificationRepository.save(notification);
        return buildNotificationResponse(updatedNotification);
    }
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }
    public void deleteNotification(Long notificationId) {
        log.debug("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }
    private void createNotification(Long userId, NotificationType type, String title, String message, Long relatedEntityId)
    {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .relatedEntityId(relatedEntityId)
                .build();
        notificationRepository.save(notification); log.debug("Notification created for user: {}, type: {}", userId, type);
    }
    private NotificationResponse buildNotificationResponse(Notification notification)
    {
        return NotificationResponse
                .builder()
                .id(notification.getId())
                .userId(notification.getUser()
                        .getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
        .relatedEntityId(notification.getRelatedEntityId())
        .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
        .messageText("Notification retrieved successfully")
        .success(true)
        .build();
}
}