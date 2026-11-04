package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.logistics.Dao.DeliveryAssignmentDao;
import com.frescuratotal.erp.logistics.Dao.DeliveryRouteDao;
import com.frescuratotal.erp.logistics.Dao.DriverDao;
import com.frescuratotal.erp.logistics.Dao.RouteSaleDao;
import com.frescuratotal.erp.logistics.Dao.VehicleDao;
import com.frescuratotal.erp.logistics.model.DeliveryAssignment;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.RouteSale;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;
import com.frescuratotal.erp.logistics.model.status.VehicleStatus;
import com.frescuratotal.erp.logistics.view.LogisticsView;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleDao;
import com.frescuratotal.erp.sales.model.SaleStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogisticsController {
    private final LogisticsView view;
    private final SaleDao saleDao;
    private final CustomerDao customerDao;
    private final DeliveryRouteDao routeDao;
    private final DeliveryAssignmentDao assignmentDao;
    private final RouteSaleDao routeSaleDao;
    private final VehicleDao vehicleDao;
    private final DriverDao driverDao;

    public LogisticsController(LogisticsView view, SaleDao saleDao, CustomerDao customerDao,
                               DeliveryRouteDao routeDao, DeliveryAssignmentDao assignmentDao,
                               RouteSaleDao routeSaleDao, VehicleDao vehicleDao, DriverDao driverDao) {
        this.view = view;
        this.saleDao = saleDao;
        this.customerDao = customerDao;
        this.routeDao = routeDao;
        this.assignmentDao = assignmentDao;
        this.routeSaleDao = routeSaleDao;
        this.vehicleDao = vehicleDao;
        this.driverDao = driverDao;
    }

    public void start() {
        int option = 0;
        do {
            // The DAOs wrap SQLException in RuntimeException, so a database failure arrives here.
            try {
                option = view.showMenu();
                switch (option) {
                    case 1 -> view.showSales(findSalesByStatus(SaleStatus.IN_PREPARATION));
                    case 2 -> createRoute();
                    case 3 -> listRoutes();
                    case 4 -> deliverSale();
                    case 5 -> registerVehicle();
                    case 6 -> registerDriver();
                    case 7 -> view.showVehicles(vehicleDao.findAllVehicles());
                    case 8 -> view.showDrivers(driverDao.findAllDrivers());
                    case 9 -> view.showMessage("Returning to main menu...");
                    default -> view.showMessage("Invalid option");
                }
            } catch (RuntimeException e) {
                view.showMessage("The operation could not be completed: " + e.getMessage());
            }
        } while (option != 9);
    }

    // ---------- Create route ----------

    private void createRoute() {
        String routeName = view.readRouteName();
        if (routeName.isBlank()) {
            view.showMessage("The route could not be created because the name is empty.");
            return;
        }

        Vehicle vehicle = selectVehicle();
        if (vehicle == null) {
            return;
        }

        Driver driver = selectDriver();
        if (driver == null) {
            return;
        }

        String createdBy = view.readCreatedBy();

        DeliveryRoute route = new DeliveryRoute();
        route.setRouteName(routeName);
        route.setCreatedBy(createdBy);

        List<RouteSale> routeSales = selectSales(route);
        if (routeSales.isEmpty()) {
            view.showMessage("The route was not created because it has no sales.");
            return;
        }

        // The sales leave when the route is created, so it starts in transit.
        route.setStatus(DeliveryRouteStatus.IN_TRANSIT);
        route.setActualDepartureTime(LocalDateTime.now());

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setRoute(route);
        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setAssignedBy(createdBy);

        // All DAOs share this connection, so everything below is saved or undone together.
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            routeDao.save(route);
            assignmentDao.saveAssignment(assignment);
            for (RouteSale routeSale : routeSales) {
                routeSaleDao.saveRouteSale(routeSale);
                saleDao.updateStatus(routeSale.getSale().getId(), SaleStatus.IN_TRANSIT);
            }

            conn.commit();
            view.showMessage("Route " + route.getRouteId() + " created with " + routeSales.size() + " sale(s).");
        } catch (SQLException | RuntimeException e) {
            rollback(conn);
            view.showMessage("The route could not be created, nothing was saved: " + e.getMessage());
        } finally {
            restoreAutoCommit(conn);
        }
    }

    /** Returns the chosen vehicle, or null if it cannot be used. */
    private Vehicle selectVehicle() {
        List<Vehicle> vehicles = vehicleDao.findByStatus(VehicleStatus.ACTIVE);
        if (vehicles.isEmpty()) {
            view.showMessage("The route could not be created because there are no active vehicles.");
            return null;
        }
        view.showVehicles(vehicles);

        Optional<Vehicle> box = vehicleDao.findVehicleById(view.readVehicleId());
        if (box.isEmpty()) {
            view.showMessage("The route could not be created because the vehicle does not exist.");
            return null;
        }
        if (box.get().getStatus() != VehicleStatus.ACTIVE) {
            view.showMessage("The route could not be created because the vehicle is not active.");
            return null;
        }
        return box.get();
    }

    /** Returns the chosen driver, or null if they cannot be used. */
    private Driver selectDriver() {
        List<Driver> drivers = driverDao.findActiveDrivers();
        if (drivers.isEmpty()) {
            view.showMessage("The route could not be created because there are no active drivers.");
            return null;
        }
        view.showDrivers(drivers);

        Optional<Driver> box = driverDao.findDriverById(view.readDriverId());
        if (box.isEmpty()) {
            view.showMessage("The route could not be created because the driver does not exist.");
            return null;
        }
        if (!box.get().isActive()) {
            view.showMessage("The route could not be created because the driver is not active.");
            return null;
        }
        return box.get();
    }

    /** Asks for sales until the user types 0. Rejected sales are reported and skipped. */
    private List<RouteSale> selectSales(DeliveryRoute route) {
        List<RouteSale> routeSales = new ArrayList<>();
        List<Sale> sales = saleDao.findAll();

        view.showSales(filterByStatus(sales, SaleStatus.IN_PREPARATION));

        int saleId = view.readSaleId();
        while (saleId != 0) {
            addSaleToRoute(saleId, sales, route, routeSales);
            saleId = view.readSaleId();
        }
        return routeSales;
    }

    private void addSaleToRoute(int saleId, List<Sale> sales, DeliveryRoute route, List<RouteSale> routeSales) {
        Sale sale = findSale(saleId, sales);
        if (sale == null) {
            view.showMessage("Sale " + saleId + " does not exist.");
            return;
        }
        if (isAlreadySelected(saleId, routeSales)) {
            view.showMessage("Sale " + saleId + " is already selected.");
            return;
        }
        if (sale.getSaleStatus() != SaleStatus.IN_PREPARATION) {
            view.showMessage("Sale " + saleId + " is not In Preparation (status: " + sale.getSaleStatus() + ").");
            return;
        }
        if (hasActiveRoute(saleId)) {
            view.showMessage("Sale " + saleId + " is already in an active route.");
            return;
        }

        // direccion_destino is NOT NULL in the database.
        String address = findCustomerAddress(sale);
        if (address == null || address.isBlank()) {
            view.showMessage("Sale " + saleId + " cannot be assigned because the customer has no address.");
            return;
        }

        RouteSale routeSale = new RouteSale();
        routeSale.setRoute(route);
        routeSale.setSale(sale);
        routeSale.setDeliveryOrder(routeSales.size() + 1);
        routeSale.setDestinationAddress(address);
        routeSales.add(routeSale);
        view.showMessage("Sale " + saleId + " added to the route.");
    }

    private boolean isAlreadySelected(int saleId, List<RouteSale> routeSales) {
        for (RouteSale routeSale : routeSales) {
            if (routeSale.getSale().getId() == saleId) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveRoute(int saleId) {
        for (RouteSale routeSale : routeSaleDao.findRouteSalesBySale(saleId)) {
            if (isRouteActive(routeSale.getRoute().getRouteId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRouteActive(int routeId) {
        Optional<DeliveryRoute> box = routeDao.findById(routeId);
        if (box.isEmpty()) {
            return false;
        }
        DeliveryRouteStatus status = box.get().getStatus();
        return status == DeliveryRouteStatus.PLANNED || status == DeliveryRouteStatus.IN_TRANSIT;
    }

    private String findCustomerAddress(Sale sale) {
        Optional<Customer> box = customerDao.findById(sale.getCustomer().getId());
        if (box.isEmpty()) {
            return null;
        }
        return box.get().getAddress();
    }

    // ---------- List routes ----------

    private void listRoutes() {
        DeliveryRouteStatus status = view.readRouteStatusFilter();

        List<DeliveryRoute> routes;
        if (status == null) {
            routes = routeDao.findAll();
        } else {
            routes = routeDao.findByStatus(status);
        }

        if (routes.isEmpty()) {
            view.showMessage("No routes found.");
            return;
        }

        for (DeliveryRoute route : routes) {
            DeliveryAssignment assignment = findFullAssignment(route.getRouteId());
            view.showRoute(route, assignment, routeSaleDao.findRouteSalesByRoute(route.getRouteId()));
        }
    }

    /** The assignment DAO only loads ids, so the vehicle and the driver are loaded here. */
    private DeliveryAssignment findFullAssignment(int routeId) {
        List<DeliveryAssignment> assignments = assignmentDao.findAssignmentsByRoute(routeId);
        if (assignments.isEmpty()) {
            return null;
        }

        DeliveryAssignment assignment = assignments.get(0);
        int vehicleId = assignment.getVehicle().getVehicleId();
        int driverId = assignment.getDriver().getDriverId();
        assignment.setVehicle(vehicleDao.findVehicleById(vehicleId).orElse(assignment.getVehicle()));
        assignment.setDriver(driverDao.findDriverById(driverId).orElse(assignment.getDriver()));
        return assignment;
    }

    // ---------- Deliver sale ----------

    private void deliverSale() {
        List<Sale> sales = saleDao.findAll();
        view.showSales(filterByStatus(sales, SaleStatus.IN_TRANSIT));

        int saleId = view.readSaleId();
        if (saleId == 0) {
            return;
        }

        Sale sale = findSale(saleId, sales);
        if (sale == null) {
            view.showMessage("Sale " + saleId + " does not exist.");
            return;
        }
        if (sale.getSaleStatus() != SaleStatus.IN_TRANSIT) {
            view.showMessage("Sale " + saleId + " is not In Transit (status: " + sale.getSaleStatus() + ").");
            return;
        }

        RouteSale routeSale = findPendingRouteSale(saleId);
        if (routeSale == null) {
            view.showMessage("Sale " + saleId + " is not in an active route.");
            return;
        }

        routeSale.setDeliveryDate(LocalDateTime.now());
        routeSale.setReceivedBy(view.readReceivedBy());

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            routeSaleDao.updateRouteSale(routeSale);
            saleDao.updateStatus(saleId, SaleStatus.DELIVERED);
            boolean routeCompleted = completeRouteIfFinished(routeSale.getRoute().getRouteId());

            conn.commit();
            view.showMessage("Sale " + saleId + " delivered.");
            if (routeCompleted) {
                view.showMessage("All the sales were delivered: route " + routeSale.getRoute().getRouteId() + " completed.");
            }
        } catch (SQLException | RuntimeException e) {
            rollback(conn);
            view.showMessage("The delivery could not be registered, nothing was saved: " + e.getMessage());
        } finally {
            restoreAutoCommit(conn);
        }
    }

    private RouteSale findPendingRouteSale(int saleId) {
        for (RouteSale routeSale : routeSaleDao.findRouteSalesBySale(saleId)) {
            if (routeSale.getDeliveryDate() == null && isRouteActive(routeSale.getRoute().getRouteId())) {
                return routeSale;
            }
        }
        return null;
    }

    /** Closes the route when no sale is pending. Returns true if it was closed. */
    private boolean completeRouteIfFinished(int routeId) {
        for (RouteSale routeSale : routeSaleDao.findRouteSalesByRoute(routeId)) {
            if (routeSale.getDeliveryDate() == null) {
                return false;
            }
        }

        DeliveryRoute route = routeDao.findById(routeId).get();
        route.setStatus(DeliveryRouteStatus.COMPLETED);
        route.setActualArrivalTime(LocalDateTime.now());
        routeDao.update(route);
        return true;
    }

    // ---------- Vehicles and drivers ----------

    private void registerVehicle() {
        Vehicle vehicle = view.readNewVehicle();

        if (vehicle.getLicensePlate().isBlank()) {
            view.showMessage("The vehicle could not be registered because the license plate is empty.");
            return;
        }
        if (vehicle.getCapacity() <= 0) {
            view.showMessage("The vehicle could not be registered because the capacity must be greater than 0.");
            return;
        }
        for (Vehicle existing : vehicleDao.findAllVehicles()) {
            if (existing.getLicensePlate().equalsIgnoreCase(vehicle.getLicensePlate())) {
                view.showMessage("A vehicle with plate " + vehicle.getLicensePlate() + " already exists.");
                return;
            }
        }

        vehicleDao.saveVehicle(vehicle);
        view.showMessage("Vehicle " + vehicle.getLicensePlate() + " registered successfully.");
    }

    private void registerDriver() {
        Driver driver = view.readNewDriver();

        if (driver.getName().isBlank() || driver.getLicenseNumber().isBlank()) {
            view.showMessage("The driver could not be registered because the name and license are required.");
            return;
        }
        for (Driver existing : driverDao.findAllDrivers()) {
            if (existing.getLicenseNumber().equalsIgnoreCase(driver.getLicenseNumber())) {
                view.showMessage("A driver with license " + driver.getLicenseNumber() + " already exists.");
                return;
            }
        }

        driverDao.saveDriver(driver);
        view.showMessage("Driver " + driver.getName() + " registered successfully.");
    }

    // ---------- Helpers ----------

    private List<Sale> findSalesByStatus(SaleStatus status) {
        return filterByStatus(saleDao.findAll(), status);
    }

    private List<Sale> filterByStatus(List<Sale> sales, SaleStatus status) {
        List<Sale> filtered = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getSaleStatus() == status) {
                filtered.add(sale);
            }
        }
        return filtered;
    }

    private Sale findSale(int saleId, List<Sale> sales) {
        for (Sale sale : sales) {
            if (sale.getId() == saleId) {
                return sale;
            }
        }
        return null;
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            view.showMessage("The changes could not be undone: " + e.getMessage());
        }
    }

    private void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            view.showMessage("The connection could not be restored: " + e.getMessage());
        }
    }
}
