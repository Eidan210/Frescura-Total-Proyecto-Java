package com.frescuratotal.erp.sales.model;

public interface InvoiceDao {
    boolean save(Invoice invoice);
    CompanyInfo findCompanyInfo();
}
