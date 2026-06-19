package com.aems.controller;

import com.aems.dto.request.FarmerRequest;
import com.aems.entity.Farmer;
import com.aems.service.FarmerService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/farmers")
@Tag(name = "Farmer Management", description = "APIs for managing farmers")
@SecurityRequirement(name = "Bearer Authentication")
public class FarmerController {
    
    @Autowired
    private FarmerService farmerService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Create a new farmer")
    public ResponseEntity<?> createFarmer(@Valid @RequestBody FarmerRequest request, Authentication authentication) {
        System.out.println("=== CREATE FARMER REQUEST RECEIVED ===");
        System.out.println("Authentication: " + authentication.getName());
        System.out.println("Request data: " + request.toString());
        
        try {
            // authentication.getName() returns email, not user ID
            String userEmail = authentication.getName();
            System.out.println("User email: " + userEmail);
            
            Farmer farmer = farmerService.createFarmerByEmail(request, userEmail);
            System.out.println("Farmer created successfully with ID: " + farmer.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(farmer);
        } catch (Exception e) {
            System.err.println("Error in createFarmer controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Update farmer details")
    public ResponseEntity<Farmer> updateFarmer(@PathVariable Long id, @Valid @RequestBody FarmerRequest request) {
        Farmer farmer = farmerService.updateFarmer(id, request);
        return ResponseEntity.ok(farmer);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get farmer by ID")
    public ResponseEntity<Farmer> getFarmerById(@PathVariable Long id) {
        Farmer farmer = farmerService.getFarmerById(id);
        return ResponseEntity.ok(farmer);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all farmers")
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        List<Farmer> farmers = farmerService.getAllFarmers();
        return ResponseEntity.ok(farmers);
    }
    
    @GetMapping("/active/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all active farmers")
    public ResponseEntity<List<Farmer>> getActiveFarmers() {
        List<Farmer> farmers = farmerService.getActiveFarmers();
        return ResponseEntity.ok(farmers);
    }
    
    @GetMapping("/verified/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all verified farmers")
    public ResponseEntity<List<Farmer>> getVerifiedFarmers() {
        List<Farmer> farmers = farmerService.getVerifiedFarmers();
        return ResponseEntity.ok(farmers);
    }
    
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Verify a farmer")
    public ResponseEntity<Farmer> verifyFarmer(@PathVariable Long id) {
        Farmer farmer = farmerService.verifyFarmer(id);
        return ResponseEntity.ok(farmer);
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Deactivate a farmer")
    public ResponseEntity<Farmer> deactivateFarmer(@PathVariable Long id) {
        Farmer farmer = farmerService.deactivateFarmer(id);
        return ResponseEntity.ok(farmer);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a farmer")
    public ResponseEntity<String> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.ok("Farmer deleted successfully");
    }
}
