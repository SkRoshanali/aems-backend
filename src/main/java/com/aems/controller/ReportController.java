package com.aems.controller;

import com.aems.dto.response.*;
import com.aems.service.ExcelService;
import com.aems.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report Management", description = "APIs for generating reports and analytics")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private ExcelService excelService;
    
    @GetMapping("/stocks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get stock report", description = "Get detailed stock report (Admin/Manager/Employee)")
    public ResponseEntity<List<StockReportResponse>> getStockReport(
            @RequestParam(required = false) Boolean activeOnly) {
        List<StockReportResponse> report = reportService.getStockReport(activeOnly);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/stocks/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Export stock report to Excel", description = "Download stock report as Excel file (Admin/Manager)")
    public ResponseEntity<byte[]> exportStockReportToExcel(
            @RequestParam(required = false) Boolean activeOnly) {
        List<StockReportResponse> report = reportService.getStockReport(activeOnly);
        byte[] excelBytes = excelService.generateStockReportExcel(report);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "stock-report.xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
    }
    
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get sales report", description = "Get detailed sales report with date range filter (Admin/Manager/Employee)")
    public ResponseEntity<List<SalesReportResponse>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SalesReportResponse> report = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/sales/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Export sales report to Excel", description = "Download sales report as Excel file (Admin/Manager)")
    public ResponseEntity<byte[]> exportSalesReportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SalesReportResponse> report = reportService.getSalesReport(startDate, endDate);
        byte[] excelBytes = excelService.generateSalesReportExcel(report);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "sales-report.xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
    }
    
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get revenue report", description = "Get revenue analysis report with optional year/month filter (Admin/Manager/Employee)")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        RevenueReportResponse report = reportService.getRevenueReport(year, month);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get dashboard statistics", description = "Get comprehensive dashboard statistics (Admin/Manager/Employee)")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = reportService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
