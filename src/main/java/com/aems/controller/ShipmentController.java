package com.aems.controller;

import com.aems.dto.request.ShipmentRequest;
import com.aems.entity.Shipment;
import com.aems.service.ShipmentService;
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
@RequestMapping("/api/shipments")
@Tag(name = "Shipment Management", description = "APIs for managing shipments")
@SecurityRequirement(name = "Bearer Authentication")
public class ShipmentController {
    
    @Autowired
    private ShipmentService shipmentService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create shipment for approved order")
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody ShipmentRequest request, Authentication authentication) {
        String email = authentication.getName();
        Shipment shipment = shipmentService.createShipmentByEmail(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'BUYER')")
    @Operation(summary = "Get shipment by ID")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/tracking/{trackingNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'BUYER')")
    @Operation(summary = "Track shipment by tracking number")
    public ResponseEntity<Shipment> trackShipment(@PathVariable String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'BUYER')")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {
        Shipment shipment = shipmentService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all shipments")
    public ResponseEntity<List<Shipment>> getAllShipments() {
        List<Shipment> shipments = shipmentService.getAllShipments();
        return ResponseEntity.ok(shipments);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get shipments by status")
    public ResponseEntity<List<Shipment>> getShipmentsByStatus(@PathVariable Shipment.ShipmentStatus status) {
        List<Shipment> shipments = shipmentService.getShipmentsByStatus(status);
        return ResponseEntity.ok(shipments);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam Shipment.ShipmentStatus status) {
        Shipment shipment = shipmentService.updateShipmentStatus(id, status);
        return ResponseEntity.ok(shipment);
    }
}
