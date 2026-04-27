package com.roomsmanager.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAppResponse {
    
    private String appId;
    
    private String appSecret;
    
    private String appToken;
    
    private String appName;
    
    private String plan;
    
    private LocalDateTime createdAt;
    
    private Long expiresIn; // in milliseconds
}
