package com.tableorder.customer.controller;

import com.tableorder.auth.dto.AuthInfo;
import com.tableorder.customer.dto.*;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.service.CustomerMenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerMenuControllerTest {

    @Mock private CustomerMenuService menuService;
    @InjectMocks private CustomerMenuController controller;

    // === TC-CUST-018: GET /categories → 200 OK ===
    @Test
    @DisplayName("TC-CUST-018: GET /categories → 200 OK")
    void getCategories_200() {
        when(menuService.getCategories(1L)).thenReturn(new CategoriesResponse(List.of("볶음", "찌개")));

        ResponseEntity<CategoriesResponse> result = controller.getCategories(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getCategories()).containsExactly("볶음", "찌개");
    }

    // === TC-CUST-019: GET /menus?category=찌개 → 200 OK ===
    @Test
    @DisplayName("TC-CUST-019: GET /menus?category=찌개 → 200 OK")
    void getMenusByCategory_200() {
        MenuDto dto = new MenuDto();
        dto.setId(1L);
        dto.setName("김치찌개");
        when(menuService.getMenusByCategory(1L, "찌개")).thenReturn(new MenuListResponse(List.of(dto)));

        ResponseEntity<MenuListResponse> result = controller.getMenusByCategory(1L, "찌개");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getMenus()).hasSize(1);
    }

    // === TC-CUST-020: GET /menus/{menuId}/spicy-options → 200 OK ===
    @Test
    @DisplayName("TC-CUST-020: GET /menus/{menuId}/spicy-options → 200 OK")
    void getSpicyOptions_200() {
        SpicyOptionsResponse resp = new SpicyOptionsResponse(List.of(
                new SpicyOptionDto(1L, "순한맛"), new SpicyOptionDto(2L, "보통"),
                new SpicyOptionDto(3L, "매운맛"), new SpicyOptionDto(4L, "아주매운맛")));
        when(menuService.getSpicyOptions(1L, 1L)).thenReturn(resp);

        ResponseEntity<SpicyOptionsResponse> result = controller.getSpicyOptions(1L, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getOptions()).hasSize(4);
    }

    // === TC-CUST-021: GET /menus/{menuId}/spicy-options → 404 MENU_NOT_FOUND ===
    @Test
    @DisplayName("TC-CUST-021: GET /menus/{menuId}/spicy-options → 404 MENU_NOT_FOUND")
    void getSpicyOptions_404() {
        when(menuService.getSpicyOptions(1L, 999L))
                .thenThrow(new CustomerException("MENU_NOT_FOUND", "존재하지 않는 메뉴입니다 (menuId: 999)", HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> controller.getSpicyOptions(1L, 999L))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("MENU_NOT_FOUND"));
    }
}
