package com.aems.repository;

import com.aems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);
    List<User> findByRoleAndIsActiveAndIsVerifiedAndRejectedAtIsNull(User.Role role, Boolean isActive, Boolean isVerified);
}
