package com.example.food_delivery.repository;

import com.example.food_delivery.entity.Restaurant;
import com.example.food_delivery.enums.CuisineType;
import com.example.food_delivery.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByCuisineTypeAndStatus(CuisineType cuisineType, RestaurantStatus status);
    List<Restaurant> findByOwnerId(Long ownerId);
    Optional<Restaurant> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND r.status = :status")
    List<Restaurant> searchByName(@Param("searchTerm") String searchTerm, @Param("status") RestaurantStatus status);

    @Query("SELECT r FROM Restaurant r WHERE r.cuisineType = :cuisineType AND r.status = 'ACTIVE'")
    List<Restaurant> findByCuisineType(@Param("cuisineType") CuisineType cuisineType);

    @Query("SELECT r FROM Restaurant r WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR r.cuisineType = :cuisineType) AND r.status = 'ACTIVE'")
    List<Restaurant> searchByNameOrExactCuisine(@Param("searchTerm") String searchTerm, @Param("cuisineType") CuisineType cuisineType);
}