package com.example.food_delivery.service;

import com.example.food_delivery.dto.DeliveryPartnerRequest;
import com.example.food_delivery.dto.DeliveryPartnerResponse;
import com.example.food_delivery.dto.DeliveryTrackingResponse;
import com.example.food_delivery.entity.*;
import com.example.food_delivery.enums.DeliveryPartnerStatus;
import com.example.food_delivery.enums.DeliveryStatus;
import com.example.food_delivery.enums.OrderStatus;
import com.example.food_delivery.enums.Role;
import com.example.food_delivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DeliveryPartnerResponse registerDeliveryPartner(DeliveryPartnerRequest request) {
        log.info("Registering delivery partner for user ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.DELIVERY_PARTNER) {
            throw new RuntimeException("User is not a delivery partner");
        }

        if (deliveryPartnerRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Delivery partner already registered for this user");
        }

        if (deliveryPartnerRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new RuntimeException("Vehicle number already registered");
        }

        if (deliveryPartnerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already registered");
        }

        DeliveryPartner deliveryPartner = DeliveryPartner.builder()
                .user(user)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .licenseNumber(request.getLicenseNumber())
                .status(DeliveryPartnerStatus.AVAILABLE)
                .rating(0.0)
                .totalDeliveries(0)
                .build();

        DeliveryPartner savedPartner = deliveryPartnerRepository.save(deliveryPartner);
        log.info("Delivery partner registered successfully: {}", savedPartner.getId());

        return buildDeliveryPartnerResponse(savedPartner, "Delivery partner registered successfully");
    }

    public DeliveryTrackingResponse assignDeliveryPartner(Long orderId) {
        log.info("Assigning delivery partner for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.READY) {
            throw new RuntimeException("Order is not ready for delivery");
        }

        if (deliveryTrackingRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Delivery partner already assigned to this order");
        }

        List<DeliveryPartner> availablePartners = deliveryPartnerRepository.findAvailableDeliveryPartners();

        if (availablePartners.isEmpty()) {
            throw new RuntimeException("No available delivery partners at the moment");
        }

        // Assign to the highest rated available partner
        DeliveryPartner assignedPartner = availablePartners.get(0);
        assignedPartner.setStatus(DeliveryPartnerStatus.BUSY);

        DeliveryTracking deliveryTracking = DeliveryTracking.builder()
                .order(order)
                .deliveryPartner(assignedPartner)
                .status(DeliveryStatus.PICKING_UP)
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(30))
                .build();

        DeliveryTracking savedTracking = deliveryTrackingRepository.save(deliveryTracking);

        // Update order status
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);

        // Send notification
        notificationService.sendOrderPickedUpNotification(order, assignedPartner);

        log.info("Delivery partner assigned: {} to order: {}", assignedPartner.getId(), orderId);
        return buildDeliveryTrackingResponse(savedTracking, "Delivery partner assigned successfully");
    }

    public DeliveryTrackingResponse updateDeliveryStatus(Long orderId, DeliveryStatus newStatus, Double latitude, Double longitude) {
        log.info("Updating delivery status for order ID: {} to {}", orderId, newStatus);

        DeliveryTracking deliveryTracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery tracking not found for order"));

        deliveryTracking.setStatus(newStatus);
        deliveryTracking.setCurrentLatitude(latitude);
        deliveryTracking.setCurrentLongitude(longitude);

        if (newStatus == DeliveryStatus.PICKING_UP) {
            deliveryTracking.setPickupTime(LocalDateTime.now());
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            deliveryTracking.setDeliveryTime(LocalDateTime.now());

            // Update delivery partner stats
            DeliveryPartner partner = deliveryTracking.getDeliveryPartner();
            partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
            partner.setStatus(DeliveryPartnerStatus.AVAILABLE);
            deliveryPartnerRepository.save(partner);

            // Update order status
            Order order = deliveryTracking.getOrder();
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            notificationService.sendOrderDeliveredNotification(order);
        }

        DeliveryTracking updatedTracking = deliveryTrackingRepository.save(deliveryTracking);
        return buildDeliveryTrackingResponse(updatedTracking, "Delivery status updated successfully");
    }

    public DeliveryTrackingResponse getDeliveryTracking(Long orderId) {
        log.debug("Fetching delivery tracking for order ID: {}", orderId);

        DeliveryTracking deliveryTracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery tracking not found"));

        return buildDeliveryTrackingResponse(deliveryTracking, "Delivery tracking retrieved successfully");
    }

    public List<DeliveryPartnerResponse> getAvailableDeliveryPartners() {
        log.info("Fetching available delivery partners");

        List<DeliveryPartner> partners = deliveryPartnerRepository.findAvailableDeliveryPartners();
        return partners.stream()
                .map(partner -> buildDeliveryPartnerResponse(partner, ""))
                .collect(Collectors.toList());
    }

    public DeliveryPartnerResponse updateDeliveryPartnerStatus(Long partnerId, DeliveryPartnerStatus newStatus) {
        log.info("Updating delivery partner {} status to {}", partnerId, newStatus);

        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        partner.setStatus(newStatus);
        DeliveryPartner updatedPartner = deliveryPartnerRepository.save(partner);

        return buildDeliveryPartnerResponse(updatedPartner, "Status updated successfully");
    }

    // Helper methods
    private DeliveryPartnerResponse buildDeliveryPartnerResponse(DeliveryPartner partner, String message) {
        return DeliveryPartnerResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .userName(partner.getUser().getName())
                .userEmail(partner.getUser().getEmailId())
                .userPhone(partner.getUser().getMobileNo())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .licenseNumber(partner.getLicenseNumber())
                .status(partner.getStatus())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .createdAt(partner.getCreatedAt())
                .message(message)
                .success(true)
                .build();
    }

    private DeliveryTrackingResponse buildDeliveryTrackingResponse(DeliveryTracking tracking, String message) {
        return DeliveryTrackingResponse.builder()
                .id(tracking.getId())
                .orderId(tracking.getOrder().getId())
                .deliveryPartnerId(tracking.getDeliveryPartner().getId())
                .deliveryPartnerName(tracking.getDeliveryPartner().getUser().getName())
                .deliveryPartnerPhone(tracking.getDeliveryPartner().getUser().getMobileNo())
                .vehicleType(tracking.getDeliveryPartner().getVehicleType())
                .vehicleNumber(tracking.getDeliveryPartner().getVehicleNumber())
                .status(tracking.getStatus())
                .currentLatitude(tracking.getCurrentLatitude())
                .currentLongitude(tracking.getCurrentLongitude())
                .pickupTime(tracking.getPickupTime())
                .deliveryTime(tracking.getDeliveryTime())
                .estimatedDeliveryTime(tracking.getEstimatedDeliveryTime())
                .deliveryNotes(tracking.getDeliveryNotes())
                .createdAt(tracking.getCreatedAt())
                .message(message)
                .success(true)
                .build();
    }
}