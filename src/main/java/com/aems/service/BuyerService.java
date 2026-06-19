package com.aems.service;

import com.aems.dto.request.BuyerRegistrationRequest;
import com.aems.entity.User;
import com.aems.repository.UserRepository;
import com.aems.rag.client.RagIngestionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BuyerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RagIngestionClient ragClient;
    
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
        
        User saved = userRepository.save(buyer);
        
        // RAG INGESTION: Push buyer application event to knowledge base
        String ragContent = String.format(
            "Buyer %s submitted application from %s, %s. Company: %s. " +
            "Contact: %s. Status: Pending approval. Application submitted on %s.",
            saved.getFullName(),
            saved.getCity(),
            saved.getCountry(),
            saved.getCompanyName() != null ? saved.getCompanyName() : "Individual",
            saved.getEmail(),
            saved.getCreatedAt()
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("visibility", "management");
        metadata.put("buyer_id", saved.getId().toString());
        metadata.put("event_type", "buyer_application");
        metadata.put("status", "pending");
        
        ragClient.ingestDocument(ragContent, metadata);
        
        return saved;
    }
    
    public User approveBuyer(Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found with ID: " + buyerId));
        
        if (buyer.getRole() != User.Role.BUYER) {
            throw new RuntimeException("User is not a buyer");
        }
        
        buyer.setIsActive(true);
        buyer.setIsVerified(true);
        User approved = userRepository.save(buyer);
        
        // RAG INGESTION: Push buyer approval event
        String ragContent = String.format(
            "Buyer %s (Company: %s) approved on %s. Account activated. " +
            "Location: %s, %s. Contact: %s. Buyer ID: %d",
            approved.getFullName(),
            approved.getCompanyName() != null ? approved.getCompanyName() : "Individual",
            java.time.LocalDateTime.now(),
            approved.getCity(),
            approved.getCountry(),
            approved.getEmail(),
            approved.getId()
        );
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("visibility", "buyer:" + approved.getId());
        metadata.put("buyer_id", approved.getId().toString());
        metadata.put("event_type", "buyer_approved");
        metadata.put("status", "active");
        
        ragClient.ingestDocument(ragContent, metadata);
        
        return approved;
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
