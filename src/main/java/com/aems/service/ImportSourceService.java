package com.aems.service;

import com.aems.dto.request.ImportSourceRequest;
import com.aems.entity.ImportSource;
import com.aems.entity.User;
import com.aems.repository.ImportSourceRepository;
import com.aems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImportSourceService {
    
    @Autowired
    private ImportSourceRepository importSourceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public ImportSource createImportSource(ImportSourceRequest request, Long userId) {
        Optional<ImportSource> existingSource = importSourceRepository.findBySourceCode(request.getSourceCode());
        if (existingSource.isPresent()) {
            throw new RuntimeException("Import source with code " + request.getSourceCode() + " already exists");
        }
        
        ImportSource source = new ImportSource();
        source.setCompanyName(request.getCompanyName());
        source.setSourceCode(request.getSourceCode());
        source.setCountry(request.getCountry());
        source.setCity(request.getCity());
        source.setAddress(request.getAddress());
        source.setContactPerson(request.getContactPerson());
        source.setPhoneNumber(request.getPhoneNumber());
        source.setEmail(request.getEmail());
        source.setWebsite(request.getWebsite());
        source.setIsActive(true);
        source.setIsVerified(false);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            source.setCreatedBy(user.get());
        }
        
        return importSourceRepository.save(source);
    }
    
    public ImportSource updateImportSource(Long id, ImportSourceRequest request) {
        ImportSource source = importSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Import source not found with ID: " + id));
        
        source.setCompanyName(request.getCompanyName());
        source.setCountry(request.getCountry());
        source.setCity(request.getCity());
        source.setAddress(request.getAddress());
        source.setContactPerson(request.getContactPerson());
        source.setPhoneNumber(request.getPhoneNumber());
        source.setEmail(request.getEmail());
        source.setWebsite(request.getWebsite());
        
        return importSourceRepository.save(source);
    }
    
    public ImportSource getImportSourceById(Long id) {
        return importSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Import source not found with ID: " + id));
    }
    
    public ImportSource getImportSourceByCode(String sourceCode) {
        return importSourceRepository.findBySourceCode(sourceCode)
                .orElseThrow(() -> new RuntimeException("Import source not found with code: " + sourceCode));
    }
    
    public List<ImportSource> getAllImportSources() {
        return importSourceRepository.findAll();
    }
    
    public List<ImportSource> getActiveImportSources() {
        return importSourceRepository.findByIsActive(true);
    }
    
    public List<ImportSource> getImportSourcesByCountry(String country) {
        return importSourceRepository.findByCountry(country);
    }
    
    public List<ImportSource> getVerifiedImportSources() {
        return importSourceRepository.findByIsVerified(true);
    }
    
    public ImportSource verifyImportSource(Long id) {
        ImportSource source = importSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Import source not found with ID: " + id));
        source.setIsVerified(true);
        return importSourceRepository.save(source);
    }
    
    public ImportSource deactivateImportSource(Long id) {
        ImportSource source = importSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Import source not found with ID: " + id));
        source.setIsActive(false);
        return importSourceRepository.save(source);
    }
    
    public void deleteImportSource(Long id) {
        if (!importSourceRepository.existsById(id)) {
            throw new RuntimeException("Import source not found with ID: " + id);
        }
        importSourceRepository.deleteById(id);
    }
}
