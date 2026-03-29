package com.aems.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderRequest {
    
    @NotNull(message = "Stock ID is required")
    private Long stockId;
    
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotBlank(message = "Delivery city is required")
    private String deliveryCity;
    
    @NotBlank(message = "Delivery state is required")
    private String deliveryState;
    
    @NotBlank(message = "Delivery zip code is required")
    private String deliveryZip;
    
    @NotBlank(message = "Delivery country is required")
    private String deliveryCountry;
    
    private String notes;
    
    // Getters and Setters
    public Long getStockId() {
        return stockId;
    }
    
    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryCity() {
        return deliveryCity;
    }
    
    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }
    
    public String getDeliveryState() {
        return deliveryState;
    }
    
    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
    }
    
    public String getDeliveryZip() {
        return deliveryZip;
    }
    
    public void setDeliveryZip(String deliveryZip) {
        this.deliveryZip = deliveryZip;
    }
    
    public String getDeliveryCountry() {
        return deliveryCountry;
    }
    
    public void setDeliveryCountry(String deliveryCountry) {
        this.deliveryCountry = deliveryCountry;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
