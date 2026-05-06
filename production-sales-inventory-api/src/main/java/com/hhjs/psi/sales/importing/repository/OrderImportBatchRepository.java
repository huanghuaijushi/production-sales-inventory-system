package com.hhjs.psi.sales.importing.repository;

import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderImportBatchRepository extends JpaRepository<OrderImportBatch, Long> {

    @EntityGraph(attributePaths = {"channel"})
    Optional<OrderImportBatch> findWithChannelById(Long id);

    @EntityGraph(attributePaths = {"channel"})
    Page<OrderImportBatch> findAll(Pageable pageable);
}
