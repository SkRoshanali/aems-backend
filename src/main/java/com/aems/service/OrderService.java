package com.aems.service;

import com.aems.dto.request.OrderRequest;
import com.aems.entity.Order;
import com.aems.entity.Stock;
import com.aems.entity.User;
import com.aems.repository.OrderRepository;
import com.aems.repository.StockRepository;
import com.aems.repository.UserRepository;
import com.aems.rag.client.RagIngestionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RagIngestionClient ragClient;
    
    public Order createOrder(OrderRequest request, Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found with ID: " + buyerId));
        
        if (buyer.getRole() != User.Role.BUYER) {
            throw new RuntimeException("Only buyers can place orders");
        }
        
        if (!buyer.getIsActive() || !buyer.getIsVerified()) {
            throw new RuntimeException("Buyer account is not active or verified");
        }
        
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + request.getStockId()));
        
        if (!stock.getIsActive()) {
            throw new RuntimeException("Stock is not available");
        }
        
        if (stock.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient stock quantity");
        }
        
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setBuyer(buyer);
        order.setStock(stock);
        order.setQuantity(request.getQuantity());
        order.setUnit(stock.getUnit());
        order.setPricePerUnit(stock.getPricePerUnit());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setDeliveryState(request.getDeliveryState());
        order.setDeliveryZip(request.getDeliveryZip());
        order.setDeliveryCountry(request.getDeliveryCountry());
        order.setNotes(request.getNotes());
        order.setStatus(Order.OrderStatus.PENDING);
        
        Order savedOrder = orderRepository.save(order);
        
        // RAG INGESTION: Push order creation event
        String ragContent = String.format(
            "Order #%s: Buyer %s placed order for %s %s of %s at $%s per unit. " +
            "Total amount: $%s. Delivery to: %s, %s. Status: Pending approval.",
            savedOrder.getOrderNumber(),
            buyer.getFullName(),
            savedOrder.getQuantity(),
            savedOrder.getUnit(),
            stock.getCrop().getCropName(),
            savedOrder.getPricePerUnit(),
            savedOrder.getTotalAmount(),
            savedOrder.getDeliveryCity(),
            savedOrder.getDeliveryCountry()
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("visibility", "buyer:" + buyer.getId());
        metadata.put("visibility_secondary", "management");
        metadata.put("buyer_id", buyer.getId().toString());
        metadata.put("order_id", savedOrder.getId().toString());
        metadata.put("event_type", "order_placed");
        metadata.put("status", "pending");
        
        ragClient.ingestDocument(ragContent, metadata);
        
        // Send confirmation email
        emailService.sendOrderConfirmationEmail(
            buyer.getEmail(),
            savedOrder.getOrderNumber(),
            buyer.getFullName()
        );
        
        return savedOrder;
    }
    
    public Order approveOrder(Long orderId, Long approverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be approved");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + approverId));
        
        // Update stock quantity
        Stock stock = order.getStock();
        if (stock.getQuantity().compareTo(order.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient stock quantity available");
        }
        stock.setQuantity(stock.getQuantity().subtract(order.getQuantity()));
        stockRepository.save(stock);
        
        order.setStatus(Order.OrderStatus.APPROVED);
        order.setApprovedBy(approver);
        order.setApprovedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        // RAG INGESTION: Push order approval event
        String ragContent = String.format(
            "Order #%s approved by %s on %s. Order for %s %s of %s. " +
            "Buyer: %s. Total amount: $%s. Stock deducted. Ready for shipment.",
            savedOrder.getOrderNumber(),
            approver.getFullName(),
            LocalDateTime.now(),
            savedOrder.getQuantity(),
            savedOrder.getUnit(),
            savedOrder.getStock().getCrop().getCropName(),
            savedOrder.getBuyer().getFullName(),
            savedOrder.getTotalAmount()
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("visibility", "buyer:" + order.getBuyer().getId());
        metadata.put("visibility_secondary", "management");
        metadata.put("buyer_id", order.getBuyer().getId().toString());
        metadata.put("order_id", savedOrder.getId().toString());
        metadata.put("event_type", "order_approved");
        metadata.put("status", "approved");
        
        ragClient.ingestDocument(ragContent, metadata);
        
        // Send approval email
        emailService.sendOrderApprovalEmail(
            order.getBuyer().getEmail(),
            order.getOrderNumber(),
            order.getBuyer().getFullName()
        );
        
        return savedOrder;
    }
    
    public Order rejectOrder(Long orderId, String reason, Long approverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be rejected");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + approverId));
        
        order.setStatus(Order.OrderStatus.REJECTED);
        order.setRejectionReason(reason);
        order.setApprovedBy(approver);
        order.setApprovedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        // Send rejection email
        emailService.sendOrderRejectionEmail(
            order.getBuyer().getEmail(),
            order.getOrderNumber(),
            order.getBuyer().getFullName(),
            reason
        );
        
        return savedOrder;
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        order.setStatus(status);
        
        // Set deliveredAt timestamp when order is marked as delivered
        if (status == Order.OrderStatus.DELIVERED && order.getDeliveredAt() == null) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public List<Order> getOrdersByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }
    
    // Email-based methods for JWT authentication
    public Order createOrderByEmail(OrderRequest request, String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Buyer not found with email: " + email));
        return createOrder(request, buyer.getId());
    }
    
    public Order approveOrderByEmail(Long orderId, String email) {
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Approver not found with email: " + email));
        return approveOrder(orderId, approver.getId());
    }
    
    public Order rejectOrderByEmail(Long orderId, String reason, String email) {
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Approver not found with email: " + email));
        return rejectOrder(orderId, reason, approver.getId());
    }
    
    public List<Order> getOrdersByBuyerEmail(String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Buyer not found with email: " + email));
        return getOrdersByBuyer(buyer.getId());
    }
}
