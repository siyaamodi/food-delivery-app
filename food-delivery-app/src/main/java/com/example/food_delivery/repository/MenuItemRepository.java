package com.example.food_delivery.repository;

import com.example.food_delivery.entity.MenuItem;
import com.example.food_delivery.enums.MenuItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantIdAndStatus(Long restaurantId, MenuItemStatus status);
    List<MenuItem> findByRestaurantId(Long restaurantId);
    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    // FIXED: category is String, not MenuCategory
    List<MenuItem> findByRestaurantIdAndCategoryAndStatus(Long restaurantId, String category, MenuItemStatus status);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND m.status = :status")
    List<MenuItem> searchByRestaurantAndName(@Param("restaurantId") Long restaurantId,
                                             @Param("searchTerm") String searchTerm,
                                             @Param("status") MenuItemStatus status);

    // FIXED: Return List<String> instead of List<MenuCategory>
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.restaurant.id = :restaurantId")
    List<String> findDistinctCategoriesByRestaurantId(@Param("restaurantId") Long restaurantId);
}