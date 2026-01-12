package com.example.food_delivery.controller;

import com.example.food_delivery.dto.RestaurantRequest;
import com.example.food_delivery.dto.RestaurantResponse;
import com.example.food_delivery.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @PostMapping("/create-restaurant")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        logger.info("Creating restaurant with name: {}", request.getName());
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all-restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        logger.info("Fetching all active restaurants");
        List<RestaurantResponse> responses = restaurantService.getAllRestaurants();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get-restaurant-by-id/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        logger.info("Fetching restaurant by ID: {}", id);
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-restaurants-by-owner/{ownerId}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByOwner(@PathVariable Long ownerId) {
        logger.info("Fetching restaurants for owner ID: {}", ownerId);
        List<RestaurantResponse> responses = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/search-restaurants")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(@RequestParam String searchTerm) {
        logger.info("Searching restaurants with term: {}", searchTerm);
        List<RestaurantResponse> responses = restaurantService.searchRestaurants(searchTerm);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurantsByNameOrCuisine(@RequestParam String searchTerm) {
        logger.info("Advanced search with term: {}", searchTerm);
        List<RestaurantResponse> responses = restaurantService.searchRestaurantsByNameOrCuisine(searchTerm);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get-restaurants-by-cuisine/{cuisineType}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByCuisine(@PathVariable String cuisineType) {
        logger.info("Fetching restaurants by cuisine type: {}", cuisineType);
        List<RestaurantResponse> responses = restaurantService.getRestaurantsByCuisine(cuisineType);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/update-restaurant/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            @RequestParam Long ownerId) {
        logger.info("Updating restaurant with ID: {}", id);
        RestaurantResponse response = restaurantService.updateRestaurant(id, request, ownerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/toggle-restaurant-status/{id}")
    public ResponseEntity<RestaurantResponse> toggleRestaurantStatus(
            @PathVariable Long id,
            @RequestParam Long ownerId) {
        logger.info("Toggling status for restaurant ID: {}", id);
        RestaurantResponse response = restaurantService.toggleRestaurantStatus(id, ownerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete-restaurant/{id}")
    public ResponseEntity<RestaurantResponse> deleteRestaurant(
            @PathVariable Long id,
            @RequestParam Long ownerId) {
        logger.info("Deleting restaurant with ID: {}", id);
        RestaurantResponse response = restaurantService.deleteRestaurant(id, ownerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}