package com.example.food_delivery.controller;

import com.example.food_delivery.dto.DeliveryPartnerRequest;
import com.example.food_delivery.dto.DeliveryPartnerResponse;
import com.example.food_delivery.dto.DeliveryTrackingResponse;
import com.example.food_delivery.enums.DeliveryPartnerStatus;
import com.example.food_delivery.enums.DeliveryStatus;
import com.example.food_delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/partners/register")
    public ResponseEntity<DeliveryPartnerResponse> registerDeliveryPartner(@Valid @RequestBody DeliveryPartnerRequest request) {
        DeliveryPartnerResponse response = deliveryService.registerDeliveryPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<DeliveryTrackingResponse> assignDeliveryPartner(@PathVariable Long orderId) {
        DeliveryTrackingResponse response = deliveryService.assignDeliveryPartner(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<DeliveryTrackingResponse> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam DeliveryStatus status,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        DeliveryTrackingResponse response = deliveryService.updateDeliveryStatus(orderId, status, latitude, longitude);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<DeliveryTrackingResponse> getDeliveryTracking(@PathVariable Long orderId) {
        DeliveryTrackingResponse response = deliveryService.getDeliveryTracking(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/partners/available")
    public ResponseEntity<List<DeliveryPartnerResponse>> getAvailableDeliveryPartners() {
        List<DeliveryPartnerResponse> responses = deliveryService.getAvailableDeliveryPartners();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/partners/{partnerId}/status")
    public ResponseEntity<DeliveryPartnerResponse> updateDeliveryPartnerStatus(
            @PathVariable Long partnerId,
            @RequestParam DeliveryPartnerStatus status) {
        DeliveryPartnerResponse response = deliveryService.updateDeliveryPartnerStatus(partnerId, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Delivery API is working!");
    }
}