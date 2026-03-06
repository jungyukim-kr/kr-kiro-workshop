package com.tableorder.customer.repository;

import com.tableorder.common.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT DISTINCT m.category FROM Menu m WHERE m.storeId = :storeId ORDER BY m.category")
    List<String> findDistinctCategoryByStoreIdOrderByCategory(Long storeId);

    List<Menu> findByStoreIdAndCategoryOrderByDisplayOrder(Long storeId, String category);
}
