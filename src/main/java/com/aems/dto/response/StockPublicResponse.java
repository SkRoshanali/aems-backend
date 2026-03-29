package com.aems.dto.response;

import java.math.BigDecimal;

public class StockPublicResponse {
    
    private Long id;
    private String cropName;
    private String cropCode;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private String quality;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCropName() {
        return cropName;
    }
    
    public void setCropName(String cropName) {
        this.cropName = cropName;
    }
    
    public String getCropCode() {
        return cropCode;
    }
    
    public void setCropCode(String cropCode) {
        this.cropCode = cropCode;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public String getQuality() {
        return quality;
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
}
