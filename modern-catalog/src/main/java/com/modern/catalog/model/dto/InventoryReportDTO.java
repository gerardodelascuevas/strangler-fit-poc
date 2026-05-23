package com.modern.catalog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Complete inventory report")
public class InventoryReportDTO {

    private final List<ProductReportDTO> lowStockProducts;
    private final List<CategorySummaryDTO> categorySummaries;
    private final Long totalProducts;
    private final Long lowStockCount;
    private final Long outOfStockCount;

    public InventoryReportDTO(List<ProductReportDTO> lowStockProducts, List<CategorySummaryDTO> categorySummaries,
                              Long totalProducts, Long lowStockCount, Long outOfStockCount) {
        this.lowStockProducts = lowStockProducts;
        this.categorySummaries = categorySummaries;
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
    }

    @Schema(description = "Products with stock below threshold")
    public List<ProductReportDTO> getLowStockProducts() {
        return lowStockProducts;
    }

    @Schema(description = "Summary per category")
    public List<CategorySummaryDTO> getCategorySummaries() {
        return categorySummaries;
    }

    @Schema(description = "Total number of products in catalog", example = "7")
    public Long getTotalProducts() {
        return totalProducts;
    }

    @Schema(description = "Number of products with low stock", example = "3")
    public Long getLowStockCount() {
        return lowStockCount;
    }

    @Schema(description = "Number of products out of stock", example = "1")
    public Long getOutOfStockCount() {
        return outOfStockCount;
    }
}
