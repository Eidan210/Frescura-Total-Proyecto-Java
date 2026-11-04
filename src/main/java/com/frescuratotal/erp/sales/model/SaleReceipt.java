package com.frescuratotal.erp.sales.model;

import java.nio.file.Path;

/** What confirmSale returns: the saved invoice and where its image was created. */
public record SaleReceipt(Invoice invoice, Path invoiceFile) {
}
