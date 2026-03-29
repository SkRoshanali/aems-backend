package com.aems.controller;

import com.aems.dto.request.OrderRequest;
import com.aems.entity.Order;
import com.aems.service.OrderService;
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
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Place a new order (Buyer only)")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        String email = authentication.getName();
        Order order = orderService.createOrderByEmail(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'BUYER')")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all orders (Admin/Manager/Employee)")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Get my orders (Buyer)")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        String email = authentication.getName();
        List<Order> orders = orderService.getOrdersByBuyerEmail(email);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get pending orders (Admin/Manager/Employee)")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get orders by status (Admin/Manager/Employee)")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Approve an order (Admin/Manager)")
    public ResponseEntity<Order> approveOrder(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Order order = orderService.approveOrderByEmail(id, email);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Reject an order (Admin/Manager)")
    public ResponseEntity<Order> rejectOrder(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        String email = authentication.getName();
        Order order = orderService.rejectOrderByEmail(id, reason, email);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update order status (Admin/Manager)")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }
}
