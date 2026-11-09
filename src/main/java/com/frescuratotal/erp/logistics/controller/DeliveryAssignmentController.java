package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.logistics.Dao.DeliveryAssignmentDao;
import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de asignaciones (ruta + vehículo + conductor).
 *
 * Usa los otros controladores para buscar la ruta, el vehículo y el conductor.
 * Así no repetimos las validaciones que ya están hechas allá.
 *
 * Reglas:
 *  - La ruta debe estar Planificada y no tener otra asignación.
 *  - El vehículo debe estar Activo y no estar ocupado en otra ruta pendiente.
 *  - El conductor debe estar activo y no estar ocupado en otra ruta pendiente.
 */
public class DeliveryAssignmentController {

    private final DeliveryAssignmentDao assignmentDao;
    private final DeliveryRouteController routeController;
    private final VehicleController vehicleController;
    private final DriverController driverController;

    public DeliveryAssignmentController(DeliveryAssignmentDao assignmentDao,
                                        DeliveryRouteController routeController,
                                        VehicleController vehicleController,
                                        DriverController driverController) {
        this.assignmentDao = assignmentDao;
        this.routeController = routeController;
        this.vehicleController = vehicleController;
        this.driverController = driverController;
    }

    // ===================== CREAR =====================

    public DeliveryAssignment assign(int routeId, int vehicleId, int driverId,
                                     String assignedBy, String notes) throws LogisticsException {
        if (isBlank(assignedBy)) {
            throw new LogisticsException("Debe indicar el usuario que hace la asignación");
        }

        DeliveryRoute route = routeController.findRoute(routeId);
        if (route.getStatus() != DeliveryRouteStatus.PLANNED) {
            throw new LogisticsException("Solo se pueden asignar rutas en estado Planificada");
        }
        if (!findByRoute(routeId).isEmpty()) {
            throw new LogisticsException("La ruta " + routeId + " ya tiene una asignación");
        }

        Vehicle vehicle = validateVehicle(vehicleId, 0);
        Driver driver = validateDriver(driverId, 0);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setRoute(route);
        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setAssignmentDate(LocalDateTime.now());
        assignment.setAssignedBy(assignedBy.trim());
        assignment.setNotes(cleanOptional(notes));

        try {
            assignmentDao.saveAssignment(assignment);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("guardar la asignación", e);
        }
        return assignment;
    }

    // ===================== CONSULTAR =====================

    public DeliveryAssignment findAssignment(int assignmentId) throws LogisticsException {
        if (assignmentId <= 0) {
            throw new LogisticsException("El id debe ser un número mayor que cero");
        }

        Optional<DeliveryAssignment> result;
        try {
            result = assignmentDao.findAssignmentById(assignmentId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar la asignación", e);
        }

        if (!result.isPresent()) {
            throw new LogisticsException("No existe una asignación con id " + assignmentId);
        }
        DeliveryAssignment assignment = result.get();
        loadDetails(assignment);
        return assignment;
    }

    public List<DeliveryAssignment> listAssignments() throws LogisticsException {
        List<DeliveryAssignment> assignments;
        try {
            assignments = assignmentDao.findAllAssignments();
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar las asignaciones", e);
        }
        loadDetails(assignments);
        return assignments;
    }

    public List<DeliveryAssignment> listAssignmentsByRoute(int routeId) throws LogisticsException {
        routeController.findRoute(routeId); // valida que exista
        List<DeliveryAssignment> assignments = findByRoute(routeId);
        loadDetails(assignments);
        return assignments;
    }

    public List<DeliveryAssignment> listAssignmentsByVehicle(int vehicleId) throws LogisticsException {
        vehicleController.findVehicle(vehicleId);
        List<DeliveryAssignment> assignments = findByVehicle(vehicleId);
        loadDetails(assignments);
        return assignments;
    }

    public List<DeliveryAssignment> listAssignmentsByDriver(int driverId) throws LogisticsException {
        driverController.findDriver(driverId);
        List<DeliveryAssignment> assignments = findByDriver(driverId);
        loadDetails(assignments);
        return assignments;
    }

    // ===================== ACTUALIZAR =====================

    /** Cambia el vehículo, el conductor o las notas. Solo si la ruta sigue planificada. */
    public DeliveryAssignment updateAssignment(int assignmentId, int vehicleId, int driverId, String notes) throws LogisticsException {
        DeliveryAssignment assignment = findAssignment(assignmentId);
        checkRouteIsPlanned(assignment, "modificar");

        Vehicle vehicle = validateVehicle(vehicleId, assignmentId);
        Driver driver = validateDriver(driverId, assignmentId);

        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setNotes(cleanOptional(notes));

        try {
            assignmentDao.updateAssignment(assignment);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("actualizar la asignación", e);
        }
        return assignment;
    }

    // ===================== ELIMINAR =====================

    public void deleteAssignment(int assignmentId) throws LogisticsException {
        DeliveryAssignment assignment = findAssignment(assignmentId);

        DeliveryRouteStatus status = assignment.getRoute().getStatus();
        if (status == DeliveryRouteStatus.IN_TRANSIT || status == DeliveryRouteStatus.COMPLETED) {
            throw new LogisticsException("No se puede eliminar la asignación de una ruta en estado " + status.getDbValue());
        }

        try {
            assignmentDao.deleteAssignment(assignmentId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("eliminar la asignación", e);
        }
    }

    // ===================== VALIDACIONES =====================

    /**
     * Revisa que el vehículo exista, esté activo y no esté ocupado.
     * @param ignoreAssignmentId asignación que se está editando (0 si es nueva)
     */
    private Vehicle validateVehicle(int vehicleId, int ignoreAssignmentId) throws LogisticsException {
        Vehicle vehicle = vehicleController.findVehicle(vehicleId);
        if (vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new LogisticsException("El vehículo " + vehicle.getLicensePlate()
                    + " no está disponible (estado: " + vehicle.getStatus().getDbValue() + ")");
        }
        if (isBusy(findByVehicle(vehicleId), ignoreAssignmentId)) {
            throw new LogisticsException("El vehículo " + vehicle.getLicensePlate() + " ya está asignado a otra ruta pendiente");
        }
        return vehicle;
    }

    private Driver validateDriver(int driverId, int ignoreAssignmentId) throws LogisticsException {
        Driver driver = driverController.findDriver(driverId);
        if (!driver.isActive()) {
            throw new LogisticsException("El conductor " + driver.getName() + " está inactivo");
        }
        if (isBusy(findByDriver(driverId), ignoreAssignmentId)) {
            throw new LogisticsException("El conductor " + driver.getName() + " ya está asignado a otra ruta pendiente");
        }
        return driver;
    }

    /**
     * Un vehículo/conductor está ocupado si tiene alguna asignación cuya ruta
     * esté Planificada o En Tránsito.
     */
    private boolean isBusy(List<DeliveryAssignment> assignments, int ignoreAssignmentId) throws LogisticsException {
        for (DeliveryAssignment a : assignments) {
            if (a.getDeliveryAssignmentId() == ignoreAssignmentId) {
                continue;
            }
            DeliveryRoute route = routeController.findRoute(a.getRoute().getRouteId());
            if (route.getStatus() == DeliveryRouteStatus.PLANNED || route.getStatus() == DeliveryRouteStatus.IN_TRANSIT) {
                return true;
            }
        }
        return false;
    }

    private void checkRouteIsPlanned(DeliveryAssignment assignment, String action) throws LogisticsException {
        if (assignment.getRoute().getStatus() != DeliveryRouteStatus.PLANNED) {
            throw new LogisticsException("Solo se puede " + action + " la asignación si la ruta está Planificada");
        }
    }

    // ===================== MÉTODOS PRIVADOS =====================

    /**
     * El DAO solo trae los ids de ruta, vehículo y conductor.
     * Aquí cargamos los objetos completos para que la vista pueda mostrar nombres y placas.
     */
    private void loadDetails(DeliveryAssignment assignment) throws LogisticsException {
        assignment.setRoute(routeController.findRoute(assignment.getRoute().getRouteId()));
        assignment.setVehicle(vehicleController.findVehicle(assignment.getVehicle().getVehicleId()));
        assignment.setDriver(driverController.findDriver(assignment.getDriver().getDriverId()));
    }

    private void loadDetails(List<DeliveryAssignment> assignments) throws LogisticsException {
        for (DeliveryAssignment a : assignments) {
            loadDetails(a);
        }
    }

    private List<DeliveryAssignment> findByRoute(int routeId) throws LogisticsException {
        try {
            return assignmentDao.findAssignmentsByRoute(routeId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar asignaciones de la ruta", e);
        }
    }

    private List<DeliveryAssignment> findByVehicle(int vehicleId) throws LogisticsException {
        try {
            return assignmentDao.findAssignmentsByVehicle(vehicleId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar asignaciones del vehículo", e);
        }
    }

    private List<DeliveryAssignment> findByDriver(int driverId) throws LogisticsException {
        try {
            return assignmentDao.findAssignmentsByDriver(driverId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar asignaciones del conductor", e);
        }
    }

    private String cleanOptional(String text) {
        if (isBlank(text)) {
            return null;
        }
        return text.trim();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
