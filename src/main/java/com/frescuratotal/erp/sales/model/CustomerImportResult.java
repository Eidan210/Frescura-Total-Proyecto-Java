package com.frescuratotal.erp.sales.model;

import java.util.List;

/** Outcome of an import (Exercise 4). Errors say which line failed and why. */
public record CustomerImportResult(Status status, int importedCount, List<String> errors) {

    public enum Status {
        FILE_NOT_FOUND,
        INVALID_RECORDS,
        SAVE_FAILED,
        COMPLETED
    }

    public static CustomerImportResult fileNotFound() {
        return new CustomerImportResult(Status.FILE_NOT_FOUND, 0, List.of());
    }

    public static CustomerImportResult invalid(List<String> errors) {
        return new CustomerImportResult(Status.INVALID_RECORDS, 0, List.copyOf(errors));
    }

    public static CustomerImportResult saveFailed(String error) {
        return new CustomerImportResult(Status.SAVE_FAILED, 0, List.of(error));
    }

    public static CustomerImportResult completed(int count) {
        return new CustomerImportResult(Status.COMPLETED, count, List.of());
    }
}
