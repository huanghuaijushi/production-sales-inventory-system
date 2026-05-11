package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.StockCheckOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockCheckOrderRepository extends JpaRepository<StockCheckOrder, Long> {

    @Query("SELECT o FROM StockCheckOrder o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.batch LEFT JOIN FETCH i.stockRecord WHERE o.id = :id")
    Optional<StockCheckOrder> findWithItemsById(Long id);

    @Query("SELECT o FROM StockCheckOrder o ORDER BY o.createdAt DESC")
    Page<StockCheckOrder> findPage(Pageable pageable);
}
