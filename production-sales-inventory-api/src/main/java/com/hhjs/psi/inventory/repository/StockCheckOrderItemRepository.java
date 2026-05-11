package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.StockCheckOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCheckOrderItemRepository extends JpaRepository<StockCheckOrderItem, Long> {
}
