package com.example.food_delivery.controller;

import com.example.food_delivery.dto.OrderRequest;
import com.example.food_delivery.dto.OrderResponse;
import com.example.food_delivery.enums.OrderStatus;
import com.example.food_delivery.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable Long customerId) {
        List<OrderResponse> responses = orderService.getCustomerOrders(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId) {
        List<OrderResponse> responses = orderService.getRestaurantOrders(restaurantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/restaurant/{restaurantId}/active")
    public ResponseEntity<List<OrderResponse>> getActiveRestaurantOrders(@PathVariable Long restaurantId) {
        List<OrderResponse> responses = orderService.getActiveRestaurantOrders(restaurantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {
        OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{orderId}/assign-delivery/{deliveryPartnerId}")
    public ResponseEntity<OrderResponse> assignDeliveryPartner(
            @PathVariable Long orderId,
            @PathVariable Long deliveryPartnerId) {
        OrderResponse response = orderService.assignDeliveryPartner(orderId, deliveryPartnerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
