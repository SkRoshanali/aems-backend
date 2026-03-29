package com.aems.controller;

import com.aems.entity.User;
import com.aems.service.BuyerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyers")
@Tag(name = "Buyer Management", description = "APIs for managing buyer accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class BuyerController {
    
    @Autowired
    private BuyerService buyerService;
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get pending buyers (Admin)")
    public ResponseEntity<java.util.List<User>> getPendingBuyers() {
        java.util.List<User> buyers = buyerService.getPendingBuyers();
        return ResponseEntity.ok(buyers);
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Approve buyer registration (Admin)")
    public ResponseEntity<User> approveBuyer(@PathVariable Long id) {
        User buyer = buyerService.approveBuyer(id);
        return ResponseEntity.ok(buyer);
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Reject buyer registration (Admin)")
    public ResponseEntity<User> rejectBuyer(@PathVariable Long id) {
        User buyer = buyerService.rejectBuyer(id);
        return ResponseEntity.ok(buyer);
    }
}
