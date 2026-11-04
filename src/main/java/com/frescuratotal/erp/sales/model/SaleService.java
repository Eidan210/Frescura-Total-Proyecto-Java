package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.common.exception.InsufficientStockException;
import com.frescuratotal.erp.common.util.CodeGenerator;
import com.frescuratotal.erp.common.util.InputUtil;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.LotDaoImpl;
import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.inventory.model.ProductDaoImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;


public class SaleService {
    private final LotDao lotDao = new LotDaoImpl();
    private final SaleDao saleDao = new SaleDaoImpl();
    private final InvoiceDao invoiceDao = new InvoiceDaoImpl();
    private final InvoiceGenerator invoiceGenerator = new InvoiceGenerator(new ProductDaoImpl());

    private static final BigDecimal NEAR_EXPIRY_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");

    /** Opens a new sale for a customer. It starts as REGISTERED (see the Sale constructor). */
    public Sale createSale(Customer customer, String registeredBy) {
        return new Sale(CodeGenerator.generateSaleNumber(), customer, registeredBy);
    }

    public void addProduct(Sale sale, int productId, BigDecimal quantity) {
        InputUtil.requireAmount(quantity, "Quantity");

        List<Lot> lots = lotDao.findAvailableLots(productId);

        // What is already in this sale is not free anymore (same product added twice)
        for (Lot lot : lots) {
            lot.setQuantity(lot.getQuantity().subtract(reservedInSale(sale, lot.getId())));
        }

        BigDecimal available = BigDecimal.ZERO;

        for (Lot lot : lots) {
            available = available.add(lot.getQuantity());
        }
        if (available.compareTo(quantity) < 0) {
            throw new InsufficientStockException("Not enough stock. Available: " + available);
        }

        BigDecimal remaining = quantity;


        for (Lot lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
            if (lot.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue; // this lot is already fully used in this sale
            }
            BigDecimal taken = remaining.min(lot.getQuantity());
            remaining = remaining.subtract(taken);


            SaleDetail saleDetail = new SaleDetail(taken,lot);

            // By date, not by stored status: that one is only as fresh as the last expiration check
            if (LotStatus.forExpiration(lot.getExpirationDate()) == LotStatus.NEAR_EXPIRY){
                saleDetail.setDiscountApplied(NEAR_EXPIRY_DISCOUNT);
            }else{
                saleDetail.setDiscountApplied(BigDecimal.ZERO);
            }

            calculateDetail(saleDetail);

            sale.getSaleDetails().add(saleDetail);


        }
    }

    /**
     * The customer says "done": totals, invoice image and then one transaction for the data.
     * The image goes first so a failure there leaves the sale and the stock untouched.
     */
    public SaleReceipt confirmSale(Sale sale) {
        if (sale.getSaleDetails().isEmpty()) {
            throw new IllegalArgumentException("The sale has no products");
        }

        calculateTotals(sale);

        CompanyInfo company = invoiceDao.findCompanyInfo();
        String invoiceNumber = CodeGenerator.generateInvoiceNumber();
        LocalDateTime issueDate = LocalDateTime.now();
        String cufe = invoiceGenerator.generateCufe(invoiceNumber, issueDate, sale, company.getNit());

        Invoice invoice = new Invoice(sale, invoiceNumber, cufe, invoiceGenerator.buildQrLink(cufe));
        invoice.setIssueDate(issueDate);

        Path invoiceFile = invoiceGenerator.generateImage(invoice, company);
        try {
            DatabaseConnection.getInstance().inTransaction(() -> {
                saleDao.save(sale);
                invoiceDao.save(invoice);
            });
        } catch (RuntimeException e) {
            invoiceFile.toFile().delete(); // no sale, so no invoice
            throw e;
        }
        return new SaleReceipt(invoice, invoiceFile);
    }

    public List<Sale> findAllSales() {
        return saleDao.findAll();
    }

    /** Sends a registered sale to the logistics panel (status In Preparation). */
    public boolean sendToPreparation(int saleId) {
        return saleDao.updateStatus(saleId, SaleStatus.REGISTERED, SaleStatus.IN_PREPARATION);
    }

    private BigDecimal reservedInSale(Sale sale, int lotId) {
        BigDecimal reserved = BigDecimal.ZERO;
        for (SaleDetail detail : sale.getSaleDetails()) {
            if (detail.getLot().getId() == lotId) {
                reserved = reserved.add(detail.getQuantitySold());
            }
        }
        return reserved;
    }

    private void calculateDetail(SaleDetail saleDetail) {

        BigDecimal subtotal = saleDetail.getUnitPrice().multiply(saleDetail.getQuantitySold()).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setSubtotal(subtotal);

        BigDecimal discountAmount = subtotal.multiply(saleDetail.getDiscountApplied()).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setDiscountAmount(discountAmount);

        BigDecimal base = subtotal.subtract(discountAmount);

        saleDetail.setTaxRate(TAX_RATE);

        BigDecimal taxAmount = base.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        saleDetail.setTaxAmount(taxAmount);

        BigDecimal lineTotal = base.add(taxAmount);
        saleDetail.setLineTotal(lineTotal);

    }

    void calculateTotals(Sale sale) {
        sale.setSubtotal(sum(sale, SaleDetail::getSubtotal));
        sale.setTotalDiscount(sum(sale, SaleDetail::getDiscountAmount));
        sale.setTotalTax(sum(sale, SaleDetail::getTaxAmount));
        sale.setTotalAmount(sum(sale, SaleDetail::getLineTotal));
    }

    private BigDecimal sum(Sale sale, Function<SaleDetail, BigDecimal> field) {
        return sale.getSaleDetails().stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }




}
