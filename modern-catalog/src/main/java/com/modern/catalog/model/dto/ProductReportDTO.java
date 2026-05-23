package com.modern.catalog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "products")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product report information")
public class ProductReportDTO extends RepresentationModel<ProductReportDTO> {

    private final Long id;
    private final String name;
    private final BigDecimal price;
    private final String formattedPrice;
    private final String category;
    private final Integer stock;
    private final StockStatus stockStatus;

    public ProductReportDTO(Long id, String name, BigDecimal price, String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.formattedPrice = "$" + (price != null ? String.format("%.2f", price) : "0.00");
        this.category = category;
        this.stock = stock;
        this.stockStatus = StockStatus.from(stock);
    }

    @Schema(description = "Product identifier")
    public Long getId() {
        return id;
    }

    @Schema(description = "Product name", example = "Laptop")
    public String getName() {
        return name;
    }

    @Schema(description = "Product price", example = "1200.00")
    public BigDecimal getPrice() {
        return price;
    }

    @Schema(description = "Formatted price with currency symbol", example = "$1,200.00")
    public String getFormattedPrice() {
        return formattedPrice;
    }

    @Schema(description = "Product category", example = "ELECTRONICS")
    public String getCategory() {
        return category;
    }

    @Schema(description = "Current stock quantity", example = "15")
    public Integer getStock() {
        return stock;
    }

    @Schema(description = "Stock status indicator")
    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public enum StockStatus {
        OUT_OF_STOCK,
        LOW,
        IN_STOCK;

        static StockStatus from(Integer stock) {
            if (stock == null || stock == 0) return OUT_OF_STOCK;
            if (stock < 10) return LOW;
            return IN_STOCK;
        }
    }
}
