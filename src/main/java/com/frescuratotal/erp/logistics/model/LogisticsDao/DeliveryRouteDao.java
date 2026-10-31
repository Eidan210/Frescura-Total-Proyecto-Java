package com.frescuratotal.erp.logistics.model.LogisticsDao;

import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.status.DeliveryRouteStatus;

import java.util.List;
import java.util.Optional;

public interface DeliveryRouteDao {
    void save(DeliveryRoute route);
    Optional<DeliveryRoute> findById(int routeId);
    List<DeliveryRoute> findAll();
    List<DeliveryRoute> findByStatus(DeliveryRouteStatus status);
    void update(DeliveryRoute route);
    void delete(int routeId);
}
