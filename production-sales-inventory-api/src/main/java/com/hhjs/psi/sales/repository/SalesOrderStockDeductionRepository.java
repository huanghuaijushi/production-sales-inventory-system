package com.hhjs.psi.sales.repository;

import com.hhjs.psi.sales.entity.SalesOrderStockDeduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderStockDeductionRepository extends JpaRepository<SalesOrderStockDeduction, Long> {
}
