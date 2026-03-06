package com.tableorder.customer.service;

import com.tableorder.common.entity.Menu;
import com.tableorder.common.entity.MenuSpicyOption;
import com.tableorder.customer.dto.CategoriesResponse;
import com.tableorder.customer.dto.MenuListResponse;
import com.tableorder.customer.dto.SpicyOptionsResponse;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.repository.MenuRepository;
import com.tableorder.customer.repository.MenuSpicyOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerMenuServiceTest {

    @Mock private MenuRepository menuRepository;
    @Mock private MenuSpicyOptionRepository menuSpicyOptionRepository;
    @InjectMocks private CustomerMenuService service;

    // === TC-CUST-001: 카테고리 목록 반환 ===
    @Test
    @DisplayName("TC-CUST-001: 매장에 메뉴가 있을 때 카테고리 목록 반환")
    void getCategories_returnsCategories() {
        when(menuRepository.findDistinctCategoryByStoreIdOrderByCategory(1L))
                .thenReturn(List.of("볶음", "찌개"));

        CategoriesResponse result = service.getCategories(1L);

        assertThat(result.getCategories()).containsExactly("볶음", "찌개");
    }

    // === TC-CUST-002: 메뉴 없으면 빈 목록 ===
    @Test
    @DisplayName("TC-CUST-002: 매장에 메뉴가 없을 때 빈 목록 반환")
    void getCategories_empty() {
        when(menuRepository.findDistinctCategoryByStoreIdOrderByCategory(99L))
                .thenReturn(Collections.emptyList());

        CategoriesResponse result = service.getCategories(99L);

        assertThat(result.getCategories()).isEmpty();
    }

    // === TC-CUST-003: 카테고리별 메뉴 목록 (hasSpicyOptions 포함) ===
    @Test
    @DisplayName("TC-CUST-003: 카테고리별 메뉴 목록 반환 (hasSpicyOptions 포함)")
    void getMenusByCategory_returnsMenusWithSpicyFlag() {
        Menu m1 = createMenu(1L, 1L, "김치찌개", 9000, "찌개", "매움");
        Menu m2 = createMenu(2L, 1L, "된장찌개", 8000, "찌개", "보통");
        when(menuRepository.findByStoreIdAndCategoryOrderByDisplayOrder(1L, "찌개"))
                .thenReturn(List.of(m1, m2));
        when(menuSpicyOptionRepository.existsByMenuId(1L)).thenReturn(true);
        when(menuSpicyOptionRepository.existsByMenuId(2L)).thenReturn(false);

        MenuListResponse result = service.getMenusByCategory(1L, "찌개");

        assertThat(result.getMenus()).hasSize(2);
        assertThat(result.getMenus().get(0).isHasSpicyOptions()).isTrue();
        assertThat(result.getMenus().get(1).isHasSpicyOptions()).isFalse();
    }

    // === TC-CUST-004: 해당 카테고리에 메뉴 없으면 빈 목록 ===
    @Test
    @DisplayName("TC-CUST-004: 해당 카테고리에 메뉴 없으면 빈 목록 반환")
    void getMenusByCategory_empty() {
        when(menuRepository.findByStoreIdAndCategoryOrderByDisplayOrder(1L, "디저트"))
                .thenReturn(Collections.emptyList());

        MenuListResponse result = service.getMenusByCategory(1L, "디저트");

        assertThat(result.getMenus()).isEmpty();
    }

    // === TC-CUST-005: 맵기 옵션 목록 반환 ===
    @Test
    @DisplayName("TC-CUST-005: 메뉴의 맵기 옵션 목록 반환")
    void getSpicyOptions_returnsOptions() {
        Menu menu = createMenu(1L, 1L, "김치찌개", 9000, "찌개", "매움");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        MenuSpicyOption o1 = createOption(1L, 1L, "순한맛");
        MenuSpicyOption o2 = createOption(2L, 1L, "보통");
        when(menuSpicyOptionRepository.findByMenuIdOrderByDisplayOrder(1L))
                .thenReturn(List.of(o1, o2));

        SpicyOptionsResponse result = service.getSpicyOptions(1L, 1L);

        assertThat(result.getOptions()).hasSize(2);
        assertThat(result.getOptions().get(0).getOptionName()).isEqualTo("순한맛");
    }

    // === TC-CUST-006: 존재하지 않는 메뉴 → MENU_NOT_FOUND ===
    @Test
    @DisplayName("TC-CUST-006: 존재하지 않는 메뉴 → MENU_NOT_FOUND")
    void getSpicyOptions_menuNotFound() {
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSpicyOptions(1L, 999L))
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("MENU_NOT_FOUND"));
    }

    // === TC-CUST-007: 다른 매장의 메뉴 → MENU_NOT_FOUND ===
    @Test
    @DisplayName("TC-CUST-007: 다른 매장의 메뉴 → MENU_NOT_FOUND")
    void getSpicyOptions_wrongStore() {
        Menu menu = createMenu(1L, 2L, "김치찌개", 9000, "찌개", "매움"); // storeId=2
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        assertThatThrownBy(() -> service.getSpicyOptions(1L, 1L)) // storeId=1 요청
                .isInstanceOf(CustomerException.class)
                .satisfies(ex -> assertThat(((CustomerException) ex).getErrorCode()).isEqualTo("MENU_NOT_FOUND"));
    }

    // === Helper Methods ===
    private Menu createMenu(Long id, Long storeId, String name, int price, String category, String spicyLevel) {
        Menu m = new Menu();
        m.setId(id);
        m.setStoreId(storeId);
        m.setName(name);
        m.setPrice(price);
        m.setCategory(category);
        m.setSpicyLevel(spicyLevel);
        return m;
    }

    private MenuSpicyOption createOption(Long id, Long menuId, String optionName) {
        MenuSpicyOption o = new MenuSpicyOption();
        o.setId(id);
        o.setMenuId(menuId);
        o.setOptionName(optionName);
        return o;
    }
}
