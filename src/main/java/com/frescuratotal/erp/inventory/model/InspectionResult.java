package com.frescuratotal.erp.inventory.model;

public enum InspectionResult {
    APPROVED("Aprobado"),
    REJECTED("Rechazado");

    private final String dbValue;

    InspectionResult(String dbValue){
        this.dbValue = dbValue;
    }

    public String getDbValue(){
        return dbValue;
    }

    public static InspectionResult fromDbValue(String dbValue){
        for(InspectionResult result : InspectionResult.values()){
            if(result.getDbValue().equals(dbValue)) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown inspection result "+ dbValue);
    }
}
