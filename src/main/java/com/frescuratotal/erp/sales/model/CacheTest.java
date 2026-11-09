package com.frescuratotal.erp.sales.model;

import java.util.Optional;

public class CacheTest {
    public static void main(String[] args) {
        CustomerService customerService = new CustomerService();
        SaleService saleService = new SaleService();

        System.out.println("--- Caso 1: cliente 5, primera vez ---");
        Optional<Customer> c1 = customerService.findById(5);
        System.out.println(c1.isPresent() ? c1.get().getName() : "Customer not found");

        System.out.println("--- Caso 2: cliente 5, recupera datos ---");
        Optional<Customer> c2 = customerService.findById(5);
        System.out.println(c2.isPresent() ? c2.get().getName() : "Customer not found");

        System.out.println("--- Caso 3: cliente 8, consulta sql ID distinto");
        Optional<Customer> c3 = customerService.findById(8);
        System.out.println(c3.isPresent() ? c3.get().getName() : "Customer not found");

        System.out.println(" Caso 4: cliente 5, consulta desde otro módulo");
        Optional<Customer> c4 = saleService.findCustomerById(5);
        System.out.println(c4.isPresent() ? c4.get().getName() : "Customer not found");

        System.out.println(" Caso 5: cliente 999, consulta cliente inexistente");
        Optional<Customer> c5 = saleService.findCustomerById(999);
        System.out.println(c5.isPresent() ? c5.get().getName() : "Customer not found");







    }


}
