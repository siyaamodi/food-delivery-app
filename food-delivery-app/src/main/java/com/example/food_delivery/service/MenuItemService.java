package com.example.food_delivery.service;

import com.example.food_delivery.dto.MenuItemRequest;
import com.example.food_delivery.dto.MenuItemResponse;
import com.example.food_delivery.entity.MenuItem;
import com.example.food_delivery.entity.Restaurant;
import com.example.food_delivery.enums.MenuItemStatus;
import com.example.food_delivery.repository.MenuItemRepository;
import com.example.food_delivery.repository.RestaurantRepository;
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
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info("Creating menu item for restaurant ID: {}", request.getRestaurantId());
        log.debug("Menu item request: name={}, category={}, price={}",
                request.getName(), request.getCategory(), request.getPrice());

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
        if (restaurant == null) {
            log.warn("Restaurant not found with ID: {}", request.getRestaurantId());
            return buildErrorResponse("Restaurant not found with id: " + request.getRestaurantId());
        }

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : MenuItemStatus.AVAILABLE)
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        log.info("Menu item created successfully with ID: {}", savedMenuItem.getId());

        return buildSuccessResponse(savedMenuItem, "Menu item created successfully");
    }

    public MenuItemResponse getMenuItemById(Long id) {
        log.debug("Fetching menu item by ID: {}", id);

        MenuItem menuItem = menuItemRepository.findById(id).orElse(null);

        if (menuItem == null) {
            log.warn("Menu item not found with ID: {}", id);
            return buildErrorResponse("Menu item not found with id: " + id);
        }

        log.debug("Menu item found: ID={}, name={}", menuItem.getId(), menuItem.getName());
        return buildSuccessResponse(menuItem, "Menu item retrieved successfully");
    }

    public MenuItemResponse getMenuItemByIdAndRestaurant(Long id, Long restaurantId) {
        log.debug("Fetching menu item ID: {} for restaurant ID: {}", id, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(id, restaurantId).orElse(null);
        if (menuItem == null) {
            log.warn("Menu item not found with ID: {} in restaurant ID: {}", id, restaurantId);
            return buildErrorResponse("Menu item not found with id: " + id + " in restaurant: " + restaurantId);
        }

        log.debug("Menu item found in restaurant: ID={}, name={}", menuItem.getId(), menuItem.getName());
        return buildSuccessResponse(menuItem, "Menu item retrieved successfully");
    }

    public MenuItemResponse getMenuByRestaurant(Long restaurantId) {
        log.info("Fetching menu for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            log.warn("Restaurant not found with ID: {}", restaurantId);
            return buildErrorResponse("Restaurant not found with id: " + restaurantId);
        }

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndStatus(restaurantId, MenuItemStatus.AVAILABLE);

        if (menuItems.isEmpty()) {
            log.info("No menu items found for restaurant: {} (ID: {})", restaurant.getName(), restaurantId);
            return buildErrorResponse("No menu items found for restaurant: " + restaurant.getName());
        }

        List<MenuItemResponse> itemResponses = menuItems.stream()
                .map(this::buildMenuItemData)
                .collect(Collectors.toList());

        log.info("Retrieved {} menu items for restaurant: {}", menuItems.size(), restaurant.getName());
        return buildListSuccessResponse(itemResponses, "Menu items retrieved successfully");
    }

    public MenuItemResponse getMenuByCategory(Long restaurantId, String category) {
        log.info("Fetching menu items for restaurant ID: {}, category: {}", restaurantId, category);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            log.warn("Restaurant not found with ID: {}", restaurantId);
            return buildErrorResponse("Restaurant not found with id: " + restaurantId);
        }

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndCategoryAndStatus(
                restaurantId, category, MenuItemStatus.AVAILABLE);

        if (menuItems.isEmpty()) {
            log.info("No menu items found in category '{}' for restaurant: {}", category, restaurant.getName());
            return buildErrorResponse("No menu items found in category '" + category + "' for restaurant: " + restaurant.getName());
        }

        List<MenuItemResponse> itemResponses = menuItems.stream()
                .map(this::buildMenuItemData)
                .collect(Collectors.toList());

        log.info("Retrieved {} menu items in category '{}' for restaurant: {}",
                menuItems.size(), category, restaurant.getName());
        return buildListSuccessResponse(itemResponses, "Menu items retrieved successfully");
    }

    public MenuItemResponse searchMenuItems(Long restaurantId, String searchTerm) {
        log.info("Searching menu items in restaurant ID: {} for term: '{}'", restaurantId, searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("Empty search term provided for restaurant ID: {}", restaurantId);
            return buildErrorResponse("Search term cannot be empty");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            log.warn("Restaurant not found with ID: {}", restaurantId);
            return buildErrorResponse("Restaurant not found with id: " + restaurantId);
        }

        List<MenuItem> menuItems = menuItemRepository.searchByRestaurantAndName(
                restaurantId, searchTerm.trim(), MenuItemStatus.AVAILABLE);

        if (menuItems.isEmpty()) {
            log.info("No search results found for term '{}' in restaurant: {}", searchTerm, restaurant.getName());
            return buildErrorResponse("No menu items found matching '" + searchTerm + "' for restaurant: " + restaurant.getName());
        }

        List<MenuItemResponse> itemResponses = menuItems.stream()
                .map(this::buildMenuItemData)
                .collect(Collectors.toList());

        log.info("Search completed: found {} items for term '{}'", menuItems.size(), searchTerm);
        return buildListSuccessResponse(itemResponses, "Search completed successfully");
    }

    public MenuItemResponse getMenuCategories(Long restaurantId) {
        log.info("Fetching categories for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            log.warn("Restaurant not found with ID: {}", restaurantId);
            return buildErrorResponse("Restaurant not found with id: " + restaurantId);
        }

        List<String> categories = menuItemRepository.findDistinctCategoriesByRestaurantId(restaurantId);

        if (categories.isEmpty()) {
            log.info("No categories found for restaurant: {}", restaurant.getName());
            return buildErrorResponse("No categories found for restaurant: " + restaurant.getName());
        }

        log.info("Retrieved {} categories for restaurant: {}", categories.size(), restaurant.getName());
        return buildCategoriesSuccessResponse(categories, "Categories retrieved successfully");
    }

    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request, Long ownerId) {
        log.info("Updating menu item ID: {} by owner ID: {}", id, ownerId);
        log.debug("Update data: name={}, price={}", request.getName(), request.getPrice());

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(id, request.getRestaurantId()).orElse(null);

        if (menuItem == null) {
            log.warn("Menu item not found with ID: {} in restaurant ID: {}", id, request.getRestaurantId());
            return buildErrorResponse("Menu item not found with id: " + id + " in restaurant: " + request.getRestaurantId());
        }

        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getOwner() == null ||
                !menuItem.getRestaurant().getOwner().getId().equals(ownerId)) {
            log.warn("Permission denied: owner ID {} cannot update menu item ID {}", ownerId, id);
            return buildErrorResponse("You don't have permission to update this menu item");
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setCategory(request.getCategory());
        menuItem.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            menuItem.setStatus(request.getStatus());
        }
        menuItem.setImageUrl(request.getImageUrl());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Menu item updated successfully: ID={}", updatedMenuItem.getId());

        return buildSuccessResponse(updatedMenuItem, "Menu item updated successfully");
    }

    public MenuItemResponse updateMenuItemStatus(Long id, Long restaurantId, Long ownerId, MenuItemStatus newStatus) {
        log.info("Updating status for menu item ID: {} to {} by owner ID: {}", id, newStatus, ownerId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(id, restaurantId).orElse(null);

        if (menuItem == null) {
            log.warn("Menu item not found with ID: {} in restaurant ID: {}", id, restaurantId);
            return buildErrorResponse("Menu item not found with id: " + id + " in restaurant: " + restaurantId);
        }

        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getOwner() == null ||
                !menuItem.getRestaurant().getOwner().getId().equals(ownerId)) {
            log.warn("Permission denied: owner ID {} cannot update status for menu item ID {}", ownerId, id);
            return buildErrorResponse("You don't have permission to update this menu item");
        }

        menuItem.setStatus(newStatus);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);

        log.info("Menu item status updated successfully: ID={}, newStatus={}", updatedMenuItem.getId(), newStatus);
        return buildSuccessResponse(updatedMenuItem, "Menu item status updated to: " + newStatus);
    }

    public MenuItemResponse deleteMenuItem(Long id, Long restaurantId, Long ownerId) {
        log.info("Deleting menu item ID: {} by owner ID: {}", id, ownerId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(id, restaurantId).orElse(null);

        if (menuItem == null) {
            log.warn("Menu item not found with ID: {} in restaurant ID: {}", id, restaurantId);
            return buildErrorResponse("Menu item not found with id: " + id + " in restaurant: " + restaurantId);
        }

        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getOwner() == null ||
                !menuItem.getRestaurant().getOwner().getId().equals(ownerId)) {
            log.warn("Permission denied: owner ID {} cannot delete menu item ID {}", ownerId, id);
            return buildErrorResponse("You don't have permission to delete this menu item");
        }

        menuItemRepository.delete(menuItem);
        log.info("Menu item deleted successfully: ID={}", id);

        return MenuItemResponse.builder()
                .id(id)
                .success(true)
                .message("Menu item deleted successfully")
                .build();
    }

    // ========== HELPER METHODS WITH LOGGER ==========

    private MenuItemResponse buildSuccessResponse(MenuItem menuItem, String message) {
        log.debug("Building success response for menu item ID: {} with message: {}", menuItem.getId(), message);

        MenuItemResponse response = buildMenuItemData(menuItem);
        response.setMessage(message);
        response.setSuccess(true);

        log.trace("Success response built: ID={}, name={}", response.getId(), response.getName());
        return response;
    }

    private MenuItemResponse buildListSuccessResponse(List<MenuItemResponse> menuItems, String message) {
        log.debug("Building list success response with {} items, message: {}",
                menuItems.size(), message);

        MenuItemResponse response = MenuItemResponse.builder()
                .success(true)
                .message(message)
                .menuItems(menuItems)
                .build();

        log.trace("List response built: {} items, success={}",
                response.getMenuItems().size(), response.getSuccess());
        return response;
    }

    private MenuItemResponse buildCategoriesSuccessResponse(List<String> categories, String message) {
        log.debug("Building categories success response with {} categories, message: {}",
                categories.size(), message);

        MenuItemResponse response = MenuItemResponse.builder()
                .success(true)
                .message(message)
                .categories(categories)
                .build();

        log.trace("Categories response built: {} categories, success={}",
                response.getCategories().size(), response.getSuccess());
        return response;
    }

    private MenuItemResponse buildErrorResponse(String message) {
        log.debug("Building error response with message: {}", message);

        MenuItemResponse response = MenuItemResponse.builder()
                .success(false)
                .message(message)
                .build();

        log.trace("Error response built: message={}, success={}", response.getMessage(), response.getSuccess());
        return response;
    }

    private MenuItemResponse buildMenuItemData(MenuItem menuItem) {
        log.trace("Building menu item data for ID: {}, name: {}", menuItem.getId(), menuItem.getName());

        Long restaurantId = null;
        String restaurantName = null;
        if (menuItem.getRestaurant() != null) {
            restaurantId = menuItem.getRestaurant().getId();
            restaurantName = menuItem.getRestaurant().getName();
        }

        MenuItemResponse response = MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .status(menuItem.getStatus())
                .imageUrl(menuItem.getImageUrl())
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .createdAt(menuItem.getCreatedAt())
                .build();

        log.trace("Menu item data built: ID={}, name={}, category={}",
                response.getId(), response.getName(), response.getCategory());
        return response;
    }
}