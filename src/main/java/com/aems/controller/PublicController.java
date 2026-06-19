package com.aems.controller;

import com.aems.dto.request.BuyerRegistrationRequest;
import com.aems.dto.response.StockPublicResponse;
import com.aems.entity.User;
import com.aems.service.BuyerService;
import com.aems.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Portal", description = "Public APIs for buyers and storefront")
public class PublicController {
    
    @Autowired
    private PublicService publicService;
    
    @Autowired
    private BuyerService buyerService;
    
    @GetMapping("/stocks")
    @Operation(summary = "Get all available stocks (public)")
    public ResponseEntity<List<StockPublicResponse>> getAvailableStocks() {
        List<StockPublicResponse> stocks = publicService.getAvailableStocks();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/stocks/{id}")
    @Operation(summary = "Get stock details by ID (public)")
    public ResponseEntity<StockPublicResponse> getStockById(@PathVariable Long id) {
        StockPublicResponse stock = publicService.getStockById(id);
        return ResponseEntity.ok(stock);
    }
    
    @PostMapping("/register-buyer")
    @Operation(summary = "Register as a buyer (public)")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody BuyerRegistrationRequest request) {
        try {
            buyerService.registerBuyer(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new java.util.HashMap<String, String>() {{
                        put("message", "Buyer registration successful. Your account will be activated after admin approval.");
                        put("status", "success");
                    }});
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new java.util.HashMap<String, String>() {{
                        put("message", e.getMessage());
                        put("status", "error");
                    }});
        }
    }
}
