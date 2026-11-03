package com.frescuratotal.erp.logistics.model;

import com.frescuratotal.erp.sales.model.Sale;

import java.time.LocalDateTime;

public class RouteSale {
    private int routeSaleId;
    private DeliveryRoute route;
    private Sale sale;
    private int deliveryOrder;
    private String destinationAddress;
    private LocalDateTime deliveryDate;
    private String receivedBy;
    private String notes;

    public RouteSale() {
    }

    public RouteSale(int routeSaleId, DeliveryRoute route, Sale sale, int deliveryOrder, String destinationAddress, LocalDateTime deliveryDate, String receivedBy, String notes) {
        this.routeSaleId = routeSaleId;
        this.route = route;
        this.sale = sale;
        this.deliveryOrder = deliveryOrder;
        this.destinationAddress = destinationAddress;
        this.deliveryDate = deliveryDate;
        this.receivedBy = receivedBy;
        this.notes = notes;
    }

    public int getRouteSaleId() {
        return routeSaleId;
    }

    public void setRouteSaleId(int routeSaleId) {
        this.routeSaleId = routeSaleId;
    }

    public DeliveryRoute getRoute() {
        return route;
    }

    public void setRoute(DeliveryRoute route) {
        this.route = route;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public int getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(int deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
