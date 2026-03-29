package com.aems.controller;

import com.aems.entity.Invoice;
import com.aems.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice Management", description = "APIs for managing invoices")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @PostMapping("/generate/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Generate invoice for order")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Long orderId, Authentication authentication) {
        String email = authentication.getName();
        Invoice invoice = invoiceService.generateInvoiceByEmail(orderId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }
    
    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Send invoice to buyer via email")
    public ResponseEntity<Invoice> sendInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.sendInvoice(id);
        return ResponseEntity.ok(invoice);
    }
    
    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Mark invoice as paid")
    public ResponseEntity<Invoice> markAsPaid(@PathVariable Long id) {
        Invoice invoice = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'BUYER')")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'BUYER')")
    @Operation(summary = "Download invoice PDF")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.downloadInvoicePdf(id);
        Invoice invoice = invoiceService.getInvoiceById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", invoice.getInvoiceNumber() + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'BUYER')")
    @Operation(summary = "Get invoice by order ID")
    public ResponseEntity<Invoice> getInvoiceByOrderId(@PathVariable Long orderId) {
        Invoice invoice = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get invoices by status")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(@PathVariable Invoice.InvoiceStatus status) {
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }
}
