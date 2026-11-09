package com.frescuratotal.erp.sales.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomerCache {
    private static CustomerCache instance;
    private final Map<Integer, Customer> cache = new HashMap<>();
    private final CustomerDao customerDao = new CustomerDaoImpl();


    private CustomerCache() {
    }

    public static CustomerCache getInstance(){ // este es el if que garantiza que solo haya uan libreta
        if(instance == null){
            instance = new CustomerCache();
        }
        return instance;
    }

    public Optional<Customer> findById(int id){
        if (cache.containsKey(id)){
            System.out.println("[CACHE] Customer " + id + " found in memory");
            return Optional.of(cache.get(id));
        }else{
            System.out.println("[SQL] Querying customer " + id + " from the database");
            Optional<Customer> result = customerDao.findById(id);
            result.ifPresent(customer -> cache.put(id, customer));

            /*if (result.isPresent()){
                cache.put(id, result.get()); esta es la manera larga de la version abreviada del lamda
            }*/
            return result;
        }

    }
}
