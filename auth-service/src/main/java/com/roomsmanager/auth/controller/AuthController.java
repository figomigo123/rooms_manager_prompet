package com.roomsmanager.auth.controller;

import com.roomsmanager.auth.dto.*;
import com.roomsmanager.auth.entity.AppUser;
import com.roomsmanager.auth.service.ApplicationService;
import com.roomsmanager.auth.service.AppUserService;
import com.roomsmanager.auth.service.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private AppUserService appUserService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Register a new application
     * POST /auth/v1/apps/register
     */
    @PostMapping("/apps/register")
    public ResponseEntity<?> registerApplication(@Valid @RequestBody RegisterAppRequest request) {
        try {
            log.info("Received app registration request: {}", request.getAppName());
            RegisterAppResponse response = applicationService.registerApplication(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error registering application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Authenticate application
     * POST /auth/v1/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginApp(@Valid @RequestBody LoginRequest request) {
        try {
            // Verify app credentials
            if (!applicationService.verifyAppCredentials(request.getAppId(), request.getAppSecret())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid app credentials"));
            }
            
            // Generate tokens
            String appToken = jwtTokenProvider.generateAppToken(request.getAppId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getAppId());
            
            LoginResponse response = LoginResponse.builder()
                    .appToken(appToken)
                    .refreshToken(refreshToken)
                    .appId(request.getAppId())
                    .expiresIn(900000L)
                    .tokenType("Bearer")
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get application details
     * GET /auth/v1/apps/{appId}
     */
    @GetMapping("/apps/{appId}")
    public ResponseEntity<?> getApp(@PathVariable String appId) {
        try {
            Optional<?> app = applicationService.getApplicationById(appId);
            if (app.isPresent()) {
                return ResponseEntity.ok(app.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Application not found"));
        } catch (Exception e) {
            log.error("Error fetching application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Verify JWT token
     * POST /auth/v1/verify-token
     */
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid authorization header"));
            }
            
            String token = authHeader.substring(7);
            
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
            
            String subject = jwtTokenProvider.getSubjectFromToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("subject", subject);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * GET /auth/v1/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
