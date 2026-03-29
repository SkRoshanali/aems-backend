package com.aems.dto.response;

import java.math.BigDecimal;

public class DashboardStatsResponse {
    private Integer totalStocks;
    private Integer activeStocks;
    private Integer totalOrders;
    private Integer pendingOrders;
    private Integer approvedOrders;
    private Integer completedOrders;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Integer totalFarmers;
    private Integer verifiedFarmers;
    private Integer totalBuyers;
    private Integer activeBuyers;
    private Integer totalShipments;
    private Integer inTransitShipments;
    private Integer totalInvoices;
    private Integer paidInvoices;
    private Integer overdueInvoices;
    
    public DashboardStatsResponse() {
    }
    
    public Integer getTotalStocks() {
        return totalStocks;
    }
    
    public void setTotalStocks(Integer totalStocks) {
        this.totalStocks = totalStocks;
    }
    
    public Integer getActiveStocks() {
        return activeStocks;
    }
    
    public void setActiveStocks(Integer activeStocks) {
        this.activeStocks = activeStocks;
    }
    
    public Integer getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public Integer getPendingOrders() {
        return pendingOrders;
    }
    
    public void setPendingOrders(Integer pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
    
    public Integer getApprovedOrders() {
        return approvedOrders;
    }
    
    public void setApprovedOrders(Integer approvedOrders) {
        this.approvedOrders = approvedOrders;
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
    
    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }
    
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
    
    public Integer getTotalFarmers() {
        return totalFarmers;
    }
    
    public void setTotalFarmers(Integer totalFarmers) {
        this.totalFarmers = totalFarmers;
    }
    
    public Integer getVerifiedFarmers() {
        return verifiedFarmers;
    }
    
    public void setVerifiedFarmers(Integer verifiedFarmers) {
        this.verifiedFarmers = verifiedFarmers;
    }
    
    public Integer getTotalBuyers() {
        return totalBuyers;
    }
    
    public void setTotalBuyers(Integer totalBuyers) {
        this.totalBuyers = totalBuyers;
    }
    
    public Integer getActiveBuyers() {
        return activeBuyers;
    }
    
    public void setActiveBuyers(Integer activeBuyers) {
        this.activeBuyers = activeBuyers;
    }
    
    public Integer getTotalShipments() {
        return totalShipments;
    }
    
    public void setTotalShipments(Integer totalShipments) {
        this.totalShipments = totalShipments;
    }
    
    public Integer getInTransitShipments() {
        return inTransitShipments;
    }
    
    public void setInTransitShipments(Integer inTransitShipments) {
        this.inTransitShipments = inTransitShipments;
    }
    
    public Integer getTotalInvoices() {
        return totalInvoices;
    }
    
    public void setTotalInvoices(Integer totalInvoices) {
        this.totalInvoices = totalInvoices;
    }
    
    public Integer getPaidInvoices() {
        return paidInvoices;
    }
    
    public void setPaidInvoices(Integer paidInvoices) {
        this.paidInvoices = paidInvoices;
    }
    
    public Integer getOverdueInvoices() {
        return overdueInvoices;
    }
    
    public void setOverdueInvoices(Integer overdueInvoices) {
        this.overdueInvoices = overdueInvoices;
    }
}
