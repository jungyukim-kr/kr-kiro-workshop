package com.tableorder.auth.repository;

import com.tableorder.common.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {
    Optional<StoreTable> findByStoreIdAndTableNumber(Long storeId, int tableNumber);
}
