package com.aems.controller;

import com.aems.dto.request.StockRequest;
import com.aems.entity.Stock;
import com.aems.service.StockService;
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
@RequestMapping("/api/stocks")
@Tag(name = "Stock Management", description = "APIs for managing inventory stocks")
@SecurityRequirement(name = "Bearer Authentication")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Create a new stock entry")
    public ResponseEntity<Stock> createStock(@Valid @RequestBody StockRequest request, Authentication authentication) {
        // authentication.getName() returns email, not user ID
        String userEmail = authentication.getName();
        Stock stock = stockService.createStockByEmail(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Update stock details")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        Stock stock = stockService.updateStock(id, request);
        return ResponseEntity.ok(stock);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get stock by ID")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        Stock stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/crop/{cropId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get stocks by crop")
    public ResponseEntity<List<Stock>> getStockByCrop(@PathVariable Long cropId) {
        List<Stock> stocks = stockService.getStockByCrop(cropId);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/farmer/{farmerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get stocks by farmer")
    public ResponseEntity<List<Stock>> getStockByFarmer(@PathVariable Long farmerId) {
        List<Stock> stocks = stockService.getStockByFarmer(farmerId);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/import-source/{importSourceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get stocks by import source")
    public ResponseEntity<List<Stock>> getStockByImportSource(@PathVariable Long importSourceId) {
        List<Stock> stocks = stockService.getStockByImportSource(importSourceId);
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/active/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all active stocks")
    public ResponseEntity<List<Stock>> getActiveStocks() {
        List<Stock> stocks = stockService.getActiveStocks();
        return ResponseEntity.ok(stocks);
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Deactivate a stock")
    public ResponseEntity<Stock> deactivateStock(@PathVariable Long id) {
        Stock stock = stockService.deactivateStock(id);
        return ResponseEntity.ok(stock);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete a stock")
    public ResponseEntity<String> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.ok("Stock deleted successfully");
    }
}
