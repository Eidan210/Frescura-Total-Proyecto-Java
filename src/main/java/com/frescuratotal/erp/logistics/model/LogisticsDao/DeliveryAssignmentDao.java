package com.frescuratotal.erp.logistics.model.LogisticsDao;

import com.frescuratotal.erp.logistics.model.DeliveryAssignment;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentDao {
    void saveAssignment(DeliveryAssignment assignment);
    Optional<DeliveryAssignment> findAssignmentById(int assignmentId);
    List<DeliveryAssignment> findAllAssignments();
    List<DeliveryAssignment> findAssignmentsByRoute(int routeId);
    List<DeliveryAssignment> findAssignmentsByDriver(int driverId);
    List<DeliveryAssignment> findAssignmentsByVehicle(int vehicleId);
    void updateAssignment(DeliveryAssignment assignment);
    void deleteAssignment(int assignmentId);
}
