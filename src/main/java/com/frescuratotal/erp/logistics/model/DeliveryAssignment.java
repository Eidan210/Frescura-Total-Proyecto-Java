package com.frescuratotal.erp.logistics.model;

import java.time.LocalDateTime;

public class DeliveryAssignment {
    private int deliveryAssignmentId;
    private DeliveryRoute route;
    private Vehicle vehicle;
    private Driver driver;
    private LocalDateTime assignmentDate;
    private String assignedBy;
    private String notes;


    public DeliveryAssignment(int deliveryAssignmentId, DeliveryRoute route, Vehicle vehicle, Driver driver, LocalDateTime assignmentDate, String assignedBy, String notes) {
        this.deliveryAssignmentId = deliveryAssignmentId;
        this.route = route;
        this.vehicle = vehicle;
        this.driver = driver;
        this.assignmentDate = assignmentDate;
        this.assignedBy = assignedBy;
        this.notes = notes;
    }

    public DeliveryAssignment() {
    }

    public int getDeliveryAssignmentId() {
        return deliveryAssignmentId;
    }

    public void setDeliveryAssignmentId(int deliveryAssignmentId) {
        this.deliveryAssignmentId = deliveryAssignmentId;
    }

    public DeliveryRoute getRoute() {
        return route;
    }

    public void setRoute(DeliveryRoute route) {
        this.route = route;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public LocalDateTime getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDateTime assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "DeliveryAssignment{" +
                "deliveryAssignmentId=" + deliveryAssignmentId +
                ", route=" + route +
                ", vehicle=" + vehicle +
                ", driver=" + driver +
                ", assignmentDate=" + assignmentDate +
                ", assignedBy='" + assignedBy + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
