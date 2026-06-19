package com.aems.service;

import com.aems.dto.request.FarmerRequest;
import com.aems.entity.Farmer;
import com.aems.entity.User;
import com.aems.repository.FarmerRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FarmerService {
    
    @Autowired
    private FarmerRepository farmerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Farmer createFarmer(FarmerRequest request, Long userId) {
        // Check for existing farmer ID
        Optional<Farmer> existingFarmer = farmerRepository.findByFarmerId(request.getFarmerId());
        if (existingFarmer.isPresent()) {
            throw new RuntimeException("Farmer with ID " + request.getFarmerId() + " already exists");
        }
        
        // Check for existing email
        Optional<Farmer> existingEmail = farmerRepository.findByEmail(request.getEmail());
        if (existingEmail.isPresent()) {
            throw new RuntimeException("Farmer with email " + request.getEmail() + " already exists");
        }
        
        try {
            Farmer farmer = new Farmer();
            farmer.setFullName(request.getFullName());
            farmer.setFarmerId(request.getFarmerId());
            farmer.setEmail(request.getEmail());
            farmer.setPhoneNumber(request.getPhoneNumber());
            farmer.setAddress(request.getAddress());
            farmer.setCity(request.getCity());
            farmer.setState(request.getState());
            farmer.setZipCode(request.getZipCode());
            farmer.setFarmName(request.getFarmName());
            farmer.setFarmSizeAcres(request.getFarmSizeAcres());
            farmer.setIsActive(true);
            farmer.setIsVerified(false);
            
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                farmer.setCreatedBy(user.get());
            }
            
            Farmer savedFarmer = farmerRepository.save(farmer);
            System.out.println("Farmer saved successfully with ID: " + savedFarmer.getId());
            return savedFarmer;
        } catch (Exception e) {
            System.err.println("Error saving farmer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save farmer: " + e.getMessage());
        }
    }
    
    public Farmer createFarmerByEmail(FarmerRequest request, String userEmail) {
        // Get user by email
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (!user.isPresent()) {
            throw new RuntimeException("User not found with email: " + userEmail);
        }
        
        return createFarmer(request, user.get().getId());
    }
    
    public Farmer updateFarmer(Long id, FarmerRequest request) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + id));
        
        farmer.setFullName(request.getFullName());
        farmer.setEmail(request.getEmail());
        farmer.setPhoneNumber(request.getPhoneNumber());
        farmer.setAddress(request.getAddress());
        farmer.setCity(request.getCity());
        farmer.setState(request.getState());
        farmer.setZipCode(request.getZipCode());
        farmer.setFarmName(request.getFarmName());
        farmer.setFarmSizeAcres(request.getFarmSizeAcres());
        
        return farmerRepository.save(farmer);
    }
    
    public Farmer getFarmerById(Long id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + id));
    }
    
    public Farmer getFarmerByFarmerId(String farmerId) {
        return farmerRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found with Farmer ID: " + farmerId));
    }
    
    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }
    
    public List<Farmer> getActiveFarmers() {
        return farmerRepository.findByIsActive(true);
    }
    
    public List<Farmer> getVerifiedFarmers() {
        return farmerRepository.findByIsVerified(true);
    }
    
    public Farmer verifyFarmer(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + id));
        farmer.setIsVerified(true);
        return farmerRepository.save(farmer);
    }
    
    public Farmer deactivateFarmer(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + id));
        farmer.setIsActive(false);
        return farmerRepository.save(farmer);
    }
    
    public void deleteFarmer(Long id) {
        if (!farmerRepository.existsById(id)) {
            throw new RuntimeException("Farmer not found with ID: " + id);
        }
        farmerRepository.deleteById(id);
    }
}
