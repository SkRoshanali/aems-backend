package com.aems.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CropRequest {
    
    @NotBlank(message = "Crop name is required")
    private String cropName;
    
    @NotBlank(message = "Crop code is required")
    private String cropCode;
    
    private String description;
    private String category;
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
