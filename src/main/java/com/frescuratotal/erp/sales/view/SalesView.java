package com.frescuratotal.erp.sales.view;

import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerCache;
import com.frescuratotal.erp.sales.model.CustomerType;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleDetail;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import com.frescuratotal.erp.sales.model.delivery.DeliveryMode;
import com.frescuratotal.erp.sales.model.stats.CategoryTotal;
import com.frescuratotal.erp.sales.model.stats.CommercialStatistics;

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
                "6. Find customer by ID\n" +
                "7. Import customers from file\n" +
                "8. Generate commercial statistics\n" +
                "9. Register sale return\n" +
                "10. Back");
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

    public int readCustomerId() {
        return readInt("Customer ID: ");
    }

    public void showCustomer(Customer c) {
        System.out.println("ID: " + c.getId() +
                "\nName: " + c.getName() +
                "\nDocument: " + c.getDocument() +
                "\nEmail: " + c.getEmail() +
                "\nPhone: " + c.getPhone() +
                "\nAddress: " + c.getAddress() +
                "\nType: " + c.getType());
    }

    public void showCacheStatus(CustomerCache cache) {
        System.out.println("(Customer cache: " + cache.size() + " stored | hits " + cache.getHits()
                + " | misses " + cache.getMisses() + ")");
    }

    public void showErrors(List<String> errors) {
        for (String error : errors) {
            System.out.println("  - " + error);
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

    public DeliveryMode readDeliveryMode() {
        DeliveryMode[] modes = DeliveryMode.values();
        System.out.println("--- Delivery ---");
        for (int i = 0; i < modes.length; i++) {
            System.out.println((i + 1) + ". " + modes[i].getLabel() + " (" + modes[i].getDescription() + ")");
        }
        while (true) {
            int option = readInt("Delivery mode: ");
            if (option >= 1 && option <= modes.length) {
                return modes[option - 1];
            }
            System.out.println("Invalid option, choose 1 to " + modes.length + ".");
        }
    }

    public void showAmountToPay(Sale sale, DeliveryMode mode, BigDecimal finalAmount) {
        System.out.println("Purchase value: " + sale.getTotalAmount());
        System.out.println("Delivery " + mode.getLabel() + " (" + mode.getDescription() + ")");
        System.out.println("AMOUNT TO PAY: " + finalAmount);
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
        Sale sale = receipt.invoice().getSale();
        System.out.println("Delivery: " + sale.getDeliveryMode().getLabel() + " | Surcharge: " + sale.getDeliveryFee());
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

    // ---------- Statistics ----------

    public void showStatistics(CommercialStatistics stats) {
        if (!stats.hasData()) {
            System.out.println("No existen datos suficientes para generar estadísticas comerciales.");
            return;
        }
        System.out.println("===== ESTADISTICAS COMERCIALES =====");
        if (stats.topCategory().isPresent()) {
            CategoryTotal top = stats.topCategory().get();
            System.out.println("Categoria con mayor volumen vendido: " + top.categoryName() + " - "
                    + top.units().stripTrailingZeros().toPlainString() + " unidades");
        } else {
            System.out.println("Categoria con mayor volumen vendido: no hay ventas confirmadas");
        }
        System.out.println("Clientes con devoluciones:");
        stats.customersWithReturns().forEach(name -> System.out.println("- " + name));
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
