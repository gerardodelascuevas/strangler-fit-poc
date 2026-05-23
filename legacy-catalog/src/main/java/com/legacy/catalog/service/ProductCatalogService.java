package com.legacy.catalog.service;

import com.legacy.catalog.dao.ProductDatabase;
import com.legacy.catalog.model.Product;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductCatalogService {

    private static int callCounter = 0;
    public static String SERVICE_VERSION = "1.0.LEGACY";

    public Product[] getAllProducts() {
        callCounter++;
        System.out.println("========================================");
        System.out.println("[SERVICE] getAllProducts() CALL #" + callCounter);
        System.out.println("[SERVICE] Timestamp: " + getCurrentTimestamp());
        System.out.println("========================================");

        Product[] products = ProductDatabase.getAllProducts();

        System.out.println("[SERVICE] Returning " + products.length + " products");
        return products;
    }

    public Product getProduct(String id) {
        callCounter++;
        System.out.println("[SERVICE] getProduct(id=" + id + ") CALL #" + callCounter);

        if (id == null || id.trim().isEmpty()) {
            System.out.println("[SERVICE] ERROR: Invalid id!");
            return null;
        }

        Product p = ProductDatabase.getProductById(id);

        if (p == null) {
            System.out.println("[SERVICE] Product " + id + " not found!");
            Product notFound = new Product();
            notFound.id = "-1";
            notFound.name = "NOT FOUND";
            notFound.price = "0";
            notFound.category = "N/A";
            notFound.stock = "0";
            return notFound;
        }

        return p;
    }

    public String createProduct(String name, String price, String category, String stock) {
        callCounter++;
        System.out.println("[SERVICE] createProduct() CALL #" + callCounter);
        System.out.println("[SERVICE] Params: " + name + ", " + price + ", " + category + ", " + stock);
        logToFile("createProduct", name);

        if (name == null || name.trim().isEmpty()) {
            return "ERROR: Product name cannot be empty";
        }

        double priceValue = 0;
        try {
            priceValue = Double.parseDouble(price);
        } catch (Exception e) {
            return "ERROR: Invalid price: " + price;
        }

        if (priceValue < 0) {
            return "ERROR: Price cannot be negative";
        }

        if (priceValue > 1000) {
            System.out.println("[SERVICE] Applying VIP discount for product over $1000");
            priceValue = priceValue * 0.90;
            name = "[DISC] " + name;
        }

        int stockValue = 0;
        try {
            stockValue = Integer.parseInt(stock);
        } catch (Exception e) {
            return "ERROR: Invalid stock: " + stock;
        }

        String normalizedCategory = normalizeCategory(category);

        String result = ProductDatabase.createProduct(name, String.valueOf(priceValue), normalizedCategory, stock);
        System.out.println("[SERVICE] Create result: " + result);
        return result;
    }

    public String updateProduct(String id, String name, String price, String category, String stock) {
        callCounter++;
        System.out.println("[SERVICE] updateProduct() CALL #" + callCounter);
        System.out.println("[SERVICE] Params: id=" + id + ", name=" + name);

        if (id == null || id.trim().isEmpty()) {
            return "ERROR: Product id is required";
        }

        String normalizedCategory = normalizeCategory(category);

        String result = ProductDatabase.updateProduct(id, name, price, normalizedCategory, stock);
        System.out.println("[SERVICE] Update result: " + result);
        return result;
    }

    public String deleteProduct(String id) {
        callCounter++;
        System.out.println("[SERVICE] deleteProduct(id=" + id + ") CALL #" + callCounter);

        if (id == null || id.trim().isEmpty()) {
            return "ERROR: Product id is required";
        }

        Product existing = ProductDatabase.getProductById(id);
        if (existing == null) {
            return "ERROR: Product " + id + " does not exist";
        }

        String result = ProductDatabase.deleteProduct(id);
        System.out.println("[SERVICE] Delete result: " + result);
        return result;
    }

    public Product[] searchProducts(String keyword) {
        callCounter++;
        System.out.println("[SERVICE] searchProducts(keyword=" + keyword + ") CALL #" + callCounter);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        Product[] byName = ProductDatabase.searchProductsByName(keyword);
        Product[] byCategory = ProductDatabase.getProductsByCategory(keyword);

        java.util.Set<String> ids = new java.util.HashSet<>();
        java.util.List<Product> merged = new java.util.ArrayList<>();

        for (Product p : byName) {
            if (ids.add(p.id)) merged.add(p);
        }
        for (Product p : byCategory) {
            if (ids.add(p.id)) merged.add(p);
        }

        System.out.println("[SERVICE] Search found " + merged.size() + " results for '" + keyword + "'");
        return merged.toArray(new Product[0]);
    }

    public String getInventoryReport() {
        callCounter++;
        System.out.println("[SERVICE] getInventoryReport() CALL #" + callCounter);

        Product[] all = ProductDatabase.getAllProducts();

        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("INVENTORY REPORT - ").append(getCurrentTimestamp()).append("\n");
        report.append("========================================\n\n");

        int totalProducts = 0;
        int lowStock = 0;
        int outOfStock = 0;

        for (Product p : all) {
            totalProducts++;
            int st = Integer.parseInt(p.stock);
            if (st == 0) outOfStock++;
            else if (st < 10) lowStock++;

            report.append("- ").append(p.name)
                    .append(" | ").append(formatPrice(p.price))
                    .append(" | Stock: ").append(p.stock);
            if (st == 0) report.append(" [OUT OF STOCK!]");
            else if (st < 10) report.append(" [LOW STOCK!]");
            report.append("\n");
        }

        report.append("\n--- SUMMARY ---\n");
        report.append("Total Products: ").append(totalProducts).append("\n");
        report.append("Low Stock (<10): ").append(lowStock).append("\n");
        report.append("Out of Stock: ").append(outOfStock).append("\n");
        report.append("================================");

        return report.toString();
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "OTHER";
        }

        String cat = category.toUpperCase();

        if (cat.contains("ELECTRONIC") || cat.contains("ELECTRO") || cat.equals("TECH")) {
            return "ELECTRONICS";
        }
        if (cat.contains("FOOD") || cat.contains("ALIMENT") || cat.contains("COMIDA")) {
            return "FOOD";
        }
        if (cat.contains("FURNITURE") || cat.contains("MUEBLE") || cat.contains("CHAIR") || cat.contains("DESK")) {
            return "FURNITURE";
        }
        if (cat.contains("CLOTH") || cat.contains("ROPA") || cat.contains("FASHION")) {
            return "CLOTHING";
        }

        return "OTHER";
    }

    public static String formatPrice(String price) {
        try {
            double p = Double.parseDouble(price);
            return "$" + String.format("%.2f", p);
        } catch (Exception e) {
            return "$0.00";
        }
    }

    public static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    private void logToFile(String operation, String detail) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " | " + operation + " | " + detail);
    }

    public static int getCallCount() {
        return callCounter;
    }

    public static void resetCallCount() {
        callCounter = 0;
    }
}
