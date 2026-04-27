package com.roomsmanager.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAppRequest {
    
    @NotBlank(message = "App name is required")
    private String appName;
    
    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be valid")
    private String owner;
    
    private String webhookUrl;
    
    private String plan;
}
