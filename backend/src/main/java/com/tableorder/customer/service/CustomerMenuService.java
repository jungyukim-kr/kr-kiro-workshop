package com.tableorder.customer.service;

import com.tableorder.common.entity.Menu;
import com.tableorder.common.entity.MenuSpicyOption;
import com.tableorder.customer.dto.*;
import com.tableorder.customer.exception.CustomerException;
import com.tableorder.customer.repository.MenuRepository;
import com.tableorder.customer.repository.MenuSpicyOptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerMenuService {
    private final MenuRepository menuRepository;
    private final MenuSpicyOptionRepository menuSpicyOptionRepository;

    public CustomerMenuService(MenuRepository menuRepository, MenuSpicyOptionRepository menuSpicyOptionRepository) {
        this.menuRepository = menuRepository;
        this.menuSpicyOptionRepository = menuSpicyOptionRepository;
    }

    public CategoriesResponse getCategories(Long storeId) {
        List<String> categories = menuRepository.findDistinctCategoryByStoreIdOrderByCategory(storeId);
        return new CategoriesResponse(categories);
    }

    public MenuListResponse getMenusByCategory(Long storeId, String category) {
        List<Menu> menus = menuRepository.findByStoreIdAndCategoryOrderByDisplayOrder(storeId, category);
        List<MenuDto> dtos = menus.stream().map(m -> {
            MenuDto dto = new MenuDto();
            dto.setId(m.getId());
            dto.setName(m.getName());
            dto.setPrice(m.getPrice());
            dto.setDescription(m.getDescription());
            dto.setCategory(m.getCategory());
            dto.setImageUrl(m.getImageUrl());
            dto.setSpicyLevel(m.getSpicyLevel());
            dto.setHasSpicyOptions(menuSpicyOptionRepository.existsByMenuId(m.getId()));
            return dto;
        }).toList();
        return new MenuListResponse(dtos);
    }

    public SpicyOptionsResponse getSpicyOptions(Long storeId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .filter(m -> m.getStoreId().equals(storeId))
                .orElseThrow(() -> new CustomerException("MENU_NOT_FOUND",
                        "존재하지 않는 메뉴입니다 (menuId: " + menuId + ")", HttpStatus.NOT_FOUND));

        List<MenuSpicyOption> options = menuSpicyOptionRepository.findByMenuIdOrderByDisplayOrder(menuId);
        List<SpicyOptionDto> dtos = options.stream()
                .map(o -> new SpicyOptionDto(o.getId(), o.getOptionName())).toList();
        return new SpicyOptionsResponse(dtos);
    }
}
