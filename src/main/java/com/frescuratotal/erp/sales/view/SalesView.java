package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.sales.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class SalesView {
    private final Scanner sc;

    public SalesView(Scanner sc) {
        this.sc = sc;
    }

    public int showMenu() {
        System.out.println("====== Sales ======");
        System.out.println("1. Register customer\n" +
                "2. List customers\n" +
                "3. New sale\n" +
                "4. List sales\n" +
                "5. Send sale to preparation (logistics)\n" +
                "6. Find customer by ID (cache)\n" +          // EJERCICIO 1
                "7. Import customers from file\n" +           // EJERCICIO 4
                "8. Generate commercial statistics\n" +       // EJERCICIO 5
                "9. Back");
        return readInt("Option: ");
    }

    // ---------- Customers ----------

    public Customer readNewCustomer() {
        String name = readRequired("Name: ");
        String document = readRequired("Document (NIT / ID): ");
        String email = readLine("Email: ");
        String phone = readLine("Phone: ");
        String address = readRequired("Address: "); // a sale without it cannot be delivered
        CustomerType type = readCustomerType();
        return new Customer(name, document, email, phone, address, type);
    }

    public void showCustomers(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        System.out.printf("%-5s %-30s %-15s %-10s %-15s%n", "ID", "Name", "Document", "Type", "Phone");
        for (Customer c : customers) {
            System.out.printf("%-5d %-30s %-15s %-10s %-15s%n",
                    c.getId(), c.getName(), c.getDocument(), c.getType(), c.getPhone());
        }
    }

    public String readDocument() {
        return readLine("Customer document: ");
    }

    // ---------- New sale ----------

    public String readSellerName() {
        return readLine("Seller name: ");
    }

    public void showProducts(List<Product> products) {
        System.out.println("--- Products ---");
        System.out.printf("%-5s %-30s %-10s%n", "ID", "Product", "Unit");
        for (Product p : products) {
            System.out.printf("%-5d %-30s %-10s%n", p.getId(), p.getName(), p.getUnitOfMeasure());
        }
    }

    /** Returns 0 when the seller wants to finish adding products. */
    public int readProductId() {
        return readInt("Product ID (0 to finish): ");
    }

    public BigDecimal readQuantity() {
        return readBigDecimal("Quantity: ");
    }

    public void showCart(Sale sale) {
        System.out.println("--- Sale " + sale.getSaleNumber() + " | " + sale.getCustomer().getName() + " ---");
        if (sale.getSaleDetails().isEmpty()) {
            System.out.println("(no products yet)");
            return;
        }
        System.out.printf("%-20s %-12s %-8s %-12s %-10s %-12s%n",
                "Lot", "Expires", "Qty", "Unit price", "Discount", "Line total");
        for (SaleDetail d : sale.getSaleDetails()) {
            System.out.printf("%-20s %-12s %-8s %-12s %-10s %-12s%n",
                    d.getLot().getLotNumber(), d.getLot().getExpirationDate(), d.getQuantitySold(),
                    d.getUnitPrice(), d.getDiscountApplied().multiply(new BigDecimal("100")).intValue() + "%",
                    d.getLineTotal());
        }
    }

    public boolean confirm(String question) {
        String answer = readLine(question + " (y/n): ");
        return answer.equalsIgnoreCase("y");
    }

    public void showReceipt(SaleReceipt receipt) {
        System.out.println("Sale confirmed!");
        System.out.println("Invoice: " + receipt.invoice().getInvoiceNumber());
        System.out.println("Subtotal: " + receipt.invoice().getSubtotal()
                + " | Discount: " + receipt.invoice().getTotalDiscount()
                + " | VAT: " + receipt.invoice().getTaxAmount());
        System.out.println("TOTAL: " + receipt.invoice().getTotalAmount());
        System.out.println("Invoice image: " + receipt.invoiceFile());
    }

    // ---------- Sales ----------

    public void showSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No sales found.");
            return;
        }
        System.out.printf("%-5s %-26s %-20s %-25s %-12s %-15s%n",
                "ID", "Sale", "Date", "Customer", "Total", "Status");
        for (Sale s : sales) {
            System.out.printf("%-5d %-26s %-20s %-25s %-12s %-15s%n",
                    s.getId(), s.getSaleNumber(), s.getSaleDate().withNano(0),
                    s.getCustomer().getName(), s.getTotalAmount(), s.getSaleStatus());
        }
    }

    public int readSaleId() {
        return readInt("Sale ID: ");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    // ---------- Input helpers ----------

    /** Uses CustomerType.fromId: if the number does not exist, we catch the error and ask again. */
    private CustomerType readCustomerType() {
        while (true) {
            int id = readInt("Type (1 = Wholesale, 2 = Retail): ");
            try {
                return CustomerType.fromId(id);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid option, choose 1 or 2.");
            }
        }
    }

    // EJERCICIO 3: la VISTA pregunta la modalidad y devuelve el enum.
    // public porque la llama el controlador (otra clase).
    // while(true) + return: si escriben 7, vuelve a preguntar hasta recibir 1 o 2.
    // No hace falta else: cada return sale del método de inmediato.
    public DeliveryType readDeliveryType() {
        while (true) {
            int option = readInt("Delivery type (1 = Standard, 2 = Urgent): ");
            if (option == 1) {
                return DeliveryType.STANDARD;
            }
            if (option == 2) {
                return DeliveryType.URGENT;
            }
            System.out.println("Invalid option, choose 1 or 2.");
        }
    }

    // ---------- EJERCICIO 1: buscar cliente por ID ----------

    public int readCustomerId() {
        return readInt("Customer ID: ");
    }

    // ---------- EJERCICIO 4: importación ----------

    // Muestra el detalle de cada fila inválida (línea y motivo).
    public void showImportErrors(List<String> errors) {
        for (String error : errors) {
            System.out.println("  - " + error);
        }
    }

    // ---------- EJERCICIO 5: estadísticas ----------

    // La vista solo IMPRIME; los cálculos ya vienen hechos desde CommercialStatsService.
    public void showCommercialStats(CommercialStats stats) {
        System.out.println("===== ESTADISTICAS COMERCIALES =====");

        // ifPresentOrElse: si hay categoría la muestra; si no (solo hubo devoluciones), otro mensaje.
        // stripTrailingZeros().toPlainString(): 70.00 -> "70" (sin notación científica como 7E+1).
        stats.topCategory().ifPresentOrElse(
                top -> System.out.println("Categoria con mayor volumen vendido: " + top.category()
                        + " - " + top.units().stripTrailingZeros().toPlainString() + " unidades"),
                () -> System.out.println("Categoria con mayor volumen vendido: no hay ventas confirmadas"));

        // Si no hay devoluciones, se muestra el apartado sin nombres (y sin errores).
        System.out.println("Clientes con devoluciones:");
        stats.customersWithReturns().forEach(name -> System.out.println("- " + name));
    }

    private int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            String line = readLine(prompt).replace(',', '.');
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                System.out.println("'" + line + "' is not a valid number.");
            }
        }
    }

    private String readRequired(String prompt) {
        String value = readLine(prompt);
        while (value.isEmpty()) {
            System.out.println("This field is required.");
            value = readLine(prompt);
        }
        return value;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
