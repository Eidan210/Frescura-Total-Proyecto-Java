package com.frescuratotal.erp.logistics.Dao.Impl;

import com.frescuratotal.erp.logistics.Dao.RouteSaleDao;
import com.frescuratotal.erp.logistics.model.RouteSale;

import java.util.List;
import java.util.Optional;

// shortcut: sin implementar porque Sale todavía no tiene id (id_venta es NOT NULL); implementar cuando Sale tenga getSaleId/setSaleId.
public class RouteSaleDaoimpl implements RouteSaleDao {

    private static final String PENDING = "Pendiente: Sale no tiene id todavía";

    @Override
    public void saveRouteSale(RouteSale routeSale) {
        throw new UnsupportedOperationException(PENDING);
    }

    @Override
    public Optional<RouteSale> findRouteSaleById(int routeSaleId) {
        throw new UnsupportedOperationException(PENDING);
    }

    @Override
    public List<RouteSale> findRouteSalesByRoute(int routeId) {
        throw new UnsupportedOperationException(PENDING);
    }

    @Override
    public List<RouteSale> findRouteSalesBySale(int saleId) {
        throw new UnsupportedOperationException(PENDING);
    }

    @Override
    public void updateRouteSale(RouteSale routeSale) {
        throw new UnsupportedOperationException(PENDING);
    }

    @Override
    public void deleteRouteSale(int routeSaleId) {
        throw new UnsupportedOperationException(PENDING);
    }
}
