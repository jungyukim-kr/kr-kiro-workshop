package com.tableorder.customer.repository;

import com.tableorder.common.entity.MenuSpicyOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuSpicyOptionRepository extends JpaRepository<MenuSpicyOption, Long> {
    List<MenuSpicyOption> findByMenuIdOrderByDisplayOrder(Long menuId);
    boolean existsByMenuId(Long menuId);
}
