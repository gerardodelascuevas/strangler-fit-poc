package com.modern.catalog.config;

import com.modern.catalog.model.entity.ProductEntity;
import com.modern.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository repository;

    public DataInitializer(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        long existing = repository.count();
        if (existing > 0) {
            log.info("Database already has {} products (likely seeded by legacy service), skipping initialization", existing);
            return;
        }

        log.info("Seeding database with initial product data (no legacy data found)");

        List<ProductEntity> products = List.of(
                new ProductEntity("Laptop Pro", new BigDecimal("1299.99"), "ELECTRONICS", 15),
                new ProductEntity("Ergonomic Mouse", new BigDecimal("34.50"), "ELECTRONICS", 100),
                new ProductEntity("Mechanical Keyboard", new BigDecimal("89.00"), "ELECTRONICS", 3),
                new ProductEntity("Standing Desk", new BigDecimal("599.00"), "FURNITURE", 8),
                new ProductEntity("Ceramic Mug", new BigDecimal("14.99"), "OTHER", 2),
                new ProductEntity("27-Inch Monitor", new BigDecimal("449.99"), "ELECTRONICS", 0),
                new ProductEntity("USB-C Cable 2m", new BigDecimal("9.99"), "ELECTRONICS", 250),
                new ProductEntity("Office Chair", new BigDecimal("389.00"), "FURNITURE", 12),
                new ProductEntity("Wireless Charger", new BigDecimal("29.99"), "ELECTRONICS", 45),
                new ProductEntity("Notebook Pack", new BigDecimal("19.99"), "OTHER", 4)
        );

        repository.saveAll(products);
        log.info("Seeded {} products into the database", products.size());
    }
}
