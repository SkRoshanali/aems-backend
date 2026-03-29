package com.aems.service;

import com.aems.dto.request.LoginRequest;
import com.aems.dto.response.AuthResponse;
import com.aems.entity.User;
import com.aems.repository.UserRepository;
import com.aems.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Your account is pending admin approval. Please wait for activation.");
        }
        
        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account is locked. Please try again later.");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Reset failed attempts on successful login
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // TODO: Implement OTP verification for Admin and Super Admin
            // For now, OTP is disabled to allow admin access
            // if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN) {
            //     AuthResponse response = new AuthResponse();
            //     response.setRequiresOtp(true);
            //     response.setEmail(user.getEmail());
            //     response.setUserId(user.getId());
            //     return response;
            // }
            
            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            
            // Calculate session expiry (30 minutes from now)
            LocalDateTime sessionExpires = LocalDateTime.now().plusMinutes(30);
            user.setSessionExpires(sessionExpires);
            userRepository.save(user);
            
            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name(),
                    sessionExpires.toString()
            );
            
        } catch (Exception e) {
            // Increment failed attempts
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (user.getFailedAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            
            userRepository.save(user);
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, null, null
        );
        
        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);
        
        // Calculate session expiry (30 minutes from now)
        LocalDateTime sessionExpires = LocalDateTime.now().plusMinutes(30);
        user.setSessionExpires(sessionExpires);
        userRepository.save(user);
        
        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                sessionExpires.toString()
        );
    }
}
