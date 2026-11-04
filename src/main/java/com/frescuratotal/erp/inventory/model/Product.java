package com.frescuratotal.erp.inventory.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String description;
    private int categoryId;
    private String unitOfMeasure;
    private BigDecimal basePrice;

    public Product(String name, String description, int categoryId, String unitOfMeasure, BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.unitOfMeasure = unitOfMeasure;
        this.basePrice = basePrice;
    }

    public Product() {}

    // getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    // setters


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", basePrice=" + basePrice +
                '}';
    }
}
