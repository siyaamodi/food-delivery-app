package com.example.food_delivery.service;

import com.example.food_delivery.dto.RestaurantRequest;
import com.example.food_delivery.dto.RestaurantResponse;
import com.example.food_delivery.entity.Restaurant;
import com.example.food_delivery.entity.User;
import com.example.food_delivery.enums.CuisineType;
import com.example.food_delivery.enums.RestaurantStatus;
import com.example.food_delivery.enums.Role;
import com.example.food_delivery.repository.RestaurantRepository;
import com.example.food_delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        logger.info("Creating restaurant: {}", request.getName());

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getOwnerId()));

        if (!owner.getRole().equals(Role.RESTAURANT_OWNER)) {
            throw new RuntimeException("User is not a restaurant owner");
        }

        if (restaurantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        if (restaurantRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered: " + request.getPhone());
        }

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .cuisineType(request.getCuisineType())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .openingHours(request.getOpeningHours())
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : 0.0)
                .estimatedDeliveryTime(request.getEstimatedDeliveryTime() != null ? request.getEstimatedDeliveryTime() : 30)
                .status(RestaurantStatus.ACTIVE)
                .owner(owner)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant created successfully with ID: {}", savedRestaurant.getId());

        return toResponse(savedRestaurant, "Restaurant created successfully");
    }

    public RestaurantResponse getRestaurantById(Long id) {
        logger.info("Fetching restaurant by ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        return toResponse(restaurant, "Restaurant retrieved successfully");
    }

    public List<RestaurantResponse> getAllRestaurants() {
        logger.info("Fetching all active restaurants");
        List<Restaurant> restaurants = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE);
        return restaurants.stream()
                .map(restaurant -> toResponse(restaurant, null))
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerId) {
        logger.info("Fetching restaurants for owner ID: {}", ownerId);
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(ownerId);
        return restaurants.stream()
                .map(restaurant -> toResponse(restaurant, null))
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> searchRestaurants(String searchTerm) {
        logger.info("Searching restaurants with term: {}", searchTerm);
        List<Restaurant> restaurants = restaurantRepository.searchByName(searchTerm, RestaurantStatus.ACTIVE);
        return restaurants.stream()
                .map(restaurant -> toResponse(restaurant, null))
                .collect(Collectors.toList());
    }


    public List<RestaurantResponse> searchRestaurantsByNameOrCuisine(String searchTerm) {
        logger.info("Advanced search with term: {}", searchTerm);

        CuisineType cuisineType = null;
        try {
            cuisineType = CuisineType.valueOf(searchTerm.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If not a valid cuisine type, search only by name
            return searchRestaurants(searchTerm);
        }

        List<Restaurant> restaurants = restaurantRepository.searchByNameOrExactCuisine(searchTerm, cuisineType);
        return restaurants.stream()
                .map(restaurant -> toResponse(restaurant, null))
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsByCuisine(String cuisineType) {
        logger.info("Fetching restaurants by cuisine: {}", cuisineType);
        try {
            CuisineType cuisine = CuisineType.valueOf(cuisineType.toUpperCase());
            List<Restaurant> restaurants = restaurantRepository.findByCuisineType(cuisine);
            return restaurants.stream()
                    .map(restaurant -> toResponse(restaurant, null))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid cuisine type: " + cuisineType);
        }
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long ownerId) {
        logger.info("Updating restaurant ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or you don't have permission to update"));

        if (!restaurant.getEmail().equals(request.getEmail()) &&
                restaurantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        if (!restaurant.getPhone().equals(request.getPhone()) &&
                restaurantRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered: " + request.getPhone());
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOpeningHours(request.getOpeningHours());
        restaurant.setDeliveryFee(request.getDeliveryFee());
        restaurant.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return toResponse(updatedRestaurant, "Restaurant updated successfully");
    }

    public RestaurantResponse toggleRestaurantStatus(Long id, Long ownerId) {
        logger.info("Toggling status for restaurant ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or you don't have permission"));

        RestaurantStatus newStatus = (restaurant.getStatus() == RestaurantStatus.ACTIVE)
                ? RestaurantStatus.INACTIVE
                : RestaurantStatus.ACTIVE;

        restaurant.setStatus(newStatus);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        String message = "Restaurant status updated to: " + newStatus;
        return toResponse(updatedRestaurant, message);
    }

    public RestaurantResponse deleteRestaurant(Long id, Long ownerId) {
        logger.info("Deleting restaurant ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found or you don't have permission"));

        restaurantRepository.delete(restaurant);

        return RestaurantResponse.builder()
                .id(id)
                .success(true)
                .message("Restaurant deleted successfully")
                .build();
    }

    private RestaurantResponse toResponse(Restaurant restaurant, String message) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .cuisineType(restaurant.getCuisineType())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .status(restaurant.getStatus())
                .rating(restaurant.getRating() != null ? restaurant.getRating() : 0.0)
                .imageUrl(restaurant.getImageUrl())
                .openingHours(restaurant.getOpeningHours())
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : 0.0)
                .estimatedDeliveryTime(restaurant.getEstimatedDeliveryTime() != null ? restaurant.getEstimatedDeliveryTime() : 30)
                .ownerId(restaurant.getOwner().getId())
                .ownerName(restaurant.getOwner().getName())
                .createdAt(restaurant.getCreatedAt())
                .message(message)
                .success(true)
                .build();
    }
}