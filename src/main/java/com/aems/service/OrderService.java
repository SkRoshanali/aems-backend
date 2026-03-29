package com.aems.service;

import com.aems.dto.request.OrderRequest;
import com.aems.entity.Order;
import com.aems.entity.Stock;
import com.aems.entity.User;
import com.aems.repository.OrderRepository;
import com.aems.repository.StockRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
