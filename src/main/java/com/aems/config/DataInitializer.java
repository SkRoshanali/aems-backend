package com.aems.config;

import com.aems.entity.Crop;
import com.aems.entity.ImportSource;
import com.aems.entity.User;
import com.aems.repository.CropRepository;
import com.aems.repository.ImportSourceRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CropRepository cropRepository;
    
    @Autowired
    private ImportSourceRepository importSourceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default Super Admin if not exists
        if (!userRepository.existsByEmail("superadmin@aems.com")) {
            User superAdmin = new User();
            superAdmin.setFullName("Super Administrator");
            superAdmin.setEmail("superadmin@aems.com");
            superAdmin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            superAdmin.setRole(User.Role.SUPER_ADMIN);
            superAdmin.setIsActive(true);
            superAdmin.setIsVerified(true);
            
            userRepository.save(superAdmin);
            System.out.println("✓ Default Super Admin created: superadmin@aems.com / Admin@123");
        }
        
        // Create demo users for testing
        createDemoUserIfNotExists("admin@aems.com", "Admin User", User.Role.ADMIN, "Admin@123");
        createDemoUserIfNotExists("manager@aems.com", "Manager User", User.Role.MANAGER, "Manager@123");
        createDemoUserIfNotExists("employee@aems.com", "Employee User", User.Role.EMPLOYEE, "Employee@123");
        createDemoUserIfNotExists("buyer@aems.com", "Buyer Company", User.Role.BUYER, "Buyer@123");
        
        // Create default crops
        createDefaultCrops();
        
        // Create default import sources
        createDefaultImportSources();
    }
    
    private void createDemoUserIfNotExists(String email, String fullName, User.Role role, String password) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setIsActive(true);
            user.setIsVerified(true);
            
            userRepository.save(user);
            System.out.println("✓ Demo user created: " + email + " / " + password);
        }
    }
    
    private void createDefaultCrops() {
        String[][] defaultCrops = {
            {"RICE001", "Basmati Rice", "GRAIN", "Premium long-grain aromatic rice"},
            {"RICE002", "Jasmine Rice", "GRAIN", "Fragrant long-grain rice from Thailand"},
            {"WHEAT001", "Durum Wheat", "GRAIN", "Hard wheat used for pasta"},
            {"CORN001", "Sweet Corn", "VEGETABLE", "Fresh sweet corn"},
            {"MANGO001", "Alphonso Mango", "FRUIT", "Premium variety of mango"},
            {"BANANA001", "Cavendish Banana", "FRUIT", "Most common banana variety"},
            {"TOMATO001", "Cherry Tomato", "VEGETABLE", "Small sweet tomatoes"},
            {"POTATO001", "Russet Potato", "VEGETABLE", "Large starchy potato"},
            {"ONION001", "Red Onion", "VEGETABLE", "Mild sweet onion"},
            {"COFFEE001", "Arabica Coffee", "SPICE", "High-quality coffee beans"}
        };
        
        for (String[] cropData : defaultCrops) {
            if (!cropRepository.existsByCropCode(cropData[0])) {
                Crop crop = new Crop();
                crop.setCropCode(cropData[0]);
                crop.setCropName(cropData[1]);
                crop.setCategory(cropData[2]);
                crop.setDescription(cropData[3]);
                crop.setIsActive(true);
                
                cropRepository.save(crop);
                System.out.println("✓ Default crop created: " + cropData[1]);
            }
        }
    }
    
    private void createDefaultImportSources() {
        String[][] defaultSources = {
            {"IMP001", "Global Agri Exports", "John Smith", "contact@globalagrexports.com", "+1-555-0101", "USA", "New York", "123 Export St", "www.globalagrexports.com"},
            {"IMP002", "Asian Farm Products", "Li Wei", "info@asianfarm.com", "+86-555-0102", "China", "Shanghai", "456 Trade Ave", "www.asianfarm.com"},
            {"IMP003", "Euro Fresh Imports", "Maria Garcia", "sales@eurofresh.eu", "+34-555-0103", "Spain", "Barcelona", "789 Market Rd", "www.eurofresh.eu"},
            {"IMP004", "India Organic Traders", "Raj Kumar", "contact@indiaorganic.in", "+91-555-0104", "India", "Mumbai", "321 Commerce Ln", "www.indiaorganic.in"},
            {"IMP005", "South American Produce", "Carlos Silva", "info@saproduce.com", "+55-555-0105", "Brazil", "São Paulo", "654 Export Blvd", "www.saproduce.com"}
        };
        
        for (String[] sourceData : defaultSources) {
            if (!importSourceRepository.existsBySourceCode(sourceData[0])) {
                ImportSource source = new ImportSource();
                source.setSourceCode(sourceData[0]);
                source.setCompanyName(sourceData[1]);
                source.setContactPerson(sourceData[2]);
                source.setEmail(sourceData[3]);
                source.setPhoneNumber(sourceData[4]);
                source.setCountry(sourceData[5]);
                source.setCity(sourceData[6]);
                source.setAddress(sourceData[7]);
                source.setWebsite(sourceData[8]);
                source.setIsActive(true);
                source.setIsVerified(true);
                
                importSourceRepository.save(source);
                System.out.println("✓ Default import source created: " + sourceData[1]);
            }
        }
    }
}
