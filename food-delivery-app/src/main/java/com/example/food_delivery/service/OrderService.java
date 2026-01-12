package com.example.food_delivery.service;

import com.example.food_delivery.dto.*;
import com.example.food_delivery.entity.*;
import com.example.food_delivery.enums.MenuItemStatus;
import com.example.food_delivery.enums.OrderStatus;
import com.example.food_delivery.enums.Role;
import com.example.food_delivery.repository.MenuItemRepository;
import com.example.food_delivery.repository.OrderRepository;
import com.example.food_delivery.repository.RestaurantRepository;
import com.example.food_delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService; // must provide current user id

    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating new order for restaurant ID: {}", orderRequest.getRestaurantId());

        // Get current user from SecurityContext (user is authenticated via JWT)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            log.error("User not authenticated for order creation");
            throw new RuntimeException("User not authenticated. Please login first.");
        }

        String email = authentication.getName();
        log.info("Creating order for authenticated user: {}", email);

        User customer = userRepository.findByEmailId(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Restaurant restaurant = restaurantRepository.findById(orderRequest.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Build order
        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .customerNotes(orderRequest.getCustomerNotes())
                .status(OrderStatus.PENDING)
                .build();

        // Add order items
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));

            if (menuItem.getStatus() != MenuItemStatus.AVAILABLE) {
                throw new RuntimeException("Menu item not available: " + menuItem.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemRequest.getQuantity())
                    .price(menuItem.getPrice())
                    .order(order)
                    .build();

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // FIXED: Use mapToOrderResponse instead of buildOrderResponse
        return mapToOrderResponse(savedOrder, "Order created successfully");
    }
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        return mapToOrderResponse(order, "Order retrieved successfully");
    }

    public List<OrderResponse> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        if (orders.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found for customer");
        return orders.stream().map(o -> mapToOrderResponse(o, "")).collect(Collectors.toList());
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        if (orders.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found for restaurant");
        return orders.stream().map(o -> mapToOrderResponse(o, "")).collect(Collectors.toList());
    }

    public List<OrderResponse> getActiveRestaurantOrders(Long restaurantId) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY
        );
        List<Order> orders = orderRepository.findByRestaurantIdAndStatusIn(restaurantId, activeStatuses);
        if (orders.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active orders found");
        return orders.stream().map(o -> mapToOrderResponse(o, "")).collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated, "Order status updated to: " + newStatus);
    }

    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel order in " + order.getStatus() + " status");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated, "Order cancelled successfully");
    }

    public OrderResponse assignDeliveryPartner(Long orderId, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        User deliveryPartner = userRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery partner not found"));

        if (deliveryPartner.getRole() != Role.DELIVERY_PARTNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a delivery partner");
        }

        order.setDeliveryPartner(deliveryPartner);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated, "Delivery partner assigned successfully");
    }

    // -------- mapping helpers --------
    private OrderResponse mapToOrderResponse(Order order, String message) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerPhone(order.getCustomer().getMobileNo())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .customerNotes(order.getCustomerNotes())
                .deliveryPartnerId(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getId() : null)
                .deliveryPartnerName(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getName() : null)
                .orderTime(order.getOrderTime())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .deliveredTime(order.getDeliveredTime())
                .message(message)
                .success(true)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .menuItemId(orderItem.getMenuItem().getId())
                .menuItemName(orderItem.getMenuItem().getName())
                .menuItemImage(orderItem.getMenuItem().getImageUrl())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}
