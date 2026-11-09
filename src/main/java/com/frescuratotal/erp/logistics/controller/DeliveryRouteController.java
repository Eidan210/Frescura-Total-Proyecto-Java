package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.logistics.Dao.DeliveryAssignmentDao;
import com.frescuratotal.erp.logistics.Dao.DeliveryRouteDao;
import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de rutas de entrega.
 *
 * Además del CRUD, maneja el ciclo de vida de una ruta:
 *   Planificada -> En Tránsito -> Completada
 *   Planificada / En Tránsito -> Cancelada
 */
public class DeliveryRouteController {

    private final DeliveryRouteDao routeDao;
    private final DeliveryAssignmentDao assignmentDao;

    public DeliveryRouteController(DeliveryRouteDao routeDao, DeliveryAssignmentDao assignmentDao) {
        this.routeDao = routeDao;
        this.assignmentDao = assignmentDao;
    }

    // ===================== CREAR =====================

    public DeliveryRoute createRoute(String routeName, String createdBy,
                                     LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival) throws LogisticsException {
        validateName(routeName);
        if (isBlank(createdBy)) {
            throw new LogisticsException("Debe indicar el usuario que crea la ruta");
        }
        validateEstimatedTimes(estimatedDeparture, estimatedArrival);

        DeliveryRoute route = new DeliveryRoute();
        route.setRouteName(routeName.trim());
        route.setCreatedBy(createdBy.trim());
        route.setCreatedAt(LocalDateTime.now());
        route.setEstimatedDepartureTime(estimatedDeparture);
        route.setEstimatedArrivalTime(estimatedArrival);
        route.setStatus(DeliveryRouteStatus.PLANNED);

        try {
            routeDao.save(route);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("crear la ruta", e);
        }
        return route;
    }

    // ===================== CONSULTAR =====================

    public DeliveryRoute findRoute(int routeId) throws LogisticsException {
        if (routeId <= 0) {
            throw new LogisticsException("El id debe ser un número mayor que cero");
        }

        Optional<DeliveryRoute> result;
        try {
            result = routeDao.findById(routeId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("buscar la ruta", e);
        }

        if (!result.isPresent()) {
            throw new LogisticsException("No existe una ruta con id " + routeId);
        }
        return result.get();
    }

    public List<DeliveryRoute> listRoutes() throws LogisticsException {
        try {
            return routeDao.findAll();
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar las rutas", e);
        }
    }

    public List<DeliveryRoute> listRoutesByStatus(DeliveryRouteStatus status) throws LogisticsException {
        if (status == null) {
            throw new LogisticsException("Debe seleccionar un estado");
        }
        try {
            return routeDao.findByStatus(status);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("listar las rutas por estado", e);
        }
    }

    // ===================== ACTUALIZAR =====================

    /** Solo se pueden editar los datos de una ruta que todavía está planificada. */
    public DeliveryRoute updateRoute(int routeId, String routeName,
                                     LocalDateTime estimatedDeparture, LocalDateTime estimatedArrival) throws LogisticsException {
        validateName(routeName);
        validateEstimatedTimes(estimatedDeparture, estimatedArrival);

        DeliveryRoute route = findRoute(routeId);
        checkStatus(route, DeliveryRouteStatus.PLANNED, "editar");

        route.setRouteName(routeName.trim());
        route.setEstimatedDepartureTime(estimatedDeparture);
        route.setEstimatedArrivalTime(estimatedArrival);
        saveChanges(route);
        return route;
    }

    // ===================== CAMBIOS DE ESTADO =====================

    /** Planificada -> En Tránsito. Necesita tener vehículo y conductor asignados. */
    public DeliveryRoute startRoute(int routeId) throws LogisticsException {
        DeliveryRoute route = findRoute(routeId);
        checkStatus(route, DeliveryRouteStatus.PLANNED, "iniciar");

        if (!hasAssignment(routeId)) {
            throw new LogisticsException("No se puede iniciar la ruta: no tiene vehículo ni conductor asignados");
        }

        route.setStatus(DeliveryRouteStatus.IN_TRANSIT);
        route.setActualDepartureTime(LocalDateTime.now());
        saveChanges(route);
        return route;
    }

    /** En Tránsito -> Completada. */
    public DeliveryRoute completeRoute(int routeId) throws LogisticsException {
        DeliveryRoute route = findRoute(routeId);
        checkStatus(route, DeliveryRouteStatus.IN_TRANSIT, "completar");

        route.setStatus(DeliveryRouteStatus.COMPLETED);
        route.setActualArrivalTime(LocalDateTime.now());
        saveChanges(route);
        return route;
    }

    /** Planificada o En Tránsito -> Cancelada. */
    public DeliveryRoute cancelRoute(int routeId) throws LogisticsException {
        DeliveryRoute route = findRoute(routeId);
        DeliveryRouteStatus current = route.getStatus();

        if (current == DeliveryRouteStatus.COMPLETED) {
            throw new LogisticsException("No se puede cancelar una ruta que ya fue completada");
        }
        if (current == DeliveryRouteStatus.CANCELLED) {
            throw new LogisticsException("La ruta ya está cancelada");
        }

        route.setStatus(DeliveryRouteStatus.CANCELLED);
        saveChanges(route);
        return route;
    }

    // ===================== ELIMINAR =====================

    /** Solo se eliminan rutas planificadas o canceladas y sin asignaciones. */
    public void deleteRoute(int routeId) throws LogisticsException {
        DeliveryRoute route = findRoute(routeId);
        DeliveryRouteStatus current = route.getStatus();

        if (current == DeliveryRouteStatus.IN_TRANSIT || current == DeliveryRouteStatus.COMPLETED) {
            throw new LogisticsException("No se puede eliminar una ruta en estado " + current.getDbValue());
        }
        if (hasAssignment(routeId)) {
            throw new LogisticsException("Primero elimine la asignación de esta ruta");
        }

        try {
            routeDao.delete(routeId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("eliminar la ruta", e);
        }
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private void saveChanges(DeliveryRoute route) throws LogisticsException {
        try {
            routeDao.update(route);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("actualizar la ruta", e);
        }
    }

    private boolean hasAssignment(int routeId) throws LogisticsException {
        List<DeliveryAssignment> assignments;
        try {
            assignments = assignmentDao.findAssignmentsByRoute(routeId);
        } catch (RuntimeException e) {
            throw LogisticsException.fromDatabase("consultar las asignaciones de la ruta", e);
        }
        return !assignments.isEmpty();
    }

    /** Lanza una excepción si la ruta no está en el estado esperado. */
    private void checkStatus(DeliveryRoute route, DeliveryRouteStatus expected, String action) throws LogisticsException {
        if (route.getStatus() != expected) {
            throw new LogisticsException("Solo se puede " + action + " una ruta en estado "
                    + expected.getDbValue() + ". Estado actual: " + route.getStatus().getDbValue());
        }
    }

    private void validateName(String routeName) throws LogisticsException {
        if (isBlank(routeName)) {
            throw new LogisticsException("El nombre de la ruta es obligatorio");
        }
    }

    private void validateEstimatedTimes(LocalDateTime departure, LocalDateTime arrival) throws LogisticsException {
        if (departure == null) {
            throw new LogisticsException("La hora de salida estimada es obligatoria");
        }
        if (arrival == null) {
            throw new LogisticsException("La hora de llegada estimada es obligatoria");
        }
        if (!arrival.isAfter(departure)) {
            throw new LogisticsException("La llegada estimada debe ser después de la salida estimada");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
