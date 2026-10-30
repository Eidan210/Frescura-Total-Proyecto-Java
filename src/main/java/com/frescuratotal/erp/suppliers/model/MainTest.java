package com.frescuratotal.erp.suppliers.model;




import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MainTest {

    public static void main(String[] args) {
        SupplierDao supplierDao = new SupplierDaoImpl();

        System.out.println("=== 1. Probando insertar un proveedor ===");
        Supplier newSupplier = new Supplier();
        newSupplier.setName("Distribuidora El Fresco S.A.S.");
        newSupplier.setNit("900123456-2");
        newSupplier.setContactPerson("Carlos Pérez");
        newSupplier.setEmail("contacto@elfresco.com");
        newSupplier.setPhone("3101234567");
        newSupplier.setAddress("Calle 10 # 20-30");
        newSupplier.setActive(1);
        newSupplier.setRegistrationDate(LocalDateTime.now());

        boolean isSaved = supplierDao.save(newSupplier);
        if (isSaved) {
            System.out.println("¡Proveedor guardado con éxito!\n");
        } else {
            System.out.println("Error al guardar el proveedor.\n");
        }

        System.out.println("=== 2. Probando listar todos los proveedores ===");
        List<Supplier> suppliers = supplierDao.findAll();
        System.out.println("Total de proveedores en la base de datos: " + suppliers.size());

        for (Supplier s : suppliers) {
            System.out.println("- ID: " + s.getId() + " | Nombre: " + s.getName() + " | NIT: " + s.getNit());
        }
        System.out.println();

        System.out.println("=== 3. Probando buscar por ID (Ej: ID 1) ===");

        Optional<Supplier> foundSupplier = supplierDao.findById(1);

        if (foundSupplier.isPresent()) {
            System.out.println("¡Encontrado!: " + foundSupplier.get());
        } else {
            System.out.println("No se encontró ningún proveedor con ese ID.");
        }
    }
}