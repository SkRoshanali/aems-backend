package com.aems.controller;

import com.aems.dto.request.CropRequest;
import com.aems.entity.Crop;
import com.aems.service.CropService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
@Tag(name = "Crop Management", description = "APIs for managing crops")
@SecurityRequirement(name = "Bearer Authentication")
public class CropController {
    
    @Autowired
    private CropService cropService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new crop")
    public ResponseEntity<Crop> createCrop(@Valid @RequestBody CropRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Crop crop = cropService.createCrop(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(crop);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update crop details")
    public ResponseEntity<Crop> updateCrop(@PathVariable Long id, @Valid @RequestBody CropRequest request) {
        Crop crop = cropService.updateCrop(id, request);
        return ResponseEntity.ok(crop);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get crop by ID")
    public ResponseEntity<Crop> getCropById(@PathVariable Long id) {
        Crop crop = cropService.getCropById(id);
        return ResponseEntity.ok(crop);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all crops")
    public ResponseEntity<List<Crop>> getAllCrops() {
        List<Crop> crops = cropService.getAllCrops();
        return ResponseEntity.ok(crops);
    }
    
    @GetMapping("/active/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all active crops")
    public ResponseEntity<List<Crop>> getActiveCrops() {
        List<Crop> crops = cropService.getActiveCrops();
        return ResponseEntity.ok(crops);
    }
    
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get crops by category")
    public ResponseEntity<List<Crop>> getCropsByCategory(@PathVariable String category) {
        List<Crop> crops = cropService.getCropsByCategory(category);
        return ResponseEntity.ok(crops);
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Deactivate a crop")
    public ResponseEntity<Crop> deactivateCrop(@PathVariable Long id) {
        Crop crop = cropService.deactivateCrop(id);
        return ResponseEntity.ok(crop);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a crop")
    public ResponseEntity<String> deleteCrop(@PathVariable Long id) {
        cropService.deleteCrop(id);
        return ResponseEntity.ok("Crop deleted successfully");
    }
}
