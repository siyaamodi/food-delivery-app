package com.example.food_delivery.service;

import com.example.food_delivery.dto.ReviewRequest;
import com.example.food_delivery.dto.ReviewResponse;
import com.example.food_delivery.entity.*;
import com.example.food_delivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserService userService;

    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Creating review for order ID: {}", request.getOrderId());

        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only review your own orders");
        }

        if (order.getStatus() != com.example.food_delivery.enums.OrderStatus.DELIVERED) {
            throw new RuntimeException("You can only review delivered orders");
        }

        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("Review already exists for this order");
        }

        // FIX: Get DeliveryPartner entity from the User deliveryPartner in Order
        DeliveryPartner deliveryPartner = getDeliveryPartnerFromOrder(order);

        Review review = Review.builder()
                .order(order)
                .customer(order.getCustomer())
                .restaurant(order.getRestaurant())
                .deliveryPartner(deliveryPartner) // Use the converted DeliveryPartner
                .restaurantRating(request.getRestaurantRating())
                .deliveryRating(request.getDeliveryRating())
                .restaurantComment(request.getRestaurantComment())
                .deliveryComment(request.getDeliveryComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update restaurant rating
        updateRestaurantRating(order.getRestaurant().getId());

        // Update delivery partner rating if delivery was rated
        if (request.getDeliveryRating() != null && deliveryPartner != null) {
            updateDeliveryPartnerRating(deliveryPartner.getId());
        }

        log.info("Review created successfully for order: {}", request.getOrderId());
        return buildReviewResponse(savedReview, "Review submitted successfully");
    }

    // HELPER METHOD to convert User deliveryPartner to DeliveryPartner entity
    private DeliveryPartner getDeliveryPartnerFromOrder(Order order) {
        if (order.getDeliveryPartner() == null) {
            return null;
        }

        // Find the DeliveryPartner entity by user ID
        return deliveryPartnerRepository.findByUserId(order.getDeliveryPartner().getId())
                .orElse(null);
    }

    public List<ReviewResponse> getRestaurantReviews(Long restaurantId) {
        log.info("Fetching reviews for restaurant ID: {}", restaurantId);

        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
        return reviews.stream()
                .map(review -> buildReviewResponse(review, ""))
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getDeliveryPartnerReviews(Long deliveryPartnerId) {
        log.info("Fetching reviews for delivery partner ID: {}", deliveryPartnerId);

        List<Review> reviews = reviewRepository.findByDeliveryPartnerId(deliveryPartnerId);
        return reviews.stream()
                .map(review -> buildReviewResponse(review, ""))
                .collect(Collectors.toList());
    }

    public ReviewResponse getOrderReview(Long orderId) {
        log.debug("Fetching review for order ID: {}", orderId);

        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Review not found for this order"));

        return buildReviewResponse(review, "Review retrieved successfully");
    }

    private void updateRestaurantRating(Long restaurantId) {
        Double averageRating = reviewRepository.findAverageRestaurantRating(restaurantId);
        Long reviewCount = reviewRepository.countByRestaurantId(restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        restaurantRepository.save(restaurant);

        log.debug("Updated restaurant {} rating to: {}", restaurantId, restaurant.getRating());
    }

    private void updateDeliveryPartnerRating(Long deliveryPartnerId) {
        Double averageRating = reviewRepository.findAverageDeliveryRating(deliveryPartnerId);

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        deliveryPartner.setRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        deliveryPartnerRepository.save(deliveryPartner);

        log.debug("Updated delivery partner {} rating to: {}", deliveryPartnerId, deliveryPartner.getRating());
    }

    private ReviewResponse buildReviewResponse(Review review, String message) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .deliveryPartnerId(review.getDeliveryPartner() != null ? review.getDeliveryPartner().getId() : null)
                .deliveryPartnerName(review.getDeliveryPartner() != null ? review.getDeliveryPartner().getUser().getName() : null)
                .restaurantRating(review.getRestaurantRating())
                .deliveryRating(review.getDeliveryRating())
                .restaurantComment(review.getRestaurantComment())
                .deliveryComment(review.getDeliveryComment())
                .createdAt(review.getCreatedAt())
                .message(message)
                .success(true)
                .build();
    }
}