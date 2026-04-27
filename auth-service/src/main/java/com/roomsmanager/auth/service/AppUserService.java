package com.roomsmanager.auth.service;

import com.roomsmanager.auth.entity.AppUser;
import com.roomsmanager.auth.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
public class AppUserService {
    
    @Autowired
    private AppUserRepository appUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create or register a new app user
     */
    public AppUser createAppUser(String appId, String email, String name, String password, String role) {
        log.info("Creating new app user: {} for app: {}", email, appId);
        
        // Check if user already exists
        Optional<AppUser> existingUser = appUserRepository.findByAppIdAndEmail(appId, email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists with this email in this app");
        }
        
        AppUser user = AppUser.builder()
                .userId("user_" + UUID.randomUUID().toString().substring(0, 12))
                .appId(appId)
                .email(email)
                .name(name)
                .passwordHash(passwordEncoder.encode(password))
                .role(role != null ? role : "USER")
                .status("ACTIVE")
                .build();
        
        AppUser saved = appUserRepository.save(user);
        log.info("App user created: {}", saved.getUserId());
        return saved;
    }
    
    /**
     * Get user by app ID and email
     */
    public Optional<AppUser> getUserByAppIdAndEmail(String appId, String email) {
        return appUserRepository.findByAppIdAndEmail(appId, email);
    }
    
    /**
     * Get user by app ID and user ID
     */
    public Optional<AppUser> getUserByAppIdAndUserId(String appId, String userId) {
        return appUserRepository.findByAppIdAndUserId(appId, userId);
    }
    
    /**
     * Verify user password
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
    
    /**
     * Update last login time
     */
    public void updateLastLogin(String userId) {
        Optional<AppUser> userOpt = appUserRepository.findById(userId);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            appUserRepository.save(user);
        }
    }
    
    /**
     * Get all users for an app
     */
    public List<AppUser> getAllUsersForApp(String appId) {
        return appUserRepository.findByAppId(appId);
    }
    
    /**
     * Update user
     */
    public AppUser updateUser(String userId, AppUser userUpdates) {
        Optional<AppUser> userOpt = appUserRepository.findById(userId);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (userUpdates.getName() != null) {
                user.setName(userUpdates.getName());
            }
            if (userUpdates.getRole() != null) {
                user.setRole(userUpdates.getRole());
            }
            if (userUpdates.getStatus() != null) {
                user.setStatus(userUpdates.getStatus());
            }
            user.setUpdatedAt(LocalDateTime.now());
            return appUserRepository.save(user);
        }
        return null;
    }
    
    /**
     * Deactivate user
     */
    public AppUser deactivateUser(String userId) {
        Optional<AppUser> userOpt = appUserRepository.findById(userId);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            user.setStatus("INACTIVE");
            user.setUpdatedAt(LocalDateTime.now());
            return appUserRepository.save(user);
        }
        return null;
    }
}
