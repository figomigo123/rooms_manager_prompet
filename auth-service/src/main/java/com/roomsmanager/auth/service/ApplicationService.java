package com.roomsmanager.auth.service;

import com.roomsmanager.auth.dto.RegisterAppRequest;
import com.roomsmanager.auth.dto.RegisterAppResponse;
import com.roomsmanager.auth.entity.Application;
import com.roomsmanager.auth.repository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Slf4j
@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new application
     */
    public RegisterAppResponse registerApplication(RegisterAppRequest request) {
        log.info("Registering new application: {}", request.getAppName());
        
        // Generate unique IDs
        String appId = "app_" + UUID.randomUUID().toString().substring(0, 12);
        String appSecret = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();
        
        // Create application
        Application app = Application.builder()
                .appId(appId)
                .appSecret(passwordEncoder.encode(appSecret))
                .appName(request.getAppName())
                .owner(request.getOwner())
                .plan(request.getPlan() != null ? request.getPlan() : "FREE")
                .apiKey(passwordEncoder.encode(apiKey))
                .webhookUrl(request.getWebhookUrl())
                .limits(Application.Limits.builder().build())
                .build();
        
        Application saved = applicationRepository.save(app);
        log.info("Application registered successfully: {}", appId);
        
        // Generate token
        String appToken = jwtTokenProvider.generateAppToken(appId);
        
        return RegisterAppResponse.builder()
                .appId(appId)
                .appSecret(appSecret) // Return plain secret only once
                .appToken(appToken)
                .appName(saved.getAppName())
                .plan(saved.getPlan())
                .createdAt(saved.getCreatedAt())
                .expiresIn(900000L) // 15 minutes
                .build();
    }
    
    /**
     * Get application by ID
     */
    public Optional<Application> getApplicationById(String appId) {
        return applicationRepository.findByAppId(appId);
    }
    
    /**
     * Get applications by owner
     */
    public List<Application> getApplicationsByOwner(String owner) {
        return applicationRepository.findByOwner(owner);
    }
    
    /**
     * Verify app credentials
     */
    public boolean verifyAppCredentials(String appId, String appSecret) {
        Optional<Application> appOpt = applicationRepository.findByAppId(appId);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            return passwordEncoder.matches(appSecret, app.getAppSecret());
        }
        return false;
    }
    
    /**
     * Update application
     */
    public Application updateApplication(String appId, Application appUpdates) {
        Optional<Application> appOpt = applicationRepository.findByAppId(appId);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            if (appUpdates.getAppName() != null) {
                app.setAppName(appUpdates.getAppName());
            }
            if (appUpdates.getWebhookUrl() != null) {
                app.setWebhookUrl(appUpdates.getWebhookUrl());
            }
            if (appUpdates.getPlan() != null) {
                app.setPlan(appUpdates.getPlan());
            }
            return applicationRepository.save(app);
        }
        return null;
    }
}
