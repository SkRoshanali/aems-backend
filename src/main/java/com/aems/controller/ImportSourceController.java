package com.aems.controller;

import com.aems.dto.request.ImportSourceRequest;
import com.aems.entity.ImportSource;
import com.aems.service.ImportSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/import-sources")
@Tag(name = "Import Source Management", description = "APIs for managing import sources")
@SecurityRequirement(name = "Bearer Authentication")
public class ImportSourceController {
    
    @Autowired
    private ImportSourceService importSourceService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new import source")
    public ResponseEntity<ImportSource> createImportSource(@Valid @RequestBody ImportSourceRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ImportSource source = importSourceService.createImportSource(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(source);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update import source details")
    public ResponseEntity<ImportSource> updateImportSource(@PathVariable Long id, @Valid @RequestBody ImportSourceRequest request) {
        ImportSource source = importSourceService.updateImportSource(id, request);
        return ResponseEntity.ok(source);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get import source by ID")
    public ResponseEntity<ImportSource> getImportSourceById(@PathVariable Long id) {
        ImportSource source = importSourceService.getImportSourceById(id);
        return ResponseEntity.ok(source);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all import sources")
    public ResponseEntity<List<ImportSource>> getAllImportSources() {
        List<ImportSource> sources = importSourceService.getAllImportSources();
        return ResponseEntity.ok(sources);
    }
    
    @GetMapping("/active/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all active import sources")
    public ResponseEntity<List<ImportSource>> getActiveImportSources() {
        List<ImportSource> sources = importSourceService.getActiveImportSources();
        return ResponseEntity.ok(sources);
    }
    
    @GetMapping("/country/{country}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get import sources by country")
    public ResponseEntity<List<ImportSource>> getImportSourcesByCountry(@PathVariable String country) {
        List<ImportSource> sources = importSourceService.getImportSourcesByCountry(country);
        return ResponseEntity.ok(sources);
    }
    
    @GetMapping("/verified/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all verified import sources")
    public ResponseEntity<List<ImportSource>> getVerifiedImportSources() {
        List<ImportSource> sources = importSourceService.getVerifiedImportSources();
        return ResponseEntity.ok(sources);
    }
    
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Verify an import source")
    public ResponseEntity<ImportSource> verifyImportSource(@PathVariable Long id) {
        ImportSource source = importSourceService.verifyImportSource(id);
        return ResponseEntity.ok(source);
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Deactivate an import source")
    public ResponseEntity<ImportSource> deactivateImportSource(@PathVariable Long id) {
        ImportSource source = importSourceService.deactivateImportSource(id);
        return ResponseEntity.ok(source);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete an import source")
    public ResponseEntity<String> deleteImportSource(@PathVariable Long id) {
        importSourceService.deleteImportSource(id);
        return ResponseEntity.ok("Import source deleted successfully");
    }
}
