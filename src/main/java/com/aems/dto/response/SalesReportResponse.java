package com.aems.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesReportResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String buyerName;
    private String buyerEmail;
    private String cropName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime approvedDate;
    private LocalDateTime deliveredDate;
    
    public SalesReportResponse() {
    }
    
    public SalesReportResponse(Long orderId, String orderNumber, LocalDateTime orderDate,
                              String buyerName, String buyerEmail, String cropName,
                              BigDecimal quantity, String unit, BigDecimal pricePerUnit,
                              BigDecimal totalAmount, String status, LocalDateTime approvedDate,
                              LocalDateTime deliveredDate) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.cropName = cropName;
        this.quantity = quantity;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = totalAmount;
        this.status = status;
        this.approvedDate = approvedDate;
        this.deliveredDate = deliveredDate;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public String getBuyerEmail() {
        return buyerEmail;
    }
    
    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }
    
    public String getCropName() {
        return cropName;
    }
    
    public void setCropName(String cropName) {
        this.cropName = cropName;
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
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }
    
    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }
    
    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }
}
