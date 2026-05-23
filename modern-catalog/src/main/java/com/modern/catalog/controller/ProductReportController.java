package com.modern.catalog.controller;

import com.modern.catalog.model.dto.CategorySummaryDTO;
import com.modern.catalog.model.dto.InventoryReportDTO;
import com.modern.catalog.model.dto.ProductReportDTO;
import com.modern.catalog.service.ProductReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/products/reports")
@Tag(name = "Product Reports", description = "Modern reporting API for product analytics. " +
        "This API is built following the Strangler Fig pattern - new functionality alongside legacy SOAP.")
public class ProductReportController {

    private final ProductReportService reportService;

    public ProductReportController(ProductReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    @Operation(summary = "Generate complete inventory report",
            description = "Returns low stock products, category summaries, and global counts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid threshold parameter")
    })
    public ResponseEntity<InventoryReportDTO> getInventoryReport(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Threshold must be at least 1")
            @Parameter(description = "Low stock threshold (products with stock below this are flagged)")
            int lowStockThreshold) {

        InventoryReportDTO report = reportService.generateInventoryReport(lowStockThreshold);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get category summaries",
            description = "Aggregated statistics per product category")
    public ResponseEntity<List<CategorySummaryDTO>> getCategorySummaries() {
        List<CategorySummaryDTO> summaries = reportService.getCategorySummaries();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products",
            description = "Full-text search across product names and categories with pagination")
    public ResponseEntity<PagedModel<EntityModel<ProductReportDTO>>> searchProducts(
            @RequestParam(required = false)
            @Parameter(description = "Search query (matches name or category)")
            String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable,
            PagedResourcesAssembler<ProductReportDTO> assembler) {

        Page<ProductReportDTO> page = reportService.searchProducts(q, pageable);
        PagedModel<EntityModel<ProductReportDTO>> pagedModel = assembler.toModel(page);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product report by ID",
            description = "Returns detailed report information for a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductReportDTO> getProductReport(
            @PathVariable
            @Parameter(description = "Product ID")
            Long id) {

        ProductReportDTO report = reportService.getProductReport(id);

        report.add(linkTo(methodOn(ProductReportController.class).getProductReport(id)).withSelfRel());
        report.add(linkTo(methodOn(ProductReportController.class).searchProducts(null,
                Pageable.ofSize(20), null)).withRel("products"));

        return ResponseEntity.ok(report);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for the modern API",
            description = "Simple health endpoint to verify the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Modern Catalog API is running. Strangler Fig pattern in action.");
    }
}
