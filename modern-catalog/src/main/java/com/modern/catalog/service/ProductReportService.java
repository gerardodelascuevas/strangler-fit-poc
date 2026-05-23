package com.modern.catalog.service;

import com.modern.catalog.model.dto.CategorySummaryDTO;
import com.modern.catalog.model.dto.InventoryReportDTO;
import com.modern.catalog.model.dto.ProductReportDTO;
import com.modern.catalog.model.entity.ProductEntity;
import com.modern.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductReportService {

    private static final Logger log = LoggerFactory.getLogger(ProductReportService.class);
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository repository;

    public ProductReportService(ProductRepository repository) {
        this.repository = repository;
    }

    public InventoryReportDTO generateInventoryReport(int lowStockThreshold) {
        log.info("Generating inventory report with low stock threshold={}", lowStockThreshold);

        int threshold = lowStockThreshold > 0 ? lowStockThreshold : DEFAULT_LOW_STOCK_THRESHOLD;

        List<ProductReportDTO> lowStockProducts = repository.findByStockLessThan(threshold)
                .stream()
                .map(this::toReportDTO)
                .toList();

        List<CategorySummaryDTO> categorySummaries = repository.getCategorySummaries();

        long totalProducts = repository.count();
        long lowStockCount = repository.countByStockLessThan(threshold);
        long outOfStockCount = repository.countByStock(0);

        log.info("Inventory report generated: {} total products, {} low stock, {} out of stock",
                totalProducts, lowStockCount, outOfStockCount);

        return new InventoryReportDTO(lowStockProducts, categorySummaries,
                totalProducts, lowStockCount, outOfStockCount);
    }

    public List<CategorySummaryDTO> getCategorySummaries() {
        log.debug("Fetching category summaries");
        return repository.getCategorySummaries();
    }

    public Page<ProductReportDTO> searchProducts(String query, Pageable pageable) {
        log.info("Searching products with query='{}', page={}, size={}", query,
                pageable.getPageNumber(), pageable.getPageSize());

        if (query == null || query.isBlank()) {
            return repository.findAll(pageable).map(this::toReportDTO);
        }

        return repository.findByNameContainingIgnoreCaseOrCategoryIgnoreCase(query, query, pageable)
                .map(this::toReportDTO);
    }

    public ProductReportDTO getProductReport(Long id) {
        log.debug("Fetching report for product id={}", id);
        return repository.findById(id)
                .map(this::toReportDTO)
                .orElseThrow(() -> new com.modern.catalog.exception.ProductNotFoundException(id));
    }

    private ProductReportDTO toReportDTO(ProductEntity entity) {
        return new ProductReportDTO(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getCategory(),
                entity.getStock()
        );
    }
}
