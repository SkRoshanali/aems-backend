package com.aems.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class StockRequest {
    
    @NotNull(message = "Crop ID is required")
    private Long cropId;
    
    private Long farmerId;
    private Long importSourceId;
    
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    
    @NotBlank(message = "Unit is required")
    private String unit;
    
    @NotNull(message = "Price per unit is required")
    private BigDecimal pricePerUnit;
    
    private String batchNumber;
    private String quality;
    private String remarks;
    
    // Getters and Setters
    public Long getCropId() {
        return cropId;
    }
    
    public void setCropId(Long cropId) {
        this.cropId = cropId;
    }
    
    public Long getFarmerId() {
        return farmerId;
    }
    
    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }
    
    public Long getImportSourceId() {
        return importSourceId;
    }
    
    public void setImportSourceId(Long importSourceId) {
        this.importSourceId = importSourceId;
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
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    
    public String getQuality() {
        return quality;
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
