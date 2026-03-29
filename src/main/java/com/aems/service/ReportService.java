package com.aems.service;

import com.aems.dto.response.*;
import com.aems.entity.*;
import com.aems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private FarmerRepository farmerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    public List<StockReportResponse> getStockReport(Boolean activeOnly) {
        List<Stock> stocks;
        
        if (activeOnly != null && activeOnly) {
            stocks = stockRepository.findByIsActive(true);
        } else {
            stocks = stockRepository.findAll();
        }
        
        return stocks.stream()
            .map(this::mapToStockReportResponse)
            .collect(Collectors.toList());
    }
    
    public List<SalesReportResponse> getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findAll();
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            orders = orders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startDate) && 
                               order.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
        }
        
        return orders.stream()
            .map(this::mapToSalesReportResponse)
            .collect(Collectors.toList());
    }
    
    public RevenueReportResponse getRevenueReport(Integer year, Integer month) {
        List<Order> orders = orderRepository.findAll();
        
        // Filter by year and month if provided
        if (year != null && month != null) {
            orders = orders.stream()
                .filter(order -> {
                    LocalDateTime createdAt = order.getCreatedAt();
                    return createdAt.getYear() == year && createdAt.getMonthValue() == month;
                })
                .collect(Collectors.toList());
        }
        
        // Calculate statistics
        Integer totalOrders = orders.size();
        Integer completedOrders = (int) orders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        
        BigDecimal totalRevenue = orders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED || 
                           order.getStatus() == Order.OrderStatus.SHIPPED)
            .map(order -> order.getPricePerUnit().multiply(order.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalTax = totalRevenue.multiply(new BigDecimal("0.18"))
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal netRevenue = totalRevenue.subtract(totalTax);
        
        BigDecimal averageOrderValue = completedOrders > 0 ? 
            totalRevenue.divide(new BigDecimal(completedOrders), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Find top crop
        String topCrop = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getStock().getCrop().getCropName(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(e -> e.getKey())
            .orElse("N/A");
        
        // Find top buyer
        String topBuyer = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getBuyer().getFullName(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(e -> e.getKey())
            .orElse("N/A");
        
        String period = year != null && month != null ? 
            YearMonth.of(year, month).toString() : "All Time";
        
        return new RevenueReportResponse(
            period, totalOrders, completedOrders, totalRevenue, totalTax, 
            netRevenue, averageOrderValue, topCrop, topBuyer
        );
    }
    
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        
        // Stock statistics
        stats.setTotalStocks((int) stockRepository.count());
        stats.setActiveStocks(stockRepository.findByIsActive(true).size());
        
        // Order statistics
        List<Order> allOrders = orderRepository.findAll();
        stats.setTotalOrders(allOrders.size());
        stats.setPendingOrders((int) allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
            .count());
        stats.setApprovedOrders((int) allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.APPROVED)
            .count());
        stats.setCompletedOrders((int) allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count());
        
        // Revenue statistics
        BigDecimal totalRevenue = allOrders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED || 
                           order.getStatus() == Order.OrderStatus.SHIPPED)
            .map(order -> order.getPricePerUnit().multiply(order.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue);
        
        // Monthly revenue (current month)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyRevenue = allOrders.stream()
            .filter(order -> order.getCreatedAt().isAfter(startOfMonth))
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED || 
                           order.getStatus() == Order.OrderStatus.SHIPPED)
            .map(order -> order.getPricePerUnit().multiply(order.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthlyRevenue(monthlyRevenue);
        
        // Farmer statistics
        List<Farmer> allFarmers = farmerRepository.findAll();
        stats.setTotalFarmers(allFarmers.size());
        stats.setVerifiedFarmers((int) allFarmers.stream()
            .filter(Farmer::getIsVerified)
            .count());
        
        // Buyer statistics
        List<User> buyers = userRepository.findByRole(User.Role.BUYER);
        stats.setTotalBuyers(buyers.size());
        stats.setActiveBuyers((int) buyers.stream()
            .filter(User::getIsActive)
            .count());
        
        // Shipment statistics
        List<Shipment> allShipments = shipmentRepository.findAll();
        stats.setTotalShipments(allShipments.size());
        stats.setInTransitShipments((int) allShipments.stream()
            .filter(s -> s.getStatus() == Shipment.ShipmentStatus.IN_TRANSIT || 
                        s.getStatus() == Shipment.ShipmentStatus.OUT_FOR_DELIVERY)
            .count());
        
        // Invoice statistics
        List<Invoice> allInvoices = invoiceRepository.findAll();
        stats.setTotalInvoices(allInvoices.size());
        stats.setPaidInvoices((int) allInvoices.stream()
            .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAID)
            .count());
        stats.setOverdueInvoices((int) allInvoices.stream()
            .filter(i -> i.getStatus() == Invoice.InvoiceStatus.OVERDUE)
            .count());
        
        return stats;
    }
    
    private StockReportResponse mapToStockReportResponse(Stock stock) {
        BigDecimal totalValue = stock.getPricePerUnit().multiply(stock.getQuantity());
        
        return new StockReportResponse(
            stock.getId(),
            stock.getCrop().getCropName(),
            stock.getCrop().getCategory(),
            stock.getFarmer() != null ? stock.getFarmer().getFullName() : "N/A",
            stock.getImportSource() != null ? stock.getImportSource().getCompanyName() : "N/A",
            stock.getImportSource() != null ? stock.getImportSource().getCountry() : "N/A",
            stock.getQuantity(),
            stock.getUnit(),
            stock.getPricePerUnit(),
            totalValue,
            stock.getIsActive(),
            stock.getQuality() != null ? stock.getQuality() : "N/A"
        );
    }
    
    private SalesReportResponse mapToSalesReportResponse(Order order) {
        BigDecimal totalAmount = order.getPricePerUnit().multiply(order.getQuantity());
        
        return new SalesReportResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCreatedAt(),
            order.getBuyer().getFullName(),
            order.getBuyer().getEmail(),
            order.getStock().getCrop().getCropName(),
            order.getQuantity(),
            order.getUnit(),
            order.getPricePerUnit(),
            totalAmount,
            order.getStatus().toString(),
            order.getApprovedAt(),
            order.getDeliveredAt()
        );
    }
}
