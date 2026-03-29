package com.aems.repository;

import com.aems.entity.ImportSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ImportSourceRepository extends JpaRepository<ImportSource, Long> {
    Optional<ImportSource> findBySourceCode(String sourceCode);
    boolean existsBySourceCode(String sourceCode);
    List<ImportSource> findByCountry(String country);
    List<ImportSource> findByIsActive(Boolean isActive);
    List<ImportSource> findByIsVerified(Boolean isVerified);
}
