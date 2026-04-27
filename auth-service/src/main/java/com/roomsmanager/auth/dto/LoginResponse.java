package com.roomsmanager.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String appToken;
    
    private String refreshToken;
    
    private String appId;
    
    private Long expiresIn; // in milliseconds
    
    private String tokenType;
}
