package com.aems.service;

import com.aems.dto.request.BuyerRegistrationRequest;
import com.aems.entity.User;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BuyerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerBuyer(BuyerRegistrationRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        User buyer = new User();
        buyer.setFullName(request.getFullName());
        buyer.setEmail(request.getEmail());
        buyer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        buyer.setRole(User.Role.BUYER);
        buyer.setIsActive(false); // Requires admin approval
        buyer.setIsVerified(false);
        buyer.setFailedAttempts(0);
        
        // Set buyer-specific fields
        buyer.setCompanyName(request.getCompanyName());
        buyer.setPhoneNumber(request.getPhoneNumber());
        buyer.setAddress(request.getAddress());
        buyer.setCity(request.getCity());
        buyer.setState(request.getState());
        buyer.setZipCode(request.getZipCode());
        buyer.setCountry(request.getCountry());
        
        return userRepository.save(buyer);
    }
    
    public User approveBuyer(Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found with ID: " + buyerId));
        
        if (buyer.getRole() != User.Role.BUYER) {
            throw new RuntimeException("User is not a buyer");
        }
        
        buyer.setIsActive(true);
        buyer.setIsVerified(true);
        return userRepository.save(buyer);
    }
    
    public User rejectBuyer(Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found with ID: " + buyerId));
        
        if (buyer.getRole() != User.Role.BUYER) {
            throw new RuntimeException("User is not a buyer");
        }
        
        buyer.setIsActive(false);
        buyer.setIsVerified(false);
        buyer.setRejectedAt(java.time.LocalDateTime.now()); // Mark as rejected
        return userRepository.save(buyer);
    }
    
    public java.util.List<User> getPendingBuyers() {
        // Get buyers who are not active, not verified, and not rejected
        return userRepository.findByRoleAndIsActiveAndIsVerifiedAndRejectedAtIsNull(
            User.Role.BUYER, false, false
        );
    }
}
