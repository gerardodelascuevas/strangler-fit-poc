package com.legacy.catalog.model;

public class Product {

    public String id;
    public String name;
    public String price;
    public String category;
    public String stock;

    public Product() {
    }

    public Product(String id, String name, String price, String category, String stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }
}
