package com.frescuratotal.erp.sales.model;

import java.util.Optional;

public class CustomerTest {
    public static void main(String[] args) {
        /*Customer customer = new Customer(
                "Restaurante El Sabor",
                "800555111",
                "elsabor@gmail.com",
                "3001234567",
                "Calle 10 #5-20",
                CustomerType.WHOLESALE
                );*/
        CustomerDao dao = new CustomerDaoImpl();

        /*boolean saved = dao.save(customer);
        System.out.println(saved);*/

        /*Optional<Customer> box = dao.findById(1);

        if (box.isPresent()) {
            System.out.println(box.get());

        } else {
            System.out.println("Customer not found");
        }*/

        /*Customer customer = new Customer(
                "Tienda Doña Rosa",
                "52123456",
                "donarosa@gmail.com",
                "3109876543",
                "Carrera 7 #12-30",
                CustomerType.RETAIL
        );
        System.out.println("Saved: " + dao.save(customer));*/

        System.out.println(dao.findAll());

    }
}
