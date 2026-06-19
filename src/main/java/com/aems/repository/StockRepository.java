package com.aems.repository;

import com.aems.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByCropId(Long cropId);
    List<Stock> findByFarmerId(Long farmerId);
    List<Stock> findByImportSourceId(Long importSourceId);
    List<Stock> findByIsActive(Boolean isActive);
}
