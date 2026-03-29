package com.aems.dto.response;

import java.math.BigDecimal;

public class RevenueReportResponse {
    private String period;
    private Integer totalOrders;
    private Integer completedOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal netRevenue;
    private BigDecimal averageOrderValue;
    private String topCrop;
    private String topBuyer;
    
    public RevenueReportResponse() {
    }
    
    public RevenueReportResponse(String period, Integer totalOrders, Integer completedOrders,
                                BigDecimal totalRevenue, BigDecimal totalTax, BigDecimal netRevenue,
                                BigDecimal averageOrderValue, String topCrop, String topBuyer) {
        this.period = period;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.totalRevenue = totalRevenue;
        this.totalTax = totalTax;
        this.netRevenue = netRevenue;
        this.averageOrderValue = averageOrderValue;
        this.topCrop = topCrop;
        this.topBuyer = topBuyer;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public Integer getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public Integer getCompletedOrders() {
        return completedOrders;
    }
    
    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getTotalTax() {
        return totalTax;
    }
    
    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }
    
    public BigDecimal getNetRevenue() {
        return netRevenue;
    }
    
    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }
    
    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public String getTopCrop() {
        return topCrop;
    }
    
    public void setTopCrop(String topCrop) {
        this.topCrop = topCrop;
    }
    
    public String getTopBuyer() {
        return topBuyer;
    }
    
    public void setTopBuyer(String topBuyer) {
        this.topBuyer = topBuyer;
    }
}
