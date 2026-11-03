package com.frescuratotal.erp.inventory.model;

public class ProductCategory {
    private int id;
    private String name;
    private String description;

    public ProductCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ProductCategory() {}

    // GETTER

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // TOSTRING


    @Override
    public String toString() {
        return "ProductCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
