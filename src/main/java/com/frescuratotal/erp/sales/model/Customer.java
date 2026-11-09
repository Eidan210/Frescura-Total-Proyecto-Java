package com.frescuratotal.erp.sales.model;

import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String address;
    private CustomerType type;
    private LocalDateTime registrationDate;

    public Customer() {
    }

    /** Copy constructor: the customer cache hands out copies so nobody can change what it stores. */
    public Customer(Customer other) {
        this.id = other.id;
        this.name = other.name;
        this.document = other.document;
        this.email = other.email;
        this.phone = other.phone;
        this.address = other.address;
        this.type = other.type;
        this.registrationDate = other.registrationDate;
    }

    public Customer(String name, String document, String email, String phone, String address, CustomerType type ) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CustomerType getType() {
        return type;
    }

    public void setType(CustomerType type) {
        this.type = type;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", document='" + document + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", type=" + type +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
