package com.modern.catalog.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Category summary statistics")
public class CategorySummaryDTO {

    private final String category;
    private final Long productCount;
    private final BigDecimal averagePrice;
    private final Long totalStock;

    public CategorySummaryDTO(String category, Long productCount, Double averagePrice, Long totalStock) {
        this.category = category;
        this.productCount = productCount;
        this.averagePrice = averagePrice != null
                ? BigDecimal.valueOf(averagePrice).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        this.totalStock = totalStock;
    }

    @Schema(description = "Product category", example = "ELECTRONICS")
    public String getCategory() {
        return category;
    }

    @Schema(description = "Number of products in this category", example = "4")
    public Long getProductCount() {
        return productCount;
    }

    @Schema(description = "Average price across products in this category", example = "542.62")
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    @Schema(description = "Total stock across all products in this category", example = "368")
    public Long getTotalStock() {
        return totalStock;
    }
}
