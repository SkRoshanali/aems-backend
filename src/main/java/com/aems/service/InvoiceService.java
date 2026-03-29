package com.aems.service;

import com.aems.entity.Invoice;
import com.aems.entity.Order;
import com.aems.entity.User;
import com.aems.repository.InvoiceRepository;
import com.aems.repository.OrderRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PdfService pdfService;
    
    @Autowired
    private EmailService emailService;
    
    private static final BigDecimal DEFAULT_TAX_PERCENTAGE = new BigDecimal("18.00"); // 18% GST
    
    public Invoice generateInvoice(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.APPROVED && order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new RuntimeException("Invoice can only be generated for approved or shipped orders");
        }
        
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrderId(order.getId());
        if (existingInvoice.isPresent()) {
            throw new RuntimeException("Invoice already exists for this order");
        }
        
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(30)); // 30 days payment term
        
        // Calculate amounts
        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal taxAmount = subtotal.multiply(DEFAULT_TAX_PERCENTAGE).divide(new BigDecimal("100"));
        BigDecimal totalAmount = subtotal.add(taxAmount);
        
        invoice.setSubtotal(subtotal);
        invoice.setTaxPercentage(DEFAULT_TAX_PERCENTAGE);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            invoice.setCreatedBy(user.get());
        }
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Generate PDF
        try {
            byte[] pdfBytes = pdfService.generateInvoicePdf(savedInvoice);
            String pdfPath = pdfService.savePdfToFile(pdfBytes, savedInvoice.getInvoiceNumber() + ".pdf");
            savedInvoice.setPdfPath(pdfPath);
            savedInvoice = invoiceRepository.save(savedInvoice);
        } catch (Exception e) {
            System.err.println("Failed to generate PDF: " + e.getMessage());
        }
        
        return savedInvoice;
    }
    
    public Invoice generateInvoiceByEmail(Long orderId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return generateInvoice(orderId, user.getId());
    }
    
    public Invoice sendInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
        
        invoice.setStatus(Invoice.InvoiceStatus.SENT);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Send email with PDF
        try {
            byte[] pdfBytes = pdfService.generateInvoicePdf(savedInvoice);
            emailService.sendInvoiceEmail(
                invoice.getOrder().getBuyer().getEmail(),
                invoice.getInvoiceNumber(),
                invoice.getOrder().getBuyer().getFullName(),
                pdfBytes
            );
        } catch (Exception e) {
            System.err.println("Failed to send invoice email: " + e.getMessage());
        }
        
        return savedInvoice;
    }
    
    public Invoice markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
        
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaymentDate(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }
    
    public byte[] downloadInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
        
        return pdfService.generateInvoicePdf(invoice);
    }
    
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }
    
    public Invoice getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for order ID: " + orderId));
    }
    
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    public List<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }
}
