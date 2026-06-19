package com.aems.repository;

import com.aems.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByFarmerId(String farmerId);
    Optional<Farmer> findByEmail(String email);
    List<Farmer> findByIsActive(Boolean isActive);
    List<Farmer> findByIsVerified(Boolean isVerified);
}
