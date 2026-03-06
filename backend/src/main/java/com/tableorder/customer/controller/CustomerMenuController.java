package com.tableorder.customer.controller;

import com.tableorder.customer.dto.CategoriesResponse;
import com.tableorder.customer.dto.MenuListResponse;
import com.tableorder.customer.dto.SpicyOptionsResponse;
import com.tableorder.customer.service.CustomerMenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores/{storeId}/customer")
public class CustomerMenuController {
    private final CustomerMenuService menuService;

    public CustomerMenuController(CustomerMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/categories")
    public ResponseEntity<CategoriesResponse> getCategories(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getCategories(storeId));
    }

    @GetMapping("/menus")
    public ResponseEntity<MenuListResponse> getMenusByCategory(@PathVariable Long storeId,
                                                                @RequestParam String category) {
        return ResponseEntity.ok(menuService.getMenusByCategory(storeId, category));
    }

    @GetMapping("/menus/{menuId}/spicy-options")
    public ResponseEntity<SpicyOptionsResponse> getSpicyOptions(@PathVariable Long storeId,
                                                                 @PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getSpicyOptions(storeId, menuId));
    }
}
