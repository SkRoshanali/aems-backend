package com.aems.service;

import com.aems.dto.request.StockRequest;
import com.aems.entity.Stock;
import com.aems.entity.Crop;
import com.aems.entity.Farmer;
import com.aems.entity.ImportSource;
import com.aems.entity.User;
import com.aems.repository.StockRepository;
import com.aems.repository.CropRepository;
import com.aems.repository.FarmerRepository;
import com.aems.repository.ImportSourceRepository;
import com.aems.repository.UserRepository;
import com.aems.rag.client.RagIngestionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class StockService {
    
    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private CropRepository cropRepository;
    
    @Autowired
    private FarmerRepository farmerRepository;
    
    @Autowired
    private ImportSourceRepository importSourceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RagIngestionClient ragClient;
    
    public Stock createStock(StockRequest request, Long userId) {
        Crop crop = cropRepository.findById(request.getCropId())
                .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + request.getCropId()));
        
        Stock stock = new Stock();
        stock.setCrop(crop);
        
        if (request.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(request.getFarmerId())
                    .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + request.getFarmerId()));
            stock.setFarmer(farmer);
        }
        
        if (request.getImportSourceId() != null) {
            ImportSource importSource = importSourceRepository.findById(request.getImportSourceId())
                    .orElseThrow(() -> new RuntimeException("Import source not found with ID: " + request.getImportSourceId()));
            stock.setImportSource(importSource);
        }
        
        stock.setQuantity(request.getQuantity());
        stock.setUnit(request.getUnit());
        stock.setPricePerUnit(request.getPricePerUnit());
        stock.setBatchNumber(request.getBatchNumber());
        stock.setQuality(request.getQuality());
        stock.setRemarks(request.getRemarks());
        stock.setIsActive(true);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            stock.setCreatedBy(user.get());
        }
        
        Stock saved = stockRepository.save(stock);
        
        // RAG INGESTION: Push stock creation event
        String source = "";
        if (saved.getFarmer() != null) {
            source = "from farmer " + saved.getFarmer().getFullName();
        } else if (saved.getImportSource() != null) {
            source = "imported from " + saved.getImportSource().getCompanyName();
        }
        
        String ragContent = String.format(
            "Stock batch %s: %s %s of %s available. Quality grade: %s. Price: $%s per %s. %s. " +
            "Status: Active and available for order.",
            saved.getBatchNumber() != null ? saved.getBatchNumber() : saved.getId(),
            saved.getQuantity(),
            saved.getUnit(),
            saved.getCrop().getCropName(),
            saved.getQuality() != null ? saved.getQuality() : "Standard",
            saved.getPricePerUnit(),
            saved.getUnit(),
            source
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("visibility", "public");
        metadata.put("stock_id", saved.getId().toString());
        metadata.put("crop_name", saved.getCrop().getCropName());
        metadata.put("event_type", "stock_created");
        metadata.put("status", "active");
        
        ragClient.ingestDocument(ragContent, metadata);
        
        return saved;
    }
    
    public Stock createStockByEmail(StockRequest request, String userEmail) {
        // Get user by email
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (!user.isPresent()) {
            throw new RuntimeException("User not found with email: " + userEmail);
        }
        
        return createStock(request, user.get().getId());
    }
    
    public Stock updateStock(Long id, StockRequest request) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + id));
        
        if (request.getCropId() != null) {
            Crop crop = cropRepository.findById(request.getCropId())
                    .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + request.getCropId()));
            stock.setCrop(crop);
        }
        
        stock.setQuantity(request.getQuantity());
        stock.setUnit(request.getUnit());
        stock.setPricePerUnit(request.getPricePerUnit());
        stock.setQuality(request.getQuality());
        stock.setRemarks(request.getRemarks());
        
        return stockRepository.save(stock);
    }
    
    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + id));
    }
    
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
    
    public List<Stock> getStockByCrop(Long cropId) {
        return stockRepository.findByCropId(cropId);
    }
    
    public List<Stock> getStockByFarmer(Long farmerId) {
        return stockRepository.findByFarmerId(farmerId);
    }
    
    public List<Stock> getStockByImportSource(Long importSourceId) {
        return stockRepository.findByImportSourceId(importSourceId);
    }
    
    public List<Stock> getActiveStocks() {
        return stockRepository.findByIsActive(true);
    }
    
    public Stock deactivateStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + id));
        stock.setIsActive(false);
        return stockRepository.save(stock);
    }
    
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new RuntimeException("Stock not found with ID: " + id);
        }
        stockRepository.deleteById(id);
    }
}
