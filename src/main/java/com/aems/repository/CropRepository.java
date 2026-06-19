package com.aems.repository;

import com.aems.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    Optional<Crop> findByCropCode(String cropCode);
    boolean existsByCropCode(String cropCode);
    List<Crop> findByIsActive(Boolean isActive);
    List<Crop> findByCategory(String category);
}
