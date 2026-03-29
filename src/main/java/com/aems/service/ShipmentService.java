package com.aems.service;

import com.aems.dto.request.ShipmentRequest;
import com.aems.entity.Order;
import com.aems.entity.Shipment;
import com.aems.entity.User;
import com.aems.repository.OrderRepository;
import com.aems.repository.ShipmentRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ShipmentService {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public Shipment createShipment(ShipmentRequest request, Long userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));
        
        if (order.getStatus() != Order.OrderStatus.APPROVED) {
            throw new RuntimeException("Only approved orders can be shipped");
        }
        
        Optional<Shipment> existingShipment = shipmentRepository.findByOrderId(order.getId());
        if (existingShipment.isPresent()) {
            throw new RuntimeException("Shipment already exists for this order");
        }
        
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setCarrier(request.getCarrier());
        shipment.setShippedDate(LocalDateTime.now());
        shipment.setEstimatedDelivery(request.getEstimatedDelivery());
        shipment.setNotes(request.getNotes());
        shipment.setStatus(Shipment.ShipmentStatus.SHIPPED);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            shipment.setCreatedBy(user.get());
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(order);
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Send email notification
        emailService.sendShipmentNotificationEmail(
            order.getBuyer().getEmail(),
            order.getOrderNumber(),
            savedShipment.getTrackingNumber(),
            order.getBuyer().getFullName()
        );
        
        return savedShipment;
    }
    
    public Shipment createShipmentByEmail(ShipmentRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return createShipment(request, user.getId());
    }
    
    public Shipment updateShipmentStatus(Long id, Shipment.ShipmentStatus status) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + id));
        
        shipment.setStatus(status);
        
        if (status == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDateTime.now());
            // Update order status and set delivered timestamp
            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        return shipmentRepository.save(shipment);
    }
    
    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + id));
    }
    
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found with tracking number: " + trackingNumber));
    }
    
    public Shipment getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Shipment not found for order ID: " + orderId));
    }
    
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }
    
    public List<Shipment> getShipmentsByStatus(Shipment.ShipmentStatus status) {
        return shipmentRepository.findByStatus(status);
    }
}
