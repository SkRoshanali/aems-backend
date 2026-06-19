package com.aems.controller;

import com.aems.rag.client.RagIngestionClient;
import com.aems.repository.UserRepository;
import com.aems.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for RAG (Retrieval-Augmented Generation) chat queries
 * 
 * SECURITY: Always extracts role from JWT (server-verified), never trusts client input
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "RAG Chat", description = "AI-powered chat with role-based knowledge")
@SecurityRequirement(name = "Bearer Authentication")
public class RagController {
    
    @Autowired
    private RagIngestionClient ragClient;

    @Autowired
    private UserRepository userRepository;
    
    /**
     * Query RAG system with role-based filtering
     * 
     * SECURITY NOTES:
     * - Role extracted from JWT (cannot be spoofed by client)
     * - Request body only contains query, NOT role
     * - Python RAG service receives server-verified role
     * 
     * @param request Contains only the user's query
     * @param authentication Spring Security authentication (contains verified JWT)
     * @return AI-generated response with sources
     */
    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'BUYER')")
    @Operation(summary = "Ask AI assistant a question (all authenticated users)")
    public ResponseEntity<Map<String, Object>> query(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String query = request.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Query cannot be empty")
            );
        }
        
        // Extract role from JWT (server-verified, cannot be spoofed)
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(auth -> auth.replace("ROLE_", ""))  // Remove ROLE_ prefix if present
                .orElseThrow(() -> new RuntimeException("No role found in JWT"));
        
        // Get user email from JWT
        String userEmail = authentication.getName();
        
        // Extract buyer-specific info if role is BUYER
        String buyerId = null;
        String buyerStatus = "ACCEPTED";  // Default, can be enhanced with actual status from DB
        
        if ("BUYER".equals(role)) {
            User buyer = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Buyer not found"));
            buyerId = buyer.getId().toString();
            buyerStatus = Boolean.TRUE.equals(buyer.getIsActive()) && Boolean.TRUE.equals(buyer.getIsVerified())
                    ? "ACCEPTED"
                    : "PENDING";
        }
        
        try {
            // Forward to Python RAG service with server-verified role
            Map<String, Object> response = ragClient.queryRag(
                query,
                role,
                buyerId,
                buyerStatus
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Log the error (use proper logging in production)
            System.err.println("RAG query failed: " + e.getMessage());
            
            // Return user-friendly error
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process your question. Please try again.");
            errorResponse.put("answer", "I apologize, but I'm experiencing technical difficulties. Please try again in a moment.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check for chat functionality
     */
    @GetMapping("/health")
    @Operation(summary = "Check if chat service is available")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "rag-chat",
            "message", "Chat service is operational"
        ));
    }
    
    /**
     * Warmup endpoint - wakes both Spring Boot AND Python RAG service
     * 
     * This endpoint:
     * 1. Wakes up Spring Boot (by receiving the request)
     * 2. Pings Python RAG service to wake it up too
     * 3. Returns quickly even if Python is still waking
     * 
     * Call this on frontend app load to reduce first-chat latency
     */
    @GetMapping("/warmup")
    @Operation(summary = "Wake up both Spring Boot and Python RAG services (no auth required)")
    public ResponseEntity<Map<String, String>> warmup() {
        // Async ping to Python service (don't wait for response)
        try {
            // This will wake Python service even if it times out
            ragClient.healthCheck();  // Simple GET to Python /health
        } catch (Exception e) {
            // Ignore errors - the HTTP request itself wakes the service
            System.out.println("Python service wake-up ping sent");
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "awake",
            "message", "Both services pinged"
        ));
    }
}
