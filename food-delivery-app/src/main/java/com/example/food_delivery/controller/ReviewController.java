package com.example.food_delivery.controller;

import com.example.food_delivery.dto.ReviewRequest;
import com.example.food_delivery.dto.ReviewResponse;
import com.example.food_delivery.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> getOrderReview(@PathVariable Long orderId) {
        ReviewResponse response = reviewService.getOrderReview(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getRestaurantReviews(@PathVariable Long restaurantId) {
        List<ReviewResponse> responses = reviewService.getRestaurantReviews(restaurantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/delivery-partner/{deliveryPartnerId}")
    public ResponseEntity<List<ReviewResponse>> getDeliveryPartnerReviews(@PathVariable Long deliveryPartnerId) {
        List<ReviewResponse> responses = reviewService.getDeliveryPartnerReviews(deliveryPartnerId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.status(HttpStatus.OK).body("Reviews API is working!");
    }

}
