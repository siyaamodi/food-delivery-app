package com.example.food_delivery.controller;

import com.example.food_delivery.dto.MenuItemRequest;
import com.example.food_delivery.dto.MenuItemResponse;
import com.example.food_delivery.enums.MenuItemStatus;
import com.example.food_delivery.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // CREATE - Add new menu item
    @PostMapping("/createMenuItem")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.createMenuItem(request);
        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Get single menu item by ID
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        MenuItemResponse response = menuItemService.getMenuItemById(id);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Get menu item by ID and Restaurant ID (security check)
    @GetMapping("/{id}/restaurant/{restaurantId}")
    public ResponseEntity<MenuItemResponse> getMenuItemByIdAndRestaurant(
            @PathVariable Long id,
            @PathVariable Long restaurantId) {
        MenuItemResponse response = menuItemService.getMenuItemByIdAndRestaurant(id, restaurantId);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Get all menu items for a restaurant
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<MenuItemResponse> getMenuByRestaurant(@PathVariable Long restaurantId) {
        MenuItemResponse response = menuItemService.getMenuByRestaurant(restaurantId);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Get menu items by category for a restaurant
    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    public ResponseEntity<MenuItemResponse> getMenuByCategory(
            @PathVariable Long restaurantId,
            @PathVariable String category) {
        MenuItemResponse response = menuItemService.getMenuByCategory(restaurantId, category);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Search menu items in a restaurant
    @GetMapping("/restaurant/{restaurantId}/search")
    public ResponseEntity<MenuItemResponse> searchMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam String searchTerm) {
        MenuItemResponse response = menuItemService.searchMenuItems(restaurantId, searchTerm);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // READ - Get all categories for a restaurant
    @GetMapping("/restaurant/{restaurantId}/categories")
    public ResponseEntity<MenuItemResponse> getMenuCategories(@PathVariable Long restaurantId) {
        MenuItemResponse response = menuItemService.getMenuCategories(restaurantId);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    // UPDATE - Update menu item
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            @RequestParam Long ownerId) {
        MenuItemResponse response = menuItemService.updateMenuItem(id, request, ownerId);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK :
                response.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    // UPDATE - Update only menu item status
    @PatchMapping("/{id}/status")
    public ResponseEntity<MenuItemResponse> updateMenuItemStatus(
            @PathVariable Long id,
            @RequestParam Long restaurantId,
            @RequestParam Long ownerId,
            @RequestParam MenuItemStatus status) {
        MenuItemResponse response = menuItemService.updateMenuItemStatus(id, restaurantId, ownerId, status);
        HttpStatus httpStatus = response.getSuccess() ? HttpStatus.OK :
                response.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(response);
    }

    // DELETE - Delete menu item
    @DeleteMapping("/{id}")
    public ResponseEntity<MenuItemResponse> deleteMenuItem(
            @PathVariable Long id,
            @RequestParam Long restaurantId,
            @RequestParam Long ownerId) {
        MenuItemResponse response = menuItemService.deleteMenuItem(id, restaurantId, ownerId);
        HttpStatus status = response.getSuccess() ? HttpStatus.OK :
                response.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}