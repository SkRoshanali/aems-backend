package com.aems.service;

import com.aems.entity.Invoice;
import com.aems.entity.Order;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            Order order = invoice.getOrder();
            
            // Title
            Paragraph title = new Paragraph("INVOICE")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            document.add(new Paragraph("\n"));
            
            // Invoice Details
            document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()).setBold());
            document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate().format(DATE_FORMATTER)));
            document.add(new Paragraph("Order Number: " + order.getOrderNumber()));
            
            if (invoice.getDueDate() != null) {
                document.add(new Paragraph("Due Date: " + invoice.getDueDate().format(DATE_FORMATTER)));
            }
            
            document.add(new Paragraph("\n"));
            
            // Buyer Details
            document.add(new Paragraph("BILL TO:").setBold());
            document.add(new Paragraph(order.getBuyer().getFullName()));
            document.add(new Paragraph(order.getBuyer().getEmail()));
            
            document.add(new Paragraph("\n"));
            
            // Delivery Address
            document.add(new Paragraph("DELIVERY ADDRESS:").setBold());
            document.add(new Paragraph(order.getDeliveryAddress()));
            document.add(new Paragraph(order.getDeliveryCity() + ", " + order.getDeliveryState() + " " + order.getDeliveryZip()));
            document.add(new Paragraph(order.getDeliveryCountry()));
            
            document.add(new Paragraph("\n"));
            
            // Items Table
            float[] columnWidths = {4, 2, 2, 2};
            Table table = new Table(columnWidths);
            table.setWidth(500);
            
            // Table Headers
            table.addHeaderCell("Item");
            table.addHeaderCell("Quantity");
            table.addHeaderCell("Price");
            table.addHeaderCell("Total");
            
            // Table Data
            table.addCell(order.getStock().getCrop().getCropName());
            table.addCell(order.getQuantity() + " " + order.getUnit());
            table.addCell("$" + order.getPricePerUnit());
            table.addCell("$" + invoice.getSubtotal());
            
            document.add(table);
            
            document.add(new Paragraph("\n"));
            
            // Totals
            document.add(new Paragraph("Subtotal: $" + invoice.getSubtotal()).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Tax (" + invoice.getTaxPercentage() + "%): $" + invoice.getTaxAmount()).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("TOTAL: $" + invoice.getTotalAmount()).setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT));
            
            document.add(new Paragraph("\n"));
            
            // Notes
            if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                document.add(new Paragraph("Notes:").setBold());
                document.add(new Paragraph(invoice.getNotes()));
            }
            
            document.add(new Paragraph("\n"));
            
            // Footer
            document.add(new Paragraph("Thank you for your business!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());
            
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
    
    public String savePdfToFile(byte[] pdfBytes, String fileName) {
        try {
            String filePath = "invoices/" + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(pdfBytes);
            fos.close();
            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Error saving PDF file: " + e.getMessage(), e);
        }
    }
}
