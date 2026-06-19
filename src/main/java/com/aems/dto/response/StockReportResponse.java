package com.aems.dto.response;

import java.math.BigDecimal;

public class StockReportResponse {
    private Long stockId;
    private String cropName;
    private String category;
    private String farmerName;
    private String importSourceName;
    private String country;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private BigDecimal totalValue;
    private Boolean isActive;
    private String qualityGrade;
    
    public StockReportResponse() {
    }
    
    public StockReportResponse(Long stockId, String cropName, String category, String farmerName,
                              String importSourceName, String country, BigDecimal quantity, String unit,
                              BigDecimal pricePerUnit, BigDecimal totalValue, Boolean isActive, String qualityGrade) {
        this.stockId = stockId;
        this.cropName = cropName;
        this.category = category;
        this.farmerName = farmerName;
        this.importSourceName = importSourceName;
        this.country = country;
        this.quantity = quantity;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.totalValue = totalValue;
        this.isActive = isActive;
        this.qualityGrade = qualityGrade;
    }
    
    public Long getStockId() {
        return stockId;
    }
    
    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }
    
    public String getCropName() {
        return cropName;
    }
    
    public void setCropName(String cropName) {
        this.cropName = cropName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getFarmerName() {
        return farmerName;
    }
    
    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }
    
    public String getImportSourceName() {
        return importSourceName;
    }
    
    public void setImportSourceName(String importSourceName) {
        this.importSourceName = importSourceName;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }
    
    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getQualityGrade() {
        return qualityGrade;
    }
    
    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }
}
