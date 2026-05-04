package com.hhjs.psi.inventory.repository;

import com.hhjs.psi.inventory.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE p.id = :productId")
    Optional<Stock> findByProductIdForUpdate(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE s.id = :stockId")
    Optional<Stock> findByIdForUpdate(Long stockId);

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE p.enabled = true")
    List<Stock> findAllWithProduct();

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE p.enabled = true")
    Page<Stock> findAllWithProduct(Pageable pageable);

    @Query("""
            SELECT s FROM Stock s JOIN FETCH s.product p
            WHERE p.enabled = true
              AND (:query IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(p.category, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(p.specification, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:category IS NULL OR p.category = :category)
              AND (:status IS NULL
                   OR (:status = 'normal' AND s.quantity > p.alertQuantity)
                   OR (:status = 'warning' AND s.quantity > 0 AND s.quantity <= p.alertQuantity)
                   OR (:status = 'out-of-stock' AND s.quantity = 0))
            """)
    Page<Stock> searchStocks(String query, String category, String status, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE p.enabled = true AND s.quantity <= p.alertQuantity")
    List<Stock> findLowStockItems();

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE p.id IN :productIds")
    List<Stock> findByProductIds(List<Long> productIds);
}
