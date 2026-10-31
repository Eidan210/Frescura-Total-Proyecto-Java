package com.frescuratotal.erp.logistics.model.LogisticsDao;

import com.frescuratotal.erp.logistics.model.RouteSale;

import java.util.List;
import java.util.Optional;

public interface RouteSaleDao {
    void saveRouteSale(RouteSale routeSale);
    Optional<RouteSale> findRouteSaleById(int routeSaleId);
    List<RouteSale> findRouteSalesByRoute(int routeId); // por deliveryOrder
    List<RouteSale> findRouteSalesBySale(int saleId);
    void updateRouteSale(RouteSale routeSale);
    void deleteRouteSale(int routeSaleId);
}
