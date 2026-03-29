package com.aems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
public class Stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;
    
    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;
    
    @ManyToOne
    @JoinColumn(name = "import_source_id")
    private ImportSource importSource;
    
    @Column(nullable = false)
    private BigDecimal quantity;
    
    @Column(nullable = false, length = 20)
    private String unit;
    
    @Column(nullable = false)
    private BigDecimal pricePerUnit;
    
    @Column(name = "total_value")
    private BigDecimal totalValue;
    
    @Column(length = 50)
    private String batchNumber;
    
    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(length = 20)
    private String quality;
    
    @Column(length = 255)
    private String remarks;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (entryDate == null) {
            entryDate = LocalDateTime.now();
        }
        if (quantity != null && pricePerUnit != null) {
            totalValue = quantity.multiply(pricePerUnit);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (quantity != null && pricePerUnit != null) {
            totalValue = quantity.multiply(pricePerUnit);
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Crop getCrop() {
        return crop;
    }
    
    public void setCrop(Crop crop) {
        this.crop = crop;
    }
    
    public Farmer getFarmer() {
        return farmer;
    }
    
    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }
    
    public ImportSource getImportSource() {
        return importSource;
    }
    
    public void setImportSource(ImportSource importSource) {
        this.importSource = importSource;
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
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    
    public LocalDateTime getEntryDate() {
        return entryDate;
    }
    
    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
