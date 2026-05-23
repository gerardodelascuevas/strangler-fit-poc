package com.modern.catalog.repository;

import com.modern.catalog.model.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    List<ProductEntity> findByCategoryIgnoreCase(String category);

    List<ProductEntity> findByStockLessThan(Integer threshold);

    List<ProductEntity> findByStock(Integer stock);

    Page<ProductEntity> findByNameContainingIgnoreCaseOrCategoryIgnoreCase(String name, String category, Pageable pageable);

    @Query("SELECT new com.modern.catalog.model.dto.CategorySummaryDTO(" +
            "  p.category, COUNT(p), AVG(p.price), SUM(p.stock)) " +
            "FROM ProductEntity p GROUP BY p.category ORDER BY p.category")
    List<com.modern.catalog.model.dto.CategorySummaryDTO> getCategorySummaries();

    long countByStockLessThan(Integer threshold);

    long countByStock(Integer stock);
}
