package com.aems.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@aems.com}")
    private String fromEmail;
    
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
    
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachment));
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email with attachment: " + e.getMessage());
        }
    }
    
    public void sendOrderConfirmationEmail(String buyerEmail, String orderNumber, String buyerName) {
        String subject = "Order Confirmation - " + orderNumber;
        String body = String.format(
            "Dear %s,\n\n" +
            "Thank you for your order!\n\n" +
            "Your order %s has been received and is being processed.\n" +
            "You will receive another email once your order is approved.\n\n" +
            "Best regards,\n" +
            "AEMS Team",
            buyerName, orderNumber
        );
        sendSimpleEmail(buyerEmail, subject, body);
    }
    
    public void sendOrderApprovalEmail(String buyerEmail, String orderNumber, String buyerName) {
        String subject = "Order Approved - " + orderNumber;
        String body = String.format(
            "Dear %s,\n\n" +
            "Great news! Your order %s has been approved.\n" +
            "We will begin processing your shipment shortly.\n\n" +
            "Best regards,\n" +
            "AEMS Team",
            buyerName, orderNumber
        );
        sendSimpleEmail(buyerEmail, subject, body);
    }
    
    public void sendOrderRejectionEmail(String buyerEmail, String orderNumber, String buyerName, String reason) {
        String subject = "Order Update - " + orderNumber;
        String body = String.format(
            "Dear %s,\n\n" +
            "We regret to inform you that your order %s could not be processed.\n\n" +
            "Reason: %s\n\n" +
            "Please contact us if you have any questions.\n\n" +
            "Best regards,\n" +
            "AEMS Team",
            buyerName, orderNumber, reason
        );
        sendSimpleEmail(buyerEmail, subject, body);
    }
    
    public void sendShipmentNotificationEmail(String buyerEmail, String orderNumber, String trackingNumber, String buyerName) {
        String subject = "Your Order Has Been Shipped - " + orderNumber;
        String body = String.format(
            "Dear %s,\n\n" +
            "Your order %s has been shipped!\n\n" +
            "Tracking Number: %s\n\n" +
            "You can track your shipment using the tracking number above.\n\n" +
            "Best regards,\n" +
            "AEMS Team",
            buyerName, orderNumber, trackingNumber
        );
        sendSimpleEmail(buyerEmail, subject, body);
    }
    
    public void sendInvoiceEmail(String buyerEmail, String invoiceNumber, String buyerName, byte[] invoicePdf) {
        String subject = "Invoice - " + invoiceNumber;
        String body = String.format(
            "Dear %s,\n\n" +
            "Please find attached your invoice %s.\n\n" +
            "Thank you for your business!\n\n" +
            "Best regards,\n" +
            "AEMS Team",
            buyerName, invoiceNumber
        );
        sendEmailWithAttachment(buyerEmail, subject, body, invoicePdf, invoiceNumber + ".pdf");
    }
}
