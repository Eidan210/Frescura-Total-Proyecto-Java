package com.frescuratotal.erp.logistics.model;

import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

public class Vehicle {
    private int vehicleId;
    private String licensePlate;
    private String model;
    private int capacity;
    private VehicleStatus status = VehicleStatus.ACTIVE;

    public Vehicle() {
    }

    public Vehicle(int vehicleId, String licensePlate, String model, int capacity, VehicleStatus status) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleId=" + vehicleId +
                ", licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}
