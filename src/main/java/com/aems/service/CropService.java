package com.aems.service;

import com.aems.dto.request.CropRequest;
import com.aems.entity.Crop;
import com.aems.entity.User;
import com.aems.repository.CropRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CropService {
    
    @Autowired
    private CropRepository cropRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Crop createCrop(CropRequest request, Long userId) {
        Optional<Crop> existingCrop = cropRepository.findByCropCode(request.getCropCode());
        if (existingCrop.isPresent()) {
            throw new RuntimeException("Crop with code " + request.getCropCode() + " already exists");
        }
        
        Crop crop = new Crop();
        crop.setCropName(request.getCropName());
        crop.setCropCode(request.getCropCode());
        crop.setDescription(request.getDescription());
        crop.setCategory(request.getCategory());
        crop.setIsActive(true);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            crop.setCreatedBy(user.get());
        }
        
        return cropRepository.save(crop);
    }
    
    public Crop updateCrop(Long id, CropRequest request) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + id));
        
        crop.setCropName(request.getCropName());
        crop.setDescription(request.getDescription());
        crop.setCategory(request.getCategory());
        
        return cropRepository.save(crop);
    }
    
    public Crop getCropById(Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + id));
    }
    
    public Crop getCropByCode(String cropCode) {
        return cropRepository.findByCropCode(cropCode)
                .orElseThrow(() -> new RuntimeException("Crop not found with code: " + cropCode));
    }
    
    public List<Crop> getAllCrops() {
        return cropRepository.findAll();
    }
    
    public List<Crop> getActiveCrops() {
        return cropRepository.findByIsActive(true);
    }
    
    public List<Crop> getCropsByCategory(String category) {
        return cropRepository.findByCategory(category);
    }
    
    public Crop deactivateCrop(Long id) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found with ID: " + id));
        crop.setIsActive(false);
        return cropRepository.save(crop);
    }
    
    public void deleteCrop(Long id) {
        if (!cropRepository.existsById(id)) {
            throw new RuntimeException("Crop not found with ID: " + id);
        }
        cropRepository.deleteById(id);
    }
}
