package com.modern.catalog.service;

import com.modern.catalog.exception.ProductNotFoundException;
import com.modern.catalog.model.dto.CategorySummaryDTO;
import com.modern.catalog.model.dto.InventoryReportDTO;
import com.modern.catalog.model.dto.ProductReportDTO;
import com.modern.catalog.model.entity.ProductEntity;
import com.modern.catalog.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReportServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductReportService service;

    @Nested
    @DisplayName("Inventory Report")
    class InventoryReportTest {

        @Test
        @DisplayName("Should generate inventory report with low stock products")
        void shouldGenerateInventoryReport() {
            var lowStockProducts = List.of(
                    new ProductEntity("Keyboard", BigDecimal.valueOf(45), "ELECTRONICS", 3),
                    new ProductEntity("Mug", BigDecimal.valueOf(12), "OTHER", 2)
            );
            var categorySummary = List.of(
                    new CategorySummaryDTO("ELECTRONICS", 2L, BigDecimal.valueOf(45), 103L)
            );

            when(repository.findByStockLessThan(10)).thenReturn(lowStockProducts);
            when(repository.getCategorySummaries()).thenReturn(categorySummary);
            when(repository.count()).thenReturn(7L);
            when(repository.countByStockLessThan(10)).thenReturn(2L);
            when(repository.countByStock(0)).thenReturn(1L);

            InventoryReportDTO report = service.generateInventoryReport(10);

            assertThat(report).isNotNull();
            assertThat(report.getTotalProducts()).isEqualTo(7);
            assertThat(report.getLowStockCount()).isEqualTo(2);
            assertThat(report.getOutOfStockCount()).isEqualTo(1);
            assertThat(report.getLowStockProducts()).hasSize(2);
            assertThat(report.getCategorySummaries()).hasSize(1);

            verify(repository).findByStockLessThan(10);
            verify(repository).getCategorySummaries();
        }

        @Test
        @DisplayName("Should use default threshold when invalid value provided")
        void shouldUseDefaultThresholdForInvalidValues() {
            var empty = List.<ProductEntity>of();
            when(repository.findByStockLessThan(10)).thenReturn(empty);
            when(repository.getCategorySummaries()).thenReturn(List.of());
            when(repository.count()).thenReturn(0L);
            when(repository.countByStockLessThan(10)).thenReturn(0L);
            when(repository.countByStock(0)).thenReturn(0L);

            service.generateInventoryReport(-5);

            verify(repository).findByStockLessThan(10);
        }
    }

    @Nested
    @DisplayName("Product Search")
    class ProductSearchTest {

        @Test
        @DisplayName("Should search products by name or category")
        void shouldSearchProducts() {
            var products = List.of(
                    new ProductEntity("Laptop", BigDecimal.valueOf(1000), "ELECTRONICS", 5)
            );
            var page = new PageImpl<>(products);

            when(repository.findByNameContainingIgnoreCaseOrCategoryIgnoreCase(
                    anyString(), anyString(), any()))
                    .thenReturn(page);

            Page<ProductReportDTO> result = service.searchProducts("laptop", PageRequest.of(0, 20));

            assertThat(result).isNotEmpty();
            assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("Should return all products when query is blank")
        void shouldReturnAllWhenQueryBlank() {
            when(repository.findAll(any(PageRequest.class)))
                    .thenReturn(Page.empty());

            Page<ProductReportDTO> result = service.searchProducts("", PageRequest.of(0, 20));

            assertThat(result).isEmpty();
            verify(repository).findAll(any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("Product Report by ID")
    class ProductByIdTest {

        @Test
        @DisplayName("Should return report when product exists")
        void shouldReturnReportWhenProductExists() {
            var entity = new ProductEntity("Mouse", BigDecimal.valueOf(25), "ELECTRONICS", 5);
            entity.setId(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            ProductReportDTO result = service.getProductReport(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Mouse");
            assertThat(result.getStockStatus()).isEqualTo(ProductReportDTO.StockStatus.LOW);
        }

        @Test
        @DisplayName("Should throw when product not found")
        void shouldThrowWhenProductNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProductReport(99L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Category Summaries")
    class CategorySummaryTest {

        @Test
        @DisplayName("Should return category summaries")
        void shouldReturnCategorySummaries() {
            var summaries = List.of(
                    new CategorySummaryDTO("ELECTRONICS", 4L, BigDecimal.valueOf(450), 368L)
            );

            when(repository.getCategorySummaries()).thenReturn(summaries);

            var result = service.getCategorySummaries();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("ELECTRONICS");
            assertThat(result.get(0).getProductCount()).isEqualTo(4);
        }
    }
}
