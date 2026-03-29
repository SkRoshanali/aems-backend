package com.aems.service;

import com.aems.dto.response.StockPublicResponse;
import com.aems.entity.Stock;
import com.aems.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicService {
    
    @Autowired
    private StockRepository stockRepository;
    
    public List<StockPublicResponse> getAvailableStocks() {
        List<Stock> stocks = stockRepository.findByIsActive(true);
        return stocks.stream()
                .map(this::convertToPublicResponse)
                .collect(Collectors.toList());
    }
    
    public StockPublicResponse getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + id));
        
        if (!stock.getIsActive()) {
            throw new RuntimeException("Stock is not available");
        }
        
        return convertToPublicResponse(stock);
    }
    
    private StockPublicResponse convertToPublicResponse(Stock stock) {
        StockPublicResponse response = new StockPublicResponse();
        response.setId(stock.getId());
        response.setCropName(stock.getCrop().getCropName());
        response.setCropCode(stock.getCrop().getCropCode());
        response.setCategory(stock.getCrop().getCategory());
        response.setQuantity(stock.getQuantity());
        response.setUnit(stock.getUnit());
        response.setPricePerUnit(stock.getPricePerUnit());
        response.setQuality(stock.getQuality());
        return response;
    }
}
