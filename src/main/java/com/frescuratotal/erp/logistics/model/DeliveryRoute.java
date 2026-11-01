package com.frescuratotal.erp.logistics.model;

import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;

import java.time.LocalDateTime;

public class DeliveryRoute {
    private int routeId;
    private String routeName;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime estimatedDepartureTime;
    private LocalDateTime estimatedArrivalTime;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualArrivalTime;
    private DeliveryRouteStatus status = DeliveryRouteStatus.PLANNED;

    public DeliveryRoute() {
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getEstimatedDepartureTime() {
        return estimatedDepartureTime;
    }

    public void setEstimatedDepartureTime(LocalDateTime estimatedDepartureTime) {
        this.estimatedDepartureTime = estimatedDepartureTime;
    }

    public LocalDateTime getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public void setEstimatedArrivalTime(LocalDateTime estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }

    public LocalDateTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(LocalDateTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public LocalDateTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(LocalDateTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }


    public DeliveryRouteStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryRouteStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeliveryRoute{" +
                "routeId=" + routeId +
                ", routeName='" + routeName + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", estimatedDepartureTime=" + estimatedDepartureTime +
                ", estimatedArrivalTime=" + estimatedArrivalTime +
                ", actualDepartureTime=" + actualDepartureTime +
                ", actualArrivalTime=" + actualArrivalTime +
                ", status=" + status +
                '}';
    }
}
