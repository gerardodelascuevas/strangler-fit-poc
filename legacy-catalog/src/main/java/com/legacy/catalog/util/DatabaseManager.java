package com.legacy.catalog.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static Connection connection = null;

    static {
        System.out.println("[DB] Initializing database...");
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:h2:mem:legacy;DB_CLOSE_DELAY=-1",
                    "sa", "sa");
            System.out.println("[DB] Connected to H2 in-memory database");
            createTable();
        } catch (Exception e) {
            System.out.println("[DB] CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTable() {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "price DECIMAL(10,2), " +
                    "category VARCHAR(100), " +
                    "stock INT)");
            System.out.println("[DB] Table 'products' ready");

            stmt.execute("MERGE INTO products (id, name, price, category, stock) KEY(id) VALUES " +
                    "(1, 'Laptop', 1200.00, 'ELECTRONICS', 15), " +
                    "(2, 'Mouse', 25.50, 'ELECTRONICS', 100), " +
                    "(3, 'Keyboard', 45.00, 'ELECTRONICS', 3), " +
                    "(4, 'Desk Chair', 350.00, 'FURNITURE', 8), " +
                    "(5, 'Coffee Mug', 12.99, 'OTHER', 2), " +
                    "(6, 'Monitor 4K', 899.99, 'ELECTRONICS', 0), " +
                    "(7, 'USB Cable', 8.99, 'ELECTRONICS', 250)");
            System.out.println("[DB] Seed data inserted");
            stmt.close();
        } catch (Exception e) {
            System.out.println("[DB] Error creating table: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Reconnecting...");
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:h2:mem:legacy;DB_CLOSE_DELAY=-1",
                        "sa", "sa");
            }
            return connection;
        } catch (Exception e) {
            System.out.println("[DB] Connection error: " + e.getMessage());
            return null;
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed");
            }
        } catch (Exception e) {
            System.out.println("[DB] Error closing: " + e.getMessage());
        }
    }
}
