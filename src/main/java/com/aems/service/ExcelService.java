package com.aems.service;

import com.aems.dto.response.SalesReportResponse;
import com.aems.dto.response.StockReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    public byte[] generateStockReportExcel(List<StockReportResponse> stocks) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Stock ID", "Crop Name", "Category", "Farmer", "Import Source", 
                              "Country", "Quantity", "Unit", "Price/Unit", "Total Value", 
                              "Quality Grade", "Status"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (StockReportResponse stock : stocks) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(stock.getStockId());
                row.createCell(1).setCellValue(stock.getCropName());
                row.createCell(2).setCellValue(stock.getCategory());
                row.createCell(3).setCellValue(stock.getFarmerName());
                row.createCell(4).setCellValue(stock.getImportSourceName());
                row.createCell(5).setCellValue(stock.getCountry());
                row.createCell(6).setCellValue(stock.getQuantity().doubleValue());
                row.createCell(7).setCellValue(stock.getUnit());
                row.createCell(8).setCellValue(stock.getPricePerUnit().doubleValue());
                row.createCell(9).setCellValue(stock.getTotalValue().doubleValue());
                row.createCell(10).setCellValue(stock.getQualityGrade());
                row.createCell(11).setCellValue(stock.getIsActive() ? "Active" : "Inactive");
                
                // Apply data style to all cells
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage(), e);
        }
    }
    
    public byte[] generateSalesReportExcel(List<SalesReportResponse> sales) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Order ID", "Order Number", "Order Date", "Buyer Name", "Buyer Email",
                              "Crop Name", "Quantity", "Unit", "Price/Unit", "Total Amount", 
                              "Status", "Approved Date", "Delivered Date"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (SalesReportResponse sale : sales) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(sale.getOrderId());
                row.createCell(1).setCellValue(sale.getOrderNumber());
                row.createCell(2).setCellValue(sale.getOrderDate() != null ? 
                    sale.getOrderDate().format(DATE_FORMATTER) : "");
                row.createCell(3).setCellValue(sale.getBuyerName());
                row.createCell(4).setCellValue(sale.getBuyerEmail());
                row.createCell(5).setCellValue(sale.getCropName());
                row.createCell(6).setCellValue(sale.getQuantity().doubleValue());
                row.createCell(7).setCellValue(sale.getUnit());
                row.createCell(8).setCellValue(sale.getPricePerUnit().doubleValue());
                row.createCell(9).setCellValue(sale.getTotalAmount().doubleValue());
                row.createCell(10).setCellValue(sale.getStatus());
                row.createCell(11).setCellValue(sale.getApprovedDate() != null ? 
                    sale.getApprovedDate().format(DATE_FORMATTER) : "");
                row.createCell(12).setCellValue(sale.getDeliveredDate() != null ? 
                    sale.getDeliveredDate().format(DATE_FORMATTER) : "");
                
                // Apply data style to all cells
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage(), e);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
