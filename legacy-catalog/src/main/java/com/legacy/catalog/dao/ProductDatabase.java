package com.legacy.catalog.dao;

import com.legacy.catalog.model.Product;
import com.legacy.catalog.util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDatabase {

    public static Product[] getAllProducts() {
        System.out.println("[DAO] getAllProducts");
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return new Product[0];

        Statement stmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM products ORDER BY id");

            while (rs.next()) {
                Product p = new Product();
                p.id = String.valueOf(rs.getInt("id"));
                p.name = rs.getString("name");
                p.price = String.valueOf(rs.getBigDecimal("price"));
                p.category = rs.getString("category");
                p.stock = String.valueOf(rs.getInt("stock"));
                products.add(p);
            }
        } catch (Exception e) {
            System.out.println("[DAO] Error fetching all products: " + e.getMessage());
            e.printStackTrace();
        }

        return products.toArray(new Product[0]);
    }

    public static Product getProductById(String id) {
        System.out.println("[DAO] getProductById: " + id);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return null;

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM products WHERE id = " + id);
            if (rs.next()) {
                Product p = new Product();
                p.id = String.valueOf(rs.getInt("id"));
                p.name = rs.getString("name");
                p.price = String.valueOf(rs.getBigDecimal("price"));
                p.category = rs.getString("category");
                p.stock = String.valueOf(rs.getInt("stock"));
                return p;
            }
        } catch (Exception e) {
            System.out.println("[DAO] Error fetching product " + id + ": " + e.getMessage());
        }

        return null;
    }

    public static Product[] getProductsByCategory(String category) {
        System.out.println("[DAO] getProductsByCategory: " + category);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return new Product[0];

        Statement stmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM products WHERE category = '" + category + "' ORDER BY name");

            while (rs.next()) {
                Product p = new Product();
                p.id = String.valueOf(rs.getInt("id"));
                p.name = rs.getString("name");
                p.price = String.valueOf(rs.getBigDecimal("price"));
                p.category = rs.getString("category");
                p.stock = String.valueOf(rs.getInt("stock"));
                products.add(p);
            }
        } catch (Exception e) {
            System.out.println("[DAO] Error: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        }

        return products.toArray(new Product[0]);
    }

    public static Product[] searchProductsByName(String name) {
        System.out.println("[DAO] searchProductsByName: " + name);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return new Product[0];

        Statement stmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM products WHERE name LIKE '%" + name + "%' ORDER BY name");

            while (rs.next()) {
                Product p = new Product();
                p.id = String.valueOf(rs.getInt("id"));
                p.name = rs.getString("name");
                p.price = String.valueOf(rs.getBigDecimal("price"));
                p.category = rs.getString("category");
                p.stock = String.valueOf(rs.getInt("stock"));
                products.add(p);
            }
        } catch (Exception e) {
            System.out.println("[DAO] Error: " + e.getMessage());
        }

        return products.toArray(new Product[0]);
    }

    public static String createProduct(String name, String price, String category, String stock) {
        System.out.println("[DAO] createProduct: " + name);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return "ERROR:No database connection";

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            stmt.execute("INSERT INTO products (name, price, category, stock) VALUES ('"
                    + name + "', " + price + ", '" + category + "', " + stock + ")",
                    Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return "OK:" + keys.getInt(1);
            }
            return "OK:" + name;
        } catch (Exception e) {
            System.out.println("[DAO] Error creating product: " + e.getMessage());
            return "ERROR:" + e.getMessage();
        }
    }

    public static String updateProduct(String id, String name, String price, String category, String stock) {
        System.out.println("[DAO] updateProduct: " + id);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return "ERROR:No database connection";

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            int affected = stmt.executeUpdate("UPDATE products SET name='" + name
                    + "', price=" + price
                    + ", category='" + category
                    + "', stock=" + stock
                    + " WHERE id=" + id);
            if (affected > 0) {
                return "OK:" + id;
            }
            return "ERROR:Product not found";
        } catch (Exception e) {
            System.out.println("[DAO] Error updating product: " + e.getMessage());
            return "ERROR:" + e.getMessage();
        }
    }

    public static String deleteProduct(String id) {
        System.out.println("[DAO] deleteProduct: " + id);
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return "ERROR:No database connection";

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            int affected = stmt.executeUpdate("DELETE FROM products WHERE id=" + id);
            if (affected > 0) {
                return "OK:" + id;
            }
            return "ERROR:Product not found";
        } catch (Exception e) {
            System.out.println("[DAO] Error deleting product: " + e.getMessage());
            return "ERROR:" + e.getMessage();
        }
    }
}
